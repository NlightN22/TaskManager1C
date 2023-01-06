package space.active.taskmanager1c.domain.use_case

import android.util.Patterns
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.UserSettings
import space.active.taskmanager1c.domain.models.User

class ValidateCredentials  {
    fun user(user: User): Boolean {
        val nameResult: Boolean = validateString(user.name)
        val idResult: Boolean = validateString(user.id)

        val finalResultList = listOf(nameResult,idResult)
        val finalRes = !finalResultList.contains(false)

        return finalRes
    }

    fun userName(user: String): Boolean = validateString(user)

    fun password(pass: String) = validateString(pass)


    private fun validateString(name: String?):Boolean {
        return name?.isNotBlank() ?: false
    }

    fun server(server: String?): Boolean {
        return server?.let { Patterns.WEB_URL.matcher(it).matches() } ?: false
    }

}