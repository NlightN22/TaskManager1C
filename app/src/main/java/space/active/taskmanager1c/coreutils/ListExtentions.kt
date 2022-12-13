package space.active.taskmanager1c.coreutils

fun <T> List<T>.addNotContainedFromList(inputList: List<T>): List<T> {
    /**
     * Find not equal items
     */
    val notAddedList = inputList.filterNot {
        this.contains(it)
    }

    /**
     *  Add not funded items and return new List
     */
    return this.plus(notAddedList)
}

fun <T> List<T>.addNotContained(item: T): List<T> {
    return if (!this.contains(item)) {
        this.plus(item)
    } else {
        this
    }
}

fun <T> List<T>.toggle(item: T): List<T> {
    return if (this.contains(item)) {
        this.minus(item)
    } else {
        this.plus(item)
    }
}