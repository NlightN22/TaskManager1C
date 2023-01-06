package space.active.taskmanager1c.coreutils

import kotlinx.serialization.Serializable


/**
 * Set only boolean or string data types
 */
@Serializable
data class EncryptedData(
    val iv: ByteArray,
    val data: ByteArray,
    val type: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!iv.contentEquals(other.iv)) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    fun getBoolean(): Boolean? = wrapEncryptExceptions {
        if (this.type == Boolean::class.simpleName!!) {
            cryptoManager.decryptBoolean(this)
        } else {
            throw IllegalStateException()
        }
    }

    fun getString(): String? = wrapEncryptExceptions {
        if (this.type == String::class.simpleName!!) {
            cryptoManager.decryptString(this)
        } else {
            throw IllegalStateException()
        }
    }

    override fun toString(): String {
        return "${EncryptedData::class.simpleName}" +
                "(" +
                "iv=${iv.toString()}," +
                "data=${data.toString()}" +
                "type=${type.toString()}" +
                ")"
    }

    companion object {

        private val cryptoManager = CryptoManager()

        fun String.toEncryptedData(): EncryptedData? = wrapEncryptExceptions {
            cryptoManager.encrypt(this)
        }

        fun Boolean.toEncryptedData(): EncryptedData? = wrapEncryptExceptions {
            cryptoManager.encrypt(this)
        }

        private fun <T> wrapEncryptExceptions(block: () -> T): T? {
            return try {
                block()
            }
            catch (e: IllegalStateException){
                throw e
            }
            catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}