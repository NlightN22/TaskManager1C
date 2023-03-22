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

private const val TAG = "CachedFilesRepositoryImpl"

@Singleton
class CachedFilesRepositoryImpl @Inject constructor(
    context: Application,
    retrofit: Retrofit,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CachedFilesRepository, BaseRetrofitSource() {

    private val fileRepository = FileRepository(context)
    private val _loadingFiles = MutableStateFlow(emptyList<CachedFile>())
    private val _uploadedFiles = MutableStateFlow(emptyList<FileDTO>())

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)
    private lateinit var taskIdObserver: String
    private val filesObserver: TaskFilesObserver =
        TaskFilesObserver(
            logger,
        )

    override fun deleteCachedFile(cachedFile: CachedFile): Flow<Boolean> = flow {
        val file = cachedFile.uri?.toFile() ?: kotlin.run {
            emit(false)
            return@flow
        }
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
            if (cachedFile.filename != response.fileName) throw DeletedNameNotEqual(cachedFile, response)
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
            emptyList<FileDTO>()
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
            emptyList<CachedFile>()
        } else if (outputList.isNotEmpty() && loadingList.isEmpty()) {
            outputList
        } else {
//            logger.log(TAG, "loadingFiles: ${loadingFiles.joinToString("\n")}")
            outputList.map { output ->
                val loadingItem = loadingList.find { it.id == output.id }
                if (loadingItem != null) {
                    output.copy(loading = true, progress = loadingItem!!.progress)
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
        if (cachedFile.isCached() || cachedFile.isLoading()) throw DownloadException(cachedFile)
        val fileId = cachedFile.id
        val fileName = cachedFile.filename
        val downloadQuery = "downloadQuery taskId: $cachePathName fileId: $fileId"
        logger.log(TAG, downloadQuery)
        wrapLoadingException(cachedFile) {
            wrapRetrofitExceptions(query = downloadQuery) {
                val response = retrofitApi.downloadFile(auth.toBasic(), cachePathName, fileId)
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
    }.flowOn(ioDispatcher)

    override fun saveExternalFileToCache(uri: Uri, cacheDirPath: String): Flow<Boolean> = flow {
        try {
            fileRepository.saveFile(uri, cacheDirPath)
            emit(true)
        } catch (e: Throwable) {
            emit(false)
        }
    }.flowOn(ioDispatcher)

    override fun uploadFileToServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        cacheDirPath: String
    ): Flow<Request<CachedFile>> = flow {
        emit(PendingRequest())
        if (cachedFile.isLoading()) throw UploadException(cachedFile)
        wrapLoadingException(cachedFile) {
            val response = multiPartUploadFileToServerWithProgress(auth, cachedFile, cacheDirPath)
            response.collect { request ->
                when (request) {
                    is ProgressRequest -> {
                        logger.log(TAG, "uploadFileToServer progress: ${request.progress}")
                        cachedFile.setLoadingProgress(request.progress)
                    }
                    is SuccessRequest -> {
                        if (validateUploadResponse(request.data, cachedFile)) {
                            logger.log(TAG, "uploadFileToServer success: ${request.data}")
                            val uploadedFile =
                                updateUploadedCachedFile(request.data, cachedFile, cacheDirPath)
                            emit(SuccessRequest(uploadedFile))
                        }
                    }
                    else -> {}
                }
            }

        }
    }.flowOn(ioDispatcher)

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
        // get name cached file from response
        val newFileAfterUpload = fileRepository.getFilenameForUploadedFile(
            successResponse.fileName,
            successResponse.fileID,
            cacheDirPath
        )
        val newCachedAfterUpload = uploadedFile.setCached(newFileAfterUpload)
        val inputFile = uploadedFile.toFile()
        wrapLoadingException(newCachedAfterUpload) {
            // get and replace id from server onSuccess
            val renameRes =
                inputFile.renameTo(newFileAfterUpload)
            if (renameRes) {
                successResponse.updateServerListByResponse()
                delay(500)
            } else {
                throw IOException()
            }
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
            if (it.id == this.id && it.progress != progress) {
                it.copy(progress = progress)
            } else {
                it
            }
        }
    }

    private fun CachedFile.setLoadingFile(state: Boolean) {
        if (state) {
            _loadingFiles.value = _loadingFiles.value.plus(this)
        } else {
            val loadingItem = _loadingFiles.value.find { it.id == this.id }
            loadingItem?.let {
                _loadingFiles.value = _loadingFiles.value.minus(it)
            } ?: run {
                logger.log(TAG, "Can't find loading item with id: ${this.id}")
            }
        }
    }

    private suspend fun multiPartUploadFileToServer(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        taskId: String
    ): FileDTO {
        val fileName = cachedFile.filename
        val file = cachedFile.toFile()
        val uploadQuery = "auth: ${auth.toBasic()} fileName: $fileName"
        return wrapRetrofitExceptions(uploadQuery) {
            val fileNameBody = fileName.toRequestBody("multipart/form-data".toMediaType())
            val fileNamePart = MultipartBody.Part.createFormData("filename", null, fileNameBody)
            // prepare file
            val requestFileBody = file.asRequestBody("multipart/form-data".toMediaType())
            val filePart =
                MultipartBody.Part.createFormData("file", null, requestFileBody)
            val fullMultipartRequest = MultipartBody.Builder()
                .addPart(fileNamePart)
                .addPart(filePart)
                .build()
            retrofitApi.uploadFile(auth.toBasic(), taskId, fullMultipartRequest)
        }
    }

    private suspend fun multiPartUploadFileToServerWithProgress(
        auth: AuthBasicDto,
        cachedFile: CachedFile,
        taskId: String
    ) = callbackFlow<Request<FileDTO>> {
        trySend(PendingRequest())
        val fileName = cachedFile.filename
        val file = cachedFile.toFile()
        val uploadQuery = "auth: ${auth.toBasic()} fileName: $fileName"
        wrapRetrofitExceptions(uploadQuery) {
            val fileNameBody = fileName.toRequestBody("multipart/form-data".toMediaType())
            val fileNamePart = MultipartBody.Part.createFormData("filename", null, fileNameBody)
            // prepare file
            val requestFileBody = file.asRequestBody("multipart/form-data".toMediaType())
            val filePart =
                MultipartBody.Part.createFormData("file", null, requestFileBody)
            val fullMultipartRequest = MultipartBody.Builder()
                .addPart(fileNamePart)
                .addPart(filePart)
                .build()
            val requestBodyWithProgress =
                RequestBodyProgress(fullMultipartRequest) { progress ->
                    // Calculate and emit progress here
                    trySend(ProgressRequest(progress))
                }
            trySend(
                SuccessRequest(
                    retrofitApi.uploadFile(
                        auth.toBasic(),
                        taskId,
                        requestBodyWithProgress
                    )
                )
            )
        }
        awaitClose { close() }
    }

    override fun getCurrentCachePath(cacheDirPath: String): File =
        fileRepository.getCurrentCacheDir(cacheDirPath)

    override fun getCacheUriForSave(cacheDirPath: String): Uri =
        fileRepository.getNewFileUriContentProvider(cacheDirPath)

    private fun combineServerFilesAndCachedFiles(
        serverFiles: List<FileDTO>,
        cachedFiles: List<File>
    ): List<CachedFile> {
        return if (serverFiles.isEmpty() && cachedFiles.isEmpty()) {
            emptyList<CachedFile>()
        } else if (serverFiles.isNotEmpty() && cachedFiles.isEmpty()) {
            serverFiles.map { it.toNotCachedFile() }
        } else if (serverFiles.isEmpty() && cachedFiles.isNotEmpty()) {
            cachedFiles.map { it.newToCachedFile() }
        } else {
            val cachedMap = cachedFiles.associateBy { it.getFileId() }
            val serverConverted = serverFiles.map { fileDTO ->
                val file = cachedMap[fileDTO.fileID]
                if (file != null) {
                    fileDTO.toCachedFile(file)
                } else {
                    fileDTO.toNotCachedFile()
                }
            }
            // handle not uploaded files task in folder
            val serverIds = serverFiles.map { it.fileID }
            val notUploadedList =
                cachedMap.filterNot { serverIds.contains(it.key) }.values.map { file ->
                    file.toNotUploaded()
                }
            return serverConverted.plus(notUploadedList)
        }
    }

    private fun File.newToCachedFile() = CachedFile(
        uri = this.toUri(),
        id = this.getFileId(),
        filename = this.getFileName(),
        notUploaded = true,
        cached = true
    )

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

    private fun File.toNotUploaded() = CachedFile(
        uri = this.toUri(),
        id = this.getFileId(),
        filename = this.getFileName(),
        cached = true,
        notUploaded = true
    )

    private fun getFilesFromServerFlow(
        auth: AuthBasicDto,
        taskId: String
    ): Flow<List<FileDTO>> = flow<List<FileDTO>> {
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

    companion object {
        private const val UPDATE_FROM_SERVER_DELAY = 2000L

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