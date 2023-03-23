package space.active.taskmanager1c.domain.use_case

import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import androidx.core.net.toUri
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.BASE_URL
import javax.inject.Inject

private const val TAG = "VerifyDeepLinkPermission"

class VerifyDeepLinkPermission @Inject constructor(
    private val logger: Logger
) {
    operator fun invoke(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 31) {
            val manager = context.getSystemService(DomainVerificationManager::class.java)
            val userState = manager.getDomainVerificationUserState(context.packageName)
            val verifiedDomains = userState?.hostToStateMap
                ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }
            val selectedDomains = userState?.hostToStateMap
                ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }
            val unapprovedDomains = userState?.hostToStateMap
                ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }
            logger.log(TAG, "verifiedDomains: $verifiedDomains")
            logger.log(TAG, "selectedDomains: $selectedDomains")
            logger.log(TAG, "unapprovedDomains: $unapprovedDomains")
            val host = BASE_URL.toUri().host
            if (!verifiedDomains.isNullOrEmpty()) {
                host?.let {
                    if (verifiedDomains.keys.contains(it)) return true
                }
            }
            return false
        }
        return true
    }
}