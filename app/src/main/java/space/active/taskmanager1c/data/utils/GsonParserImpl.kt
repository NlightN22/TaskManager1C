package space.active.taskmanager1c.data.utils

import com.google.gson.Gson
import java.lang.reflect.Type

class GsonParserImpl(
    private val gson: Gson
): JsonParser

{
    override fun <T> toJson(obj: T, type: Type): String? {
        return gson.toJson(obj, type)
    }

    override fun <T> fromJson(json: String, type: Type): T? {
        return gson.fromJson(json, type)
    }

    override fun <T> toJsonSimple(obj: T): String? {
        return gson.toJson(obj)
    }

}