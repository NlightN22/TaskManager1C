package space.active.taskmanager1c.domain.use_case

import android.app.Application
import javax.inject.Inject

class LoadFromAsset @Inject constructor (
    private val context: Application
) {
    suspend operator  fun invoke(): String? {
        return try {
            val inputStream = context.assets.open("server_address.info")
            inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}