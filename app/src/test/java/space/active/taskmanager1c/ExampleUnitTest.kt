package space.active.taskmanager1c


import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {

    @Test
    fun compareEqualsTwoDataClasses() {
        val class1 = TestClass1()
        val class2 = TestClass2()
        val expCompareResult = true

        val eqResult = class1.equalWithTest2(class2)

        assertEquals(expCompareResult, eqResult)
    }

    @Test
    fun compareHashTwoDataClasses() {
        val class1 = TestClass1()
        val class2 = TestClass2()
        val expCompareResult = true

        val hashResult = class1.hashWithTest2(class2)

        assertEquals(expCompareResult, hashResult)
    }

    @Test
    fun combineTwoListOfClasses() {
        val class1 = Combined1()
        val classList1 = listOf(
            class1,
            class1.copy(id = "2"),
            class1.copy(id = "3")
        )
        val class2 = Combined2(combined1 = class1)
        val classList2 = listOf(
            class2.copy(id = "1"),
            class2.copy(
                id = "2",
                combined1 = Combined1(id = "3", listStr = listOf("diff_value"))
            ),
            class2.copy(
                id = "3",
                combined1 = Combined1(id = "4", listStr = listOf("new_value"))
            ),
        )
        val expectedRes = true

        var result: Boolean = false

        // if lists is equal

        // приводим списки к одному типу
        val classList2to1 = classList2.map { it.combined1 }
        // находим неодинаковые значения
        val itemsFromList2NotInList1 = classList2to1.filterNot {
            classList1.contains(it)
        }
        // после этого находим по уникальному параметру заменяемое значение в базовом списке
        val replacedList = classList1.map { list1Item ->
            itemsFromList2NotInList1.find { list2Item -> (list1Item.id == list2Item.id) } ?: list1Item
        }

        // далее заменяем в базовом списке заменяемое значение
        val finalList = replacedList.addNotContained(classList2to1)

//        // проверяем на оставшие значения на добавление
//        val notAddedList = classList2to1.filterNot {
//            replacedList.contains(it)
//        }
//
//        val finalList = replacedList.plus(notAddedList)

        val combinedList = classList1.map { combined1 ->
            if (classList2to1.contains(combined1)) {
                classList2to1.find { comb2 -> combined1 == comb2 }
            } else {
                combined1
            }
        }
        result = false

        assertEquals(expectedRes, result)

    }

}

private fun <T> List<T>.addNotContained(inputList: List<T>): List<T> {
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

private fun <T> List<T>.insertByParameter(listValue: List<T>) {
    this.map { list1Item ->
        listValue.find { list1Item == it }
    }
}


data class Combined1(
    val id: String = "1",
    val listStr: List<String> = listOf("1", "2", "3"),
)

data class Combined2(
    val id: String = "1",
    val combined1: Combined1
) {
    fun isNotEq(combinedEq: Combined1): Boolean {
        return this.combined1 != combinedEq
    }
}

data class TestClass2(
    val name: String = "name1",
    val listNum: List<Int> = listOf(1, 2, 3, 4),
    val listStr: List<String> = listOf("1", "2", "3"),
    val arrayStr: ArrayList<String> = arrayListOf("1", "2", "3")
)

data class TestClass1(
    val name: String = "name1",
    val listNum: List<Int> = (1..4).toList(),
    val listStr: List<String> = listOf("1", "2", "3"),
    val arrayStr: ArrayList<String> = arrayListOf("1", "2", "3")
) {
    fun fromTestClass2(testClass2: TestClass2): TestClass1 = TestClass1(
        name = testClass2.name,
        listNum = testClass2.listNum,
        listStr = testClass2.listStr,
        arrayStr = testClass2.arrayStr
    )

    fun equalWithTest2(testClass2: TestClass2): Boolean {
        return this == fromTestClass2(testClass2)
    }

    fun hashWithTest2(testClass2: TestClass2): Boolean {
        return this.hashCode() == fromTestClass2(testClass2).hashCode()
    }
}