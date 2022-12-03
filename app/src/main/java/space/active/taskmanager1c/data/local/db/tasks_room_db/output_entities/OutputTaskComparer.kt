package space.active.taskmanager1c.data.utils

import space.active.taskmanager1c.coreutils.diff
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import kotlin.reflect.full.memberProperties

fun OutputTask.compareAndGetDiffs(toCompare: OutputTask): Map<String, String> {
    // prepare data class for compare clear values witch is always different
    val incomeWithoutId = toCompare.copy(outputId = 0)
    val inDBWithoutId = this.copy(outputId = 0)
    // create list or mapped params
    val incomeMap = incomeWithoutId.getParameterMap()
    val inDBMap = inDBWithoutId.getParameterMap()
    // compare two parameter maps and return result
    return incomeMap.diff(inDBMap)
}

fun OutputTask.getParameterMap(): Map<String,String> {

    val mapProperties: MutableMap<String, String> = mutableMapOf()

    OutputTask::class.memberProperties.forEach { member ->
        val name = member.name
        val value = member.get(this) as String

        mapProperties += Pair(name,value)
    }

    return mapProperties
}