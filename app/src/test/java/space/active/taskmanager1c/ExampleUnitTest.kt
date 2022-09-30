package space.active.taskmanager1c


import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


//    @Test
//    fun test_suspends() = runBlocking {
//        var job1 = launch(Dispatchers.Unconfined) { start_auto()}
//        delay(500L)
//        start_manual()
//        delay(500L)
//        if (!job1.isActive) {
//            println("Job1 start new one")
//            job1 = launch(Dispatchers.Unconfined) { start_auto() }
//        }
//    }
//
//    var i = 0
//    suspend fun start_auto() {
//        while (true) {
//            println("Auto val: ${increase_value()}")
//            delay(1000L)
//        }
//    }
//
//    suspend fun start_manual() {
//        println("Manual val: ${increase_value()}")
//    }
//
//    fun increase_value(): Int {
//        return i++
//    }
}