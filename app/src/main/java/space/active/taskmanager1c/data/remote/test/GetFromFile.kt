package space.active.taskmanager1c.data.remote.test

import android.content.Context

class GetFromFile(
    private val context: Context
)
{
    operator fun invoke(): String {
        val inputStream = context.assets.open("demo_json/md4_original_cut.json") // demo_json
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer, Charsets.UTF_8)
        return json
    }
}