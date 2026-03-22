package space.active.taskmanager1c.data.local.cache_storage

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.files.FileDTO
import space.active.taskmanager1c.data.remote.retrofit.BaseRetrofitSource
import space.active.taskmanager1c.data.remote.retrofit.RequestBodyProgress
import space.active.taskmanager1c.data.remote.retrofit.RetrofitApi
import space.active.taskmanager1c.di.IoDispatcher
import java.io.File
import java.io.IOException
import java.security.InvalidParameterException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import org.json.JSONObject
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "CachedFilesRepositoryImpl"
private val activeUploadCalls = mutableMapOf<String, Call<FileDTO>>()


@Singleton
class CachedFilesRepositoryImpl @Inject constructor(
    context: Application,
    retrofit: Retrofit,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CachedFilesRepository, BaseRetrofitSource() {

    data class SizeTooHighException(val name: String) :
        Throwable(message = "File $name is larger than ${MAX_FILE_SIZE_BYTES / 1024L / 1024L}")

    data class DuplicateFileException(val name: String) :
        Throwable(message = "File $name already exists in cache")

    private val appContext = context
    val resolver = appContext.contentResolver
    private val fileRepository = FileRepository(context)
    private val _loadingFiles = MutableStateFlow(emptyList<CachedFile>())
    private val _uploadedFiles = MutableStateFlow(emptyList<FileDTO>())

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)
    private lateinit var taskIdObserver: String
    private val filesObserver: TaskFilesObserver =
        TaskFilesObserver(
            logger,
        )

    private data class CachedFileMeta(
        val hash: String,
        val sizeBytes: Long
    )

    override fun deleteCachedFile(cachedFile: CachedFile): Flow<Boolean> = flow {
        val file = cachedFile.uri?.toFile() ?: kotlin.run {
            emit(false)
            return@flow
        }

        deleteMetaFile(file)
        emit(fileRepository.deleteFile(file))
    }

    override fun deleteFileFromServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Flow<Boolean> = flow {
        if (cachedFile.notUploaded) throw DeleteNotExistingAtServer(cachedFile)
        wrapRetrofitExceptions {
            val response = retrofitApi.deleteFile(auth.toBasic(), cacheDirPath, cachedFile.id)
            if (cachedFile.id != response.fileID) throw DeletedIdNotEqual(cachedFile, response)
            if (cachedFile.filename != response.fileName) throw DeletedNameNotEqual(
                cachedFile,
                response
            )
            emit(true)
        }
    }

    override fun getFileList(auth: AuthBasicDto, cacheDirPath: String): Flow<List<CachedFile>> {
        taskIdObserver = cacheDirPath
        return combine(
            getFilesFromServerFlow(auth, cacheDirPath),
            _uploadedFiles
        ) { outputList, uploadedList ->
            updateServerList(outputList, uploadedList)
        }.combine(getCacheFilesListFlow()) { serverFiles, cachedFiles ->
            combineServerFilesAndCachedFiles(serverFiles, cachedFiles)
        }.combine(_loadingFiles) { outputList, loadingList ->
            updateLoadingFiles(outputList, loadingList)

        }.flowOn(ioDispatcher)
    }

    private fun updateServerList(
        serverList: List<FileDTO>,
        uploadedList: List<FileDTO>
    ): List<FileDTO> {
        return if (serverList.isEmpty() && uploadedList.isEmpty()) {
            emptyList()
        } else if (serverList.isNotEmpty() && uploadedList.isEmpty()) {
            serverList
        } else {
//            logger.log(TAG, "loadingFiles: ${loadingFiles.joinToString("\n")}")
            val uploadedMap = uploadedList.associateBy { it.fileID }
            val serverIds = serverList.map { it.fileID }
            // delete from uploaded list if it exist at server answer
            uploadedMap.filter { serverIds.contains(it.key) }.values.forEach { uploaded ->
                _uploadedFiles.value = _uploadedFiles.value.minus(uploaded)
            }
            val notContainedUploaded =
                uploadedMap.filterNot { serverIds.contains(it.key) }.map { it.value }
            return serverList.plus(notContainedUploaded)
        }
    }

    private fun updateLoadingFiles(
        outputList: List<CachedFile>,
        loadingList: List<CachedFile>
    ): List<CachedFile> {
        return if (outputList.isEmpty() && loadingList.isEmpty()) {
            emptyList()
        } else if (outputList.isNotEmpty() && loadingList.isEmpty()) {
            outputList
        } else {
            outputList.map { output ->
                val loadingItem = loadingList.find { it.id == output.id }
                if (loadingItem != null) {
                    output.copy(
                        loading = true,
                        progress = loadingItem.progress,
                        hash = loadingItem.hash ?: output.hash
                    )
                } else {
                    output
                }
            }
        }
    }

    override fun downloadFromServerToCache(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cachePathName: String
    ): Flow<Request<CachedFile>> = flow {
        emit(PendingRequest())
        try {
            if (cachedFile.isCached() || cachedFile.isLoading()) throw DownloadException(cachedFile)
            val fileId = cachedFile.id
            val fileName = cachedFile.filename
            val downloadQuery = "downloadQuery taskId: $cachePathName fileId: $fileId"
            logger.log(TAG, downloadQuery)
            wrapLoadingException(cachedFile) {
                wrapRetrofitExceptions(query = downloadQuery) {
                    val response = retrofitApi.downloadFile(auth.toBasic(), cachePathName, fileId)
                    logger.log(TAG, "response code $response.code()")
                    if (response.isSuccessful) {
                        val inputStream = response.body()?.byteStream()
                        inputStream?.let {
                            val contentLength = response.body()!!.contentLength()
                            val finalName = fileRepository.saveDownloadedFile(
                                inputStream,
                                cachePathName,
                                fileId,
                                fileName,
                                contentLength
                            )
                            finalName.collect { saveRequest ->
                                when (saveRequest) {
                                    is ProgressRequest -> {
                                        cachedFile.setLoadingProgress(saveRequest.progress)
                                    }

                                    is ErrorRequest -> {}
                                    is PendingRequest -> {}
                                    is SuccessRequest -> {
                                        logger.log(TAG, "save completed")
                                        emit(SuccessRequest(cachedFile.setCached(saveRequest.data)))
                                    }
                                }
                            }
                        }
                    } else {
                        throw HttpException(response)
                    }
                }

            }
        } catch (e: Throwable) {
            emit(ErrorRequest(e))
        }
    }.flowOn(ioDispatcher)

    override fun saveExternalFileToCache(uri: Uri, cacheDirPath: String): Flow<Boolean> = flow {
        try {
            val resolver = appContext.contentResolver

            val originalName = runCatching {
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
            }.getOrNull() ?: "file.bin"

            val inputSize = runCatching {
                resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            }.getOrNull() ?: -1L

            logger.log(
                TAG,
                "saveExternalFileToCache start: taskId=$cacheDirPath uri=$uri originalName=$originalName inputSize=$inputSize"
            )

            if (inputSize > MAX_FILE_SIZE_BYTES) {
                logger.log(TAG, "saveExternalFileToCache too large: inputSize=$inputSize name=$originalName")
                throw SizeTooHighException(originalName)
            }

            val fileHash = resolver.openInputStream(uri)?.use { sha256(it) }
                ?: throw IOException("Cannot open input stream for hash")

            logger.log(
                TAG,
                "saveExternalFileToCache input hash: name=$originalName hash=$fileHash"
            )

            val cacheDir = getCurrentCachePath(cacheDirPath)
            val existingFiles = cacheDir.listFiles()
                ?.filter { it.isFile && !it.isMetaFile() }
                .orEmpty()

            logger.log(
                TAG,
                "saveExternalFileToCache cache scan: dir=${cacheDir.absolutePath} filesCount=${existingFiles.size}"
            )

            existingFiles.forEach { cached ->
                val meta = readMetaFile(cached)
                logger.log(
                    TAG,
                    "cache file: name=${cached.name} abs=${cached.absolutePath} size=${cached.length()} metaHash=${meta?.hash} metaSize=${meta?.sizeBytes}"
                )
            }

            val duplicateFile = existingFiles.firstOrNull { cached ->
                val meta = readMetaFile(cached)
                meta?.hash == fileHash
            }

            if (duplicateFile != null) {
                val duplicateMeta = readMetaFile(duplicateFile)
                logger.log(
                    TAG,
                    "saveExternalFileToCache duplicate by hash: inputName=$originalName inputHash=$fileHash duplicateName=${duplicateFile.name} duplicatePath=${duplicateFile.absolutePath} duplicateMetaHash=${duplicateMeta?.hash} duplicateMetaSize=${duplicateMeta?.sizeBytes}"
                )
                emit(false)
                return@flow
            }

            val targetFile = buildTempImportFile(cacheDirPath, originalName)

            logger.log(
                TAG,
                "saveExternalFileToCache target file: name=${targetFile.name} abs=${targetFile.absolutePath}"
            )

            resolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Cannot open input stream for copy")

            targetFile.requireAllowedSize()

            writeMetaFile(
                targetFile,
                CachedFileMeta(
                    hash = fileHash,
                    sizeBytes = targetFile.length()
                )
            )

            logger.log(
                TAG,
                "saveExternalFileToCache saved: file=${targetFile.name} size=${targetFile.length()} metaHash=$fileHash"
            )

            emit(true)
        } catch (e: Throwable) {
            logger.log(TAG, "saveExternalFileToCache error: ${e.message}")
            emit(false)
        }
    }.flowOn(ioDispatcher)

    override fun uploadFileToServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Flow<Request<CachedFile>> = flow<Request<CachedFile>> {
        emit(PendingRequest<CachedFile>())

        if (cachedFile.isLoading()) throw UploadException(cachedFile)

        val inputFile = cachedFile.toFile()
        inputFile.requireAllowedSize()
        val fileHash = inputFile.sha256()

        val duplicateLoading = _loadingFiles.value.any {
            it.id != cachedFile.id && it.hash != null && it.hash == fileHash
        }
        if (duplicateLoading) {
            throw UploadException(cachedFile)
        }

        try {
            wrapLoadingException(cachedFile.copy(hash = fileHash, sizeBytes = inputFile.length())) {
                if (validateUploadOnServer(auth, cachedFile, cacheDirPath)) {
                    multiPartUploadFileToServerWithProgress(auth, cachedFile, cacheDirPath)
                        .collect { request ->
                            when (request) {
                                is ProgressRequest -> {
                                    logger.log(TAG, "uploadFileToServer progress: ${request.progress}")
                                    cachedFile.setLoadingProgress(request.progress)
                                }

                                is SuccessRequest -> {
                                    if (validateUploadResponse(request.data, cachedFile)) {
                                        logger.log(TAG, "uploadFileToServer success: ${request.data}")
                                        val uploadedFile = updateUploadedCachedFile(
                                            request.data,
                                            cachedFile.copy(hash = fileHash, sizeBytes = inputFile.length()),
                                            cacheDirPath
                                        )
                                        emit(SuccessRequest(uploadedFile))
                                    }
                                }

                                is ErrorRequest -> {
                                    logger.log(TAG, "uploadFileToServer error: ${request.exception.message}")
                                    emit(ErrorRequest<CachedFile>(request.exception))
                                }

                                is PendingRequest -> Unit
                            }
                        }
                }
            }
        } catch (e: Throwable) {
            emit(ErrorRequest<CachedFile>(e))
        }
    }.flowOn(ioDispatcher)


    override fun cancelUpload(cachedFile: CachedFile) {
        activeUploadCalls.remove(cachedFile.id)?.cancel()
        cachedFile.setLoadingFile(false)
    }

    private suspend fun validateUploadOnServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Boolean {
        val fileNameBody = "{ \"fileName\": \"${cachedFile.filename}\" }"
        wrapRetrofitExceptions(fileNameBody) {
            retrofitApi.checkFileName(auth.toBasic(), cacheDirPath, fileNameBody)
        }
        return true
    }

    private suspend fun wrapLoadingException(
        cachedFile: CachedFile,
        block: suspend () -> Unit
    ) {
        if (!cachedFile.loading) {
            cachedFile.setLoadingFile(state = true)
            try {
                block()
            } catch (e: Throwable) {
                cachedFile.setLoadingFile(state = false)
                throw e
            } finally {
                cachedFile.setLoadingFile(state = false)
            }
        }
    }

    private fun validateUploadResponse(response: FileDTO, cachedFile: CachedFile): Boolean {
        if (cachedFile.filename != response.fileName) {
            throw ParseBackendException(
                "Internal filename not equal server filename",
                InvalidParameterException()
            )
        }
        if (response.fileID == null) {
            throw ParseBackendException(
                "Null fileId in response from server",
                InvalidParameterException()
            )
        }
        return true
    }

    private suspend fun updateUploadedCachedFile(
        successResponse: FileDTO,
        uploadedFile: CachedFile,
        cacheDirPath: String,
    ): CachedFile {
        val newFileAfterUpload = fileRepository.getFilenameForUploadedFile(
            successResponse.fileName,
            successResponse.fileID,
            cacheDirPath
        )

        val newCachedAfterUpload = uploadedFile.setCached(newFileAfterUpload)
        val inputFile = uploadedFile.toFile()
        val oldMetaFile = inputFile.metaFile()
        val newMetaFile = newFileAfterUpload.metaFile()

        wrapLoadingException(newCachedAfterUpload) {
            val renameRes = inputFile.renameTo(newFileAfterUpload)
            if (!renameRes) throw IOException()

            if (oldMetaFile.exists()) {
                oldMetaFile.renameTo(newMetaFile)
            }

            successResponse.updateServerListByResponse()
            delay(500)
        }

        return newCachedAfterUpload
    }

    private fun FileDTO.updateServerListByResponse() {
        _uploadedFiles.value = _uploadedFiles.value.plus(this)
    }

    private fun CachedFile.isLoading(): Boolean = _loadingFiles.value.any { it.id == this.id }

    private fun CachedFile.isCached(): Boolean = _uploadedFiles.value.any { it.fileID == this.id }

    private fun CachedFile.setLoadingProgress(progress: Int) {
        _loadingFiles.value = _loadingFiles.value.map {
            if (it.id == this.id) {
                it.copy(progress = progress)
            } else {
                it
            }
        }
    }

    private fun CachedFile.setLoadingFile(state: Boolean) {
        if (state) {
            val updated = this.copy(loading = true)
            _loadingFiles.value = _loadingFiles.value
                .filterNot { it.id == this.id }
                .plus(updated)
        } else {
            _loadingFiles.value = _loadingFiles.value
                .filterNot { it.id == this.id }
        }
    }

    private fun multiPartUploadFileToServerWithProgress(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Flow<Request<FileDTO>> = callbackFlow<Request<FileDTO>> {
        trySend(PendingRequest<FileDTO>())

        val fileName = cachedFile.filename
        val file = cachedFile.toFile()
        if (!file.exists()) {
            trySend(ErrorRequest<FileDTO>(IOException("File not found: ${file.absolutePath}")))
            close()
            return@callbackFlow
        }

        val uploadQuery = "auth: ${auth.toBasic()} fileName: $fileName"

        try {
            val fileNameBody = fileName.toRequestBody("multipart/form-data".toMediaType())
            val fileNamePart = MultipartBody.Part.createFormData("filename", null, fileNameBody)

            val requestFileBody = file.asRequestBody("multipart/form-data".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", null, requestFileBody)

            val fullMultipartRequest = MultipartBody.Builder()
                .addPart(fileNamePart)
                .addPart(filePart)
                .build()

            val requestBodyWithProgress = RequestBodyProgress(fullMultipartRequest) { progress ->
                trySend(ProgressRequest(progress))
            }

            val call = retrofitApi.uploadFile(
                auth.toBasic(),
                cacheDirPath,
                requestBodyWithProgress
            )

            activeUploadCalls[cachedFile.id] = call
            logger.log(TAG, "upload started: $uploadQuery")

            call.enqueue(object : Callback<FileDTO> {
                override fun onResponse(call: Call<FileDTO>, response: Response<FileDTO>) {
                    activeUploadCalls.remove(cachedFile.id)

                    if (!response.isSuccessful) {
                        logger.log(TAG, "upload http error: ${response.code()}")
                        trySend(ErrorRequest<FileDTO>(HttpException(response)))
                        close()
                        return
                    }

                    val body = response.body()
                    if (body == null) {
                        logger.log(TAG, "upload empty body")
                        trySend(ErrorRequest<FileDTO>(IOException("Empty response body")))
                        close()
                        return
                    }

                    logger.log(TAG, "upload success: $body")
                    trySend(SuccessRequest(body))
                    close()
                }

                override fun onFailure(call: Call<FileDTO>, t: Throwable) {
                    activeUploadCalls.remove(cachedFile.id)

                    if (call.isCanceled) {
                        logger.log(TAG, "upload canceled: ${cachedFile.id}")
                        trySend(ErrorRequest<FileDTO>(CancellationException("Upload cancelled")))
                    } else {
                        logger.log(TAG, "upload failure: ${t.message}")
                        trySend(ErrorRequest<FileDTO>(t))
                    }
                    close()
                }
            })
        } catch (e: Throwable) {
            logger.log(TAG, "upload build error: ${e.message}")
            trySend(ErrorRequest<FileDTO>(e))
            close()
        }

        awaitClose {
            logger.log(TAG, "awaitClose upload: ${cachedFile.id}")
            activeUploadCalls.remove(cachedFile.id)?.cancel()
        }
    }

    override fun getCurrentCachePath(cacheDirPath: String): File =
        fileRepository.getCurrentCacheDir(cacheDirPath)

    override fun getCacheUriForSave(cacheDirPath: String): Uri =
        fileRepository.getNewFileUriContentProvider(cacheDirPath)

    private fun combineServerFilesAndCachedFiles(
        serverFiles: List<FileDTO>,
        cachedFiles: List<File>
    ): List<CachedFile> {
        val actualCachedFiles = cachedFiles.filter { it.isFile && !it.isMetaFile() }
        logger.log(
            TAG,
            "combineServerFilesAndCachedFiles: serverFiles=${serverFiles.joinToString { "${it.fileID}:${it.fileName}" }}"
        )

        logger.log(
            TAG,
            "combineServerFilesAndCachedFiles: cachedFiles=${cachedFiles.joinToString { it.name }}"
        )
        return if (serverFiles.isEmpty() && actualCachedFiles.isEmpty()) {
            emptyList()
        } else if (serverFiles.isNotEmpty() && actualCachedFiles.isEmpty()) {
            serverFiles.map { it.toNotCachedFile() }
        } else if (serverFiles.isEmpty() && actualCachedFiles.isNotEmpty()) {
            actualCachedFiles.map { it.newToCachedFile() }
        } else {
            val cachedMap = actualCachedFiles.associateBy { it.getFileId() }
            val serverConverted = serverFiles.map { fileDTO ->
                val file = cachedMap[fileDTO.fileID]
                if (file != null) fileDTO.toCachedFile(file) else fileDTO.toNotCachedFile()
            }

            val serverIds = serverFiles.mapNotNull { it.fileID }
            val notUploadedList = cachedMap
                .filterNot { serverIds.contains(it.key) }
                .values
                .map { file -> file.toNotUploaded() }

            serverConverted.plus(notUploadedList)
        }
    }

    private fun FileDTO.toCachedFile(file: File): CachedFile {
        val meta = readMetaFile(file)

        return CachedFile(
            uri = file.toUri(),
            id = fileID,
            filename = fileName,
            hash = meta?.hash,
            sizeBytes = meta?.sizeBytes ?: file.length(),
            cached = true,
            notUploaded = false
        )
    }

    private fun FileDTO.toNotCachedFile(): CachedFile {
        return CachedFile(
            uri = null,
            id = fileID,
            filename = fileName,
            hash = null,
            sizeBytes = 0L,
            cached = false,
            notUploaded = false
        )
    }

    private fun File.newToCachedFile(): CachedFile {
        val meta = readMetaFile(this)

        return CachedFile(
            uri = this.toUri(),
            id = this.getFileId(),
            filename = this.getFileName(),
            hash = meta?.hash,
            sizeBytes = meta?.sizeBytes ?: this.length(),
            notUploaded = true,
            cached = true
        )
    }

    private fun File.getFileId(): String {
        val fileId: String = this.name.substringBefore("@", "")
        if (fileId.isEmpty()) throw EmptyObject("Cached file ID name")
        return fileId
    }

    private fun File.getFileName(): String {
        val fileName: String = this.name.substringAfter("@", "")
        if (fileName.isEmpty()) throw EmptyObject("Cached taskID name")
        return fileName
    }

    private fun File.toNotUploaded(): CachedFile {
        val meta = readMetaFile(this)

        return CachedFile(
            uri = this.toUri(),
            id = this.getFileId(),
            filename = this.getFileName(),
            hash = meta?.hash,
            sizeBytes = meta?.sizeBytes ?: this.length(),
            cached = true,
            notUploaded = true
        )
    }

    private fun getFilesFromServerFlow(
        auth: AuthBasicDto,
        taskId: String
    ): Flow<List<FileDTO>> = flow {
        while (true) {
            wrapRetrofitExceptions(query = "tasks/$taskId/file") {
                emit(
                    retrofitApi.getFileList(auth.toBasic(), taskId).files
                )
                delay(UPDATE_FROM_SERVER_DELAY)
            }
        }
    }

    private fun getCacheFilesListFlow(): Flow<List<File>> {
        return filesObserver.observe(fileRepository.getCurrentCacheDir(taskIdObserver).path)
    }

    private fun File.requireAllowedSize() {
        if (!exists()) throw IOException("File not found: $absolutePath")
        if (length() > MAX_FILE_SIZE_BYTES) {
            throw InvalidParameterException("File is larger than 50 MB")
        }
    }

    private fun sha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) break
            digest.update(buffer, 0, read)
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun File.sha256(): String =
        inputStream().use { sha256(it) }

    private fun File.metaFile(): File =
        File(parentFile, "$name.meta")

    private fun File.isMetaFile(): Boolean =
        name.endsWith(".meta")

    private fun writeMetaFile(file: File, meta: CachedFileMeta) {
        val json = JSONObject()
            .put("hash", meta.hash)
            .put("sizeBytes", meta.sizeBytes)

        file.metaFile().writeText(json.toString())
    }

    private fun readMetaFile(file: File): CachedFileMeta? {
        val metaFile = file.metaFile()
        if (!metaFile.exists() || !metaFile.isFile) return null

        return runCatching {
            val json = JSONObject(metaFile.readText())
            CachedFileMeta(
                hash = json.getString("hash"),
                sizeBytes = json.optLong("sizeBytes", file.length())
            )
        }.getOrNull()
    }

    private fun deleteMetaFile(file: File) {
        runCatching { file.metaFile().delete() }
    }

    private fun buildTempImportFile(cacheDirPath: String, originalName: String): File {
        val safeName = originalName.ifBlank { "file.bin" }
        val generatedId = UUID.randomUUID().toString()
        val targetName = "${generatedId}@${safeName}"
        return File(getCurrentCachePath(cacheDirPath), targetName)
    }

    companion object {
        private const val UPDATE_FROM_SERVER_DELAY = 2000L

        const val MAX_FILE_SIZE_BYTES = 50L * 1024L * 1024L

        data class DownloadException(val cachedFile: CachedFile) :
            Throwable(message = "File ${cachedFile.filename} is already downloaded or in progress")

        data class UploadException(val cachedFile: CachedFile) :
            Throwable(message = "File ${cachedFile.filename} is already uploaded or in progress")

        data class DeleteNotExistingAtServer(val cachedFile: CachedFile) :
            Throwable(message = "File ${cachedFile.filename} is not uploaded at server")

        data class DeletedIdNotEqual(val cachedFile: CachedFile, val response: FileDTO) :
            Throwable(message = "File ${cachedFile.filename} ID ${cachedFile.id} is not equal response ID ${response.fileID}")

        data class DeletedNameNotEqual(val cachedFile: CachedFile, val response: FileDTO) :
            Throwable(message = "File ${cachedFile.filename} is not equal response filename ${response.fileName}")

    }
}