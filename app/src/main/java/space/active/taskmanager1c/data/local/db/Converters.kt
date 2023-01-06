package space.active.taskmanager1c.data.local.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import space.active.taskmanager1c.coreutils.EncryptedData
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.utils.JsonParser

@ProvidedTypeConverter
class Converters(
    private val jsonParser: JsonParser
)
{
    @TypeConverter
    fun listStringToJson(listString: List<String>): String {
        return jsonParser.toJson(
            listString,
            object : TypeToken<ArrayList<String>>(){}.type
        ) ?: "[]"
    }

    @TypeConverter
    fun jsonToListStrings (json: String): List<String> {
        return jsonParser.fromJson(
            json,
            object : TypeToken<ArrayList<String>>(){}.type
        ) ?: emptyList<String>()
    }

    @TypeConverter
    fun encryptedToString(encryptedData: EncryptedData): String =
        Json.encodeToString(EncryptedData.serializer(), encryptedData)

    @TypeConverter
    fun stringToEncrypted(json: String): EncryptedData =
        Json.decodeFromString(EncryptedData.serializer(), json)

    fun <T,R>mapToJson(map: Map<T,R>): String {
        return jsonParser.toJsonSimple(map) ?: ""
    }

    fun taskListDtoFromJson(json: String): TaskListDto? {
         return jsonParser.fromJson<TaskListDto>(
             json,
            object : TypeToken<TaskListDto>(){}.type)
    }

}