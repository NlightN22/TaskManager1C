package space.active.taskmanager1c.data.local.db.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.ConnectionException
import space.active.taskmanager1c.coreutils.ParseBackendException
import java.io.IOException

open class BaseRetrofitSource (
    retrofitConfig: RetrofitConfig
        ) {
    val retrofit = retrofitConfig.retrofit

    private val errorAdapter =
        retrofitConfig.moshi.adapter(ErrorResponseBody::class.java)

    /**
     * Map network exceptions:
     * @throws BackendException
     * @throws ParseBackendException
     * @throws ConnectionException
     */
    suspend fun <T> wrapRetrofitExceptions(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: JsonDataException) {
            throw ParseBackendException( inEx = e)
        } catch (e: JsonEncodingException) {
            throw ParseBackendException( inEx = e)
        } catch (e: HttpException) {
            throw transformBackendException(e)
        } catch (e: IOException) {
            throw ConnectionException(e)
        }
    }

    private fun transformBackendException(e: HttpException): Throwable {
        return try {
            val errorBody = e.response()!!.errorBody()!!.string()
            BackendException(errorBody = errorBody, errorCode = e.code().toString())
        } catch (e: Exception) {
            throw ParseBackendException(inEx = e)
        }
    }

    class ErrorResponseBody(
        val error: String
    )

}