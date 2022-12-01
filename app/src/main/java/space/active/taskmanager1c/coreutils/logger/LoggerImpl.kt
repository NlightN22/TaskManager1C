package space.active.taskmanager1c.coreutils.logger

import android.util.Log

object LoggerImpl: Logger {

    private val mainTAG = Logger.MAIN_TAG

    override fun log(localTag: String, message: String) {
        Log.d("$mainTAG $localTag", message)
    }

    override fun error(localTag: String, message: String) {
        Log.e("$mainTAG $localTag", message)
    }
}