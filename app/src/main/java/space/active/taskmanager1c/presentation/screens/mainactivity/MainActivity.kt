package space.active.taskmanager1c.presentation.screens.mainactivity

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.ActivityMainBinding
import space.active.taskmanager1c.domain.use_case.HandleDeepLink
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.LOGIN_SUCCESSFUL
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    @Inject
    lateinit var handleDeepLink: HandleDeepLink

    private val navController by lazy { findNavController(R.id.fragmentContainerView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listeners()
        observers()
    }

    override fun onStart() {
        super.onStart()
        handleDeepLinkOnStart()
    }

    private fun handleDeepLinkOnStart() {
        logger.log(TAG, "OnStart: $intent  with activity: $this")
        /**
        Флаг 0x13000000 в Intent является комбинацией следующих флагов:
        FLAG_ACTIVITY_NEW_TASK (0x10000000) - Запуск новой задачи для целевой активности. Если этот флаг не установлен, активность будет запущена в контексте задачи, которая уже существует, если таковая имеется.
        FLAG_ACTIVITY_SINGLE_TOP (0x20000000) - Если активность уже запущена в вершине стека, то не создавайте новый экземпляр активности, а вместо этого передайте ей новый Intent, содержащий последние изменения.
        FLAG_ACTIVITY_NO_ANIMATION (0x4000000) - Отключение стандартной анимации для этой операции старта активности.
         */
        if ((intent.flags == 0x13000000
                    || intent.flags == FLAG_ACTIVITY_NEW_TASK) && intent.action == Intent.ACTION_VIEW) {
            handleDeepLink(navController, intent)
            intent.data = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        logger.log(TAG, "onNewIntent: $intent with activity: $this")
        intent?.let {
            handleDeepLink(navController, it)
            intent.data = null
        }
    }

    private fun observers() {
    }

    private fun listeners() {
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        logger.log(TAG, "onBackPressed")
        val backDestination =
            navController.previousBackStackEntry?.destination?.id
        val currentDestination = navController.currentDestination
        logger.log(
            TAG,
            "currentFragment ID: ${currentDestination?.id} login ID: ${R.id.loginFragment}"
        )
        if (currentDestination!!.id == R.id.loginFragment) {
            handleLoginBack(navController)
        } else {
            if (backDestination == null) {
                exitWithTimer()
            } else {
                super.onBackPressed()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleLoginBack(navController: NavController) {
        val previousState = navController.previousBackStackEntry?.savedStateHandle
        val loginState = previousState?.get<Boolean>(LOGIN_SUCCESSFUL)
        logger.log(TAG, "previousState loginState: ${loginState}")
        loginState?.let {
            if (it) {
                super.onBackPressed()
                return
            }
        }
        exitWithTimer()
    }

    private var lastPress: Long = 0
    private fun exitWithTimer() {
        val currentTime: Long = System.currentTimeMillis()
        val delay: Int = 2000
        if (currentTime - lastPress > delay) {
            val message = getString(R.string.exit_msg)

            val currentFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView)
                ?.childFragmentManager
                ?.fragments
                ?.firstOrNull()

            if (currentFragment is BaseFragment) {
                logger.log(TAG, "BaseFragment")
                currentFragment.showSnackBar(UiText.Dynamic(message))
            } else {
                logger.log(TAG, "not BaseFragment")
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
            }

            lastPress = System.currentTimeMillis()
        } else {
            this.finishAffinity()
        }
    }
}