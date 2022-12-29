package space.active.taskmanager1c.domain.models


import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import space.active.taskmanager1c.coreutils.CryptoManager
import java.io.InputStream
import java.io.OutputStream

class UserSettingsSerializer(
    private val cryptoManager: CryptoManager
): Serializer<UserSettings>  {

    override val defaultValue: UserSettings
        get() = UserSettings()

    override suspend fun readFrom(input: InputStream): UserSettings {
        val decryptedBytes = cryptoManager.decrypt(input)
        return try {
            Json.decodeFromString(deserializer = UserSettings.serializer(),
            string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        cryptoManager.encrypt(
            byteArray = Json.encodeToString(
                serializer = UserSettings.serializer(),
                value = t
            ).encodeToByteArray(),
            outputStream = output
        )
    }
}