package space.active.taskmanager1c.domain.use_case

import android.util.Patterns
import space.active.taskmanager1c.domain.models.UserSettings

class ValidateCredentials  {
    operator fun invoke(userSettings: UserSettings): Boolean {
        val nameResult: Boolean = validateString(userSettings.username)
        val idResult: Boolean = validateString(userSettings.userId)
        val passResult: Boolean = validateString(userSettings.password)
        val serverResult: Boolean = validateServer(userSettings.serverAddress)

        val finalResultList = listOf(nameResult,idResult,passResult,serverResult)
        val finalRes = !finalResultList.contains(false)

        return finalRes
    }

    private fun validateString(name: String?):Boolean {
        return name?.isNotBlank() ?: false
    }

    private fun validateServer(server: String?): Boolean {
        return server?.let { Patterns.WEB_URL.matcher(it).matches() } ?: false
    }

}