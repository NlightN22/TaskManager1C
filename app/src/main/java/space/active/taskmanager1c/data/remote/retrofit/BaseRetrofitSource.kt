package space.active.taskmanager1c.data.remote.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.AuthException
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.ConnectionException
import space.active.taskmanager1c.coreutils.ParseBackendException
import java.io.IOException

open class BaseRetrofitSource {
    /**
     * Map network exceptions:
     * @throws BackendException
     * @throws ParseBackendException
     * @throws ConnectionException
     */
    suspend fun <T> wrapRetrofitExceptions(query: Any? = null, block: suspend () -> T): T {
        return try {
            block()
        } catch (e: JsonDataException) {
            throw ParseBackendException(message = e.message, cause = e.cause)
        } catch (e: JsonEncodingException) {
            throw ParseBackendException(message = e.message, cause = e.cause)
        } catch (e: HttpException) {
            throw transformBackendException(e, query)
        } catch (e: IOException) {
            throw ConnectionException(e)
        }
    }

    private fun transformBackendException(e: HttpException, query: Any?): Throwable {
        return try {
            val errorBody = e.response()!!.errorBody()!!.string()
            if (e.code() == 401) { throw AuthException}
            BackendException(errorBody = errorBody, errorCode = e.code().toString(), query)
        } catch ( e: AuthException) {
            throw AuthException
        }
        catch (e: Exception) {
            throw ParseBackendException(message = e.message, cause = e.cause)
        }
    }
}