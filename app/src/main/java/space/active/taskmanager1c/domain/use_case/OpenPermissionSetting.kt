package space.active.taskmanager1c.domain.use_case

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import javax.inject.Inject

class OpenPermissionSetting @Inject constructor() {
    operator fun invoke(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= 31) {
            intent.action = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS

        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:" + context.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(intent)
    }
}