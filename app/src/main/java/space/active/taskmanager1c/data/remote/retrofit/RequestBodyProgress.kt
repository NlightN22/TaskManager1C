package space.active.taskmanager1c.data.remote.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

class RequestBodyProgress(
    private val requestBody: RequestBody,
    private val onProgressUpdate: (progress: Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = requestBody.contentType()

    override fun contentLength(): Long = requestBody.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten = 0L

        private var currentProgress: Int = -1
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val progress = (bytesWritten * 100 / contentLength()).toInt()
            if (currentProgress != progress) {
                onProgressUpdate(progress)
            }
            currentProgress = progress
        }
    }
}