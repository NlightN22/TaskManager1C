package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.coreutils.diff
import space.active.taskmanager1c.data.remote.model.TaskDto
import kotlin.reflect.full.memberProperties

fun TaskDto.compareWithAndGetDiffs(compareWith: TaskDto): Map<String, Any> {
    // prepare data class for compare clear values witch is always different
    val incomeWithoutId = compareWith.copy(id = "")
    val thisWithoutId = this.copy(id = "")
    // create list or mapped params
    val incomeMap = incomeWithoutId.getParameterMap()
    val thisMap = thisWithoutId.getParameterMap()
    // compare two parameter maps and return result
    return incomeMap.diff(thisMap)
}

fun TaskDto.parameterDiffs(toCompare: TaskDto): List<Any> {
    // prepare data class for compare clear values witch is always different
    val incomeWithoutId = toCompare.copy(id = "")
    val inDBWithoutId = this.copy(id = "")
    // create list or mapped params
    val incomeList = incomeWithoutId.getParameters()
    val inDBList = inDBWithoutId.getParameters()
    // compare two parameter maps and return result
    val differenceList = mutableListOf<Any>()
    incomeList.forEach {
        if (!inDBList.contains(it)) {
            differenceList += it
        }
    }
    return differenceList
}

fun TaskDto.getParameterMap(): Map<String,Any> {

    val mapProperties: MutableMap<String, Any> = mutableMapOf()

    TaskDto::class.memberProperties.forEach { member ->
        val name = member.name
        // broke at List<String>
        val value = member.get(this)

        mapProperties += Pair(name,value?:"")
    }
    return mapProperties
}

fun TaskDto.getParameters(): List<Any> {

    val properties = mutableListOf<Any>()

    TaskDto::class.memberProperties.forEach { member ->
        properties += member
    }

    return properties
}