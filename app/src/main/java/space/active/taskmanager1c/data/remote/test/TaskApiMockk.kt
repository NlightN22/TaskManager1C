package space.active.taskmanager1c.data.remote.test

import android.content.Context

class TaskApiMockk(
    private val context: Context
) {
    fun getTaskDtoFromFile(): String {
        val inputStream = context.assets.open("demo_json/md4_original.json") // demo_json
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer, Charsets.UTF_8)
        return json
    }
}