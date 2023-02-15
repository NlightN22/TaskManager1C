package space.active.taskmanager1c.data.local.cache_storage

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.Credentials
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
import space.active.taskmanager1c.data.remote.retrofit.RetrofitApi
import space.active.taskmanager1c.di.IoDispatcher
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.InvalidParameterException
import javax.inject.Inject

private const val TAG = "CachedFilesRepositoryImpl"

class CachedFilesRepositoryImpl @Inject constructor(
    private val context: Application,
    private val retrofit: Retrofit,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CachedFilesRepository, BaseRetrofitSource() {

    private val fileRepository = FileRepository(context)
    private val _loadingFiles = MutableStateFlow(emptyList<CachedFile>())
    private val _uploadedFiles = MutableStateFlow(emptyList<FileDTO>())

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)
    private lateinit var taskIdObserver: String
    private val filesObserver: TaskFilesObserver by lazy {
        TaskFilesObserver(
            logger,
            fileRepository.getCurrentCacheDir(taskIdObserver)
        )
    }

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
    ) {
        TODO("Not yet implemented")
    }

    override fun uploadFileToServer(
        auth: AuthBasicDto,
        inputStream: InputStream,
        cacheDirPath: String
    ): Flow<Request<CachedFile>> {
        TODO("Not yet implemented")
    }

    override fun uploadFileToServer(
        auth: AuthBasicDto,
        cacheDirPath: Uri,
        cachePathName: String
    ): Flow<Request<CachedFile>> {
        TODO("Not yet implemented")
    }

    override fun getFileList(auth: AuthBasicDto, cacheDirPath: String): Flow<List<CachedFile>> {
        taskIdObserver = cacheDirPath
        // todo delete after tests
//        return combine(
//            getFilesFromServerFlow(auth, cacheDirPath),
//            getCacheFilesListFlow()) { serverFiles, cachedFiles ->
//            combineServerFilesAndCachedFiles(serverFiles, cachedFiles)
//        }.combine(_loadingFiles) { outputList, loadingList ->
//            updateLoadingFiles(outputList, loadingList)
//        }.flowOn(ioDispatcher)

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
                    if (loadingList.map { it.id }.contains(output.id)) {
                        output.copy(loading = true)
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
            val fileId = cachedFile.id
            val fileName = cachedFile.filename
            val downloadQuery = "auth: ${auth.toBasic()} taskId: $cachePathName fileId: $fileId"
            logger.log(TAG, "downloadQuery $downloadQuery")
            wrapLoadingException(cachedFile) {
                wrapRetrofitExceptions(query = downloadQuery) {
                    val response = retrofitApi.downloadFile(auth.toBasic(), cachePathName, fileId)
                    if (response.isSuccessful) {
                        val inputStream = response.body()?.byteStream()
                        inputStream?.let {
                            val finalName = fileRepository.saveDownloadedFile(
                                inputStream,
                                cachePathName,
                                fileId,
                                fileName
                            )
                            cachedFile.setLoadingFile(false)
                            emit(SuccessRequest(cachedFile.setCached(finalName)))
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
            val fileName = cachedFile.filename
            val inputFile = cachedFile.toFile()
            // todo get progress by flow
            wrapLoadingException(cachedFile) {
                val response = multiPartUploadFileToServer(auth, fileName, inputFile, cacheDirPath)
                logger.log(TAG, "uploadFileToServer response: $response")
                if (fileName != response.fileName) {
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
                // get name cached file from response
                val newFileAfterUpload = fileRepository.getFilenameForUploadedFile(
                    response.fileName,
                    response.fileID,
                    cacheDirPath
                )
                val newCachedAfterUpload = cachedFile.setCached(newFileAfterUpload)
                wrapLoadingException(newCachedAfterUpload) {
                    // get and replace id from server onSuccess
                    val renameRes =
                        inputFile.renameTo(newFileAfterUpload)
                    if (renameRes) {
                        _uploadedFiles.value = _uploadedFiles.value.plus(response)
                        newCachedAfterUpload.setLoadingFile(state = false)
                        emit(SuccessRequest(newCachedAfterUpload))
                    } else {
                        throw IllegalStateException()
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

        private fun CachedFile.setLoadingFile(state: Boolean) {
            if (state) {
                _loadingFiles.value = _loadingFiles.value.plus(this)
            } else {
                _loadingFiles.value = _loadingFiles.value.minus(this)
            }
        }

        private suspend fun multiPartUploadFileToServer(
            auth: AuthBasicDto,
            fileName: String,
            file: File,
            taskId: String
        ): FileDTO {
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
            return filesObserver.getFlow()
        }

        private fun AuthBasicDto.toBasic(): String =
            Credentials.basic(this.name, this.pass, StandardCharsets.UTF_8)

        companion object {
            private const val UPDATE_FROM_SERVER_DELAY = 2000L
        }
    }