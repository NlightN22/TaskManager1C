package space.active.taskmanager1c.data.remote

import okhttp3.Credentials
import okhttp3.Interceptor
import java.io.IOException
import java.nio.charset.StandardCharsets

class BasicAuthInterceptor(user: String, password: String) : Interceptor {

    private val credentials: String = Credentials.basic(user, password, StandardCharsets.UTF_8)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", credentials)
            .build()
        return chain.proceed(authenticatedRequest)
    }

}
