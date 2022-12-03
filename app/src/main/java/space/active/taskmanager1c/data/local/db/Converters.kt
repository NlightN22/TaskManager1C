package space.active.taskmanager1c.data.local.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
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

    fun <T,R>mapToJson(map: Map<T,R>): String {
        return jsonParser.toJsonSimple(map) ?: ""
    }
}