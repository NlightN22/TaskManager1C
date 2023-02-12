package space.active.taskmanager1c.data.repository

import android.app.Application
import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Credentials
import retrofit2.HttpException
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.TaskFilesObserver
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.files.FileDTO
import space.active.taskmanager1c.data.remote.retrofit.BaseRetrofitSource
import space.active.taskmanager1c.data.remote.retrofit.RetrofitApi
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.InternalStorageFile
import space.active.taskmanager1c.domain.repository.FilesRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "FilesRepositoryImpl"

class FilesRepositoryImpl @Inject constructor(
    private val context: Application,
    private val retrofit: Retrofit,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FilesRepository, BaseRetrofitSource() {

    private val retrofitApi = retrofit.create(RetrofitApi::class.java)
    lateinit var taskIdObserver: String
    private val filesObserver: TaskFilesObserver by lazy {
        TaskFilesObserver(
            logger,
            getTaskCacheDir(context, taskIdObserver)
        )
    }

    override fun getFileList(auth: AuthBasicDto, taskId: String): Flow<List<InternalStorageFile>> {
        taskIdObserver = taskId
        return combine(
            getTaskFilesFromServerFlow(auth, taskId),
            getTaskCacheDirListFlow()
        ) { serverFiles, cachedFiles ->
//            logger.log(TAG, "cachedFiles: ${cachedFiles.map { it.name }}")
            combineServerFilesAndCachedFiles(serverFiles, cachedFiles)
        }.flowOn(ioDispatcher)

    }

    override fun downloadFileToCache(
        auth: AuthBasicDto,
        taskId: String,
        fileId: String,
        fileName: String
    ): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        val downloadQuery = "auth: ${auth.toBasic()} taskId: $taskId fileId: $fileId"
        logger.log(TAG, "downloadQuery $downloadQuery")
        wrapRetrofitExceptions(query = downloadQuery) {
            val response = retrofitApi.downloadFile(auth.toBasic(), taskId, fileId)
            if (response.isSuccessful) {
                val inputStream = response.body()?.byteStream()
                wrapOutputStreamExceptions {
                    inputStream?.use { input ->
                        val cacheFile =
                            File(
                                getTaskCacheDir(context, taskId),
                                createCachedFilename(fileId, fileName)
                            )
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                        emit(SuccessRequest(Any()))
                    }
                }
            } else {
                throw HttpException(response)
            }
        }
    }.flowOn(ioDispatcher)

    override fun uploadFileToServer(file: File): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }

    private suspend fun <T> wrapOutputStreamExceptions(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: FileNotFoundException) {
            throw FileDownloadException
        } catch (e: IOException) {
            throw FileDownloadException
        }
    }

    private fun combineServerFilesAndCachedFiles(
        serverFiles: List<FileDTO>,
        cachedFiles: List<File>
    ): List<InternalStorageFile> {
        return if (serverFiles.isEmpty() && cachedFiles.isEmpty()) {
            emptyList<InternalStorageFile>()
        } else if (serverFiles.isNotEmpty() && cachedFiles.isEmpty()) {
            serverFiles.map { it.toInternalStorageFile() }
        } else if (serverFiles.isEmpty() && cachedFiles.isNotEmpty()) {
            cachedFiles.map { it.toInternalStorageFile() }
        } else {
            val cachedMap = cachedFiles.associateBy { it.getFileId() }
            val serverConverted = serverFiles.map {
                val file = cachedMap[it.fileID]
                if (file != null) {
                    InternalStorageFile(
                        uri = file.toUri(),
                        id = it.fileID,
                        filename = it.fileName,
                        cached = true
                    )
                } else {
                    it.toInternalStorageFile()
                }
            }
            // handle not uploaded files task in folder
            val serverIds = serverFiles.map { it.fileID }
            val notUploadedList = cachedMap.filterNot { serverIds.contains(it.key) }.values.map { file->
                InternalStorageFile(
                    uri = file.toUri(),
                    id = file.getFileId(),
                    filename = file.getFileName(),
                    cached = true,
                    notUploaded = true
                )
            }
            return serverConverted.plus(notUploadedList)
        }
    }

    private fun createCachedFilename(fileId: String, fileName: String): String {
        return "$fileId@$fileName"
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

    private fun File.toInternalStorageFile() = InternalStorageFile(
        uri = this.toUri(),
        id = null,
        filename = this.name,
        notUploaded = true
    )

    private fun getTaskFilesFromServerFlow(
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

    private fun getTaskCacheDirListFlow(): Flow<List<File>> {
        return filesObserver.getFlow()
    }

    private fun AuthBasicDto.toBasic(): String =
        Credentials.basic(this.name, this.pass, StandardCharsets.UTF_8)

    companion object {
        private const val UPDATE_FROM_SERVER_DELAY = 2000L

        fun getTaskCacheDir(context: Context, taskId: String): File {
            val cacheDir: File = context.cacheDir
            val subfolder = File(cacheDir, taskId)
            if (!subfolder.exists()) subfolder.mkdir()
            return subfolder
        }
    }
}