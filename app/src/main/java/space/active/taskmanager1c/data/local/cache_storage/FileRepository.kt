package space.active.taskmanager1c.data.local.cache_storage

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import space.active.taskmanager1c.coreutils.FileDownloadException
import space.active.taskmanager1c.coreutils.ProgressRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FileRepository(
    private val context: Application,
) {
    // saveNew

    suspend fun deleteFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (file.exists()) file.delete()
                return@withContext true
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    // save downloaded file
    fun saveDownloadedFile(
        inputStream: InputStream, cachedPathDir: String,
        fileId: String, fileName: String,
        contentLength: Long
    ): Flow<Request<File>> {
        val newDownloadedFile =
            File(getCurrentCacheDir(cachedPathDir), createCachedFilename(fileId, fileName))
        return saveFileProgress(inputStream, newDownloadedFile, contentLength)
    }

    suspend fun saveFile(uri: Uri, cacheDirPath: String): File {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val selectedCacheFile = getFilenameForSelectedFile(uri.toFileName(), cacheDirPath)
            selectedCacheFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()
            selectedCacheFile
        }
    }

    suspend fun saveFile(inputStream: InputStream, file: File): File {
        wrapOutputStreamExceptions {
            inputStream.use { input ->
                val downloadPartFile =
                    setDownloadPartName(finalName = file)
                downloadPartFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                val finalRename = downloadPartFile.renameTo(file)
                if (!finalRename) {
                    deleteCacheAfterError(downloadPartFile)
                    deleteCacheAfterError(file)
                    throw IllegalStateException()
                }
            }
        }
        return file
    }

    private fun saveFileProgress(inputStream: InputStream, file: File, contentLength: Long) = flow<Request<File>> {
        wrapOutputStreamExceptions {
            emit(ProgressRequest(0))
            inputStream.use { input ->
                val downloadPartFile =
                    setDownloadPartName(finalName = file)
                downloadPartFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead = input.read(buffer)
                    var bytesCopied = 0L
                    var currentPercent = -1
                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        val percent =
                            (bytesCopied.toFloat() / contentLength.toFloat() * 100).toInt()
                        if (currentPercent != percent) {
                            emit(ProgressRequest(percent))
                        }
                        currentPercent = percent
                        bytesRead = inputStream.read(buffer)
                    }
                }
                val finalRename = downloadPartFile.renameTo(file)
                if (!finalRename) {
                    deleteCacheAfterError(downloadPartFile)
                    deleteCacheAfterError(file)
                    throw IOException()
                }
            }
        }
        emit(SuccessRequest(file))
    }

    /**
     * Add extension ".part" for downloaded files
     */
    private fun setDownloadPartName(finalName: File): File {
        return finalName.resolveSibling("$finalName.part")
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

    fun getCurrentCacheDir(cacheDirPath: String): File {
        val cacheDir: File = context.cacheDir
        val subfolder = File(cacheDir, cacheDirPath)
        if (!subfolder.exists()) subfolder.mkdir()
        return subfolder
    }

    fun getNewFilename(cacheDirPath: String): File {
        val taskFolder = getCurrentCacheDir(cacheDirPath)
        return File(taskFolder, createNewFileName())
    }

    fun getNewFileUriContentProvider(cacheDirPath: String): Uri {
        return FileProvider.getUriForFile(
            context,
            "space.active.taskmanager1c.fileprovider",
            getNewFilename(cacheDirPath)
        )
    }

    fun getFilenameForUploadedFile(fileName: String, fileId: String, cacheDirPath: String): File {
        val finalName = "$fileId@$fileName"
        val cacheDir = getCurrentCacheDir(cacheDirPath)
        return File(cacheDir, finalName)
    }

    fun getFilenameForSelectedFile(fileName: String, cacheDirPath: String): File {
        val fileId = UUID.randomUUID().toString()
        val finalName = "$fileId@$fileName"
        val cacheDir = getCurrentCacheDir(cacheDirPath)
        return File(cacheDir, finalName)
    }

    private fun createNewFileName(): String {
        // name has 2 parts: fileId @ filename.jpg
        // new fileId must unique
        // fileId replaced by server after upload
        val fileId = UUID.randomUUID().toString()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
        val filename = "$timeStamp.jpg"
        val finalName = "$fileId@$filename"
        return finalName
    }

    fun deleteCacheAfterError(file: File) {
        if (file.exists()) file.delete()
    }

    private fun Uri.toFileName(): String {
        var result: String? = null
        if (this.scheme == "content") {
            context.contentResolver.query(this, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = this.path
            result = this.path?.substringAfterLast("/")
        }
        return result ?: throw IllegalStateException()
    }

    private fun createCachedFilename(fileId: String, fileName: String): String {
        return "$fileId@$fileName"
    }
}