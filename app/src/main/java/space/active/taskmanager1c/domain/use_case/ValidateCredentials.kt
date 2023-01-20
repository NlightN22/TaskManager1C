package space.active.taskmanager1c.domain.use_case

import android.util.Patterns
import space.active.taskmanager1c.domain.models.UserDomain

class ValidateCredentials  {
    fun user(userDomain: UserDomain): Boolean {
        val nameResult: Boolean = validateString(userDomain.name)
        val idResult: Boolean = validateString(userDomain.id)

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