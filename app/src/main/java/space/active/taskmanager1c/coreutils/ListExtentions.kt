package space.active.taskmanager1c.coreutils

fun <T> List<T>.addNotContained(inputList: List<T>): List<T> {
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