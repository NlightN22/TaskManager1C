package space.active.taskmanager1c.coreutils

import android.util.Log

/**
 * Compare two maps current "this" and coming
 * The current map will be the base one,
 * with which the incoming one will be compared
 * WARNING! Keys and size must equal!
 */
fun <T,R> Map<T,R>.diff(incomingMapToCompare: Map<T,R>): Map<T,R> {

    val diff = mutableMapOf<T,R>()

    // compare hashs and return empty map if they are equal
    if (this == incomingMapToCompare) {
        return diff
    }
    // compare size and return error if they not equal
    if (this.size != incomingMapToCompare.size) {
        throw IllegalStateException()
    }
    // compare key by key if they not equal return error
    this.keys.forEach { key ->
        if (! incomingMapToCompare.containsKey(key)) {
            throw IllegalStateException()
        }
    }
    // compare values in all keys and return different
    this.forEach { (key, value) ->
        if (incomingMapToCompare[key] != value) {
            // todo delete debug
            if (key == "observers") { Log.d("Map.diff", "observers: $value -- ${incomingMapToCompare[key]}" )}
            diff += Pair(key,value)
        }
    }
    // todo delete debug
    Log.d("Map.diff", diff.toString())
    return diff
}