package space.active.taskmanager1c.presentation.screens.mainactivity

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            logger.log(TAG, "OnBackPressedDispatcher callback fired")
            handleBackPressed()
        }
    }

    private var backInvokedCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val originalTopPadding = binding.fragmentContainerView.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = originalTopPadding + bars.top)
            insets
        }

        setupBackPressHandler()
        listeners()
        observers()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backInvokedCallback?.let {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it)
            }
            backInvokedCallback = null
        }
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        handleDeepLinkOnStart()
    }

    private fun setupBackPressHandler() {
        logger.log(TAG, "setupBackPressHandler register dispatcher callback")
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = OnBackInvokedCallback {
                logger.log(TAG, "OnBackInvokedCallback fired")
                handleBackPressed()
            }
            backInvokedCallback = callback

            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
        }
    }

    private fun handleBackPressed() {
        logger.log(TAG, "handleBackPressed")

        val backDestination = navController.previousBackStackEntry?.destination?.id
        val currentDestination = navController.currentDestination

        logger.log(
            TAG,
            "currentFragment ID: ${currentDestination?.id} login ID: ${R.id.loginFragment}"
        )

        if (currentDestination?.id == R.id.loginFragment) {
            handleLoginBack(navController)
        } else {
            if (backDestination == null) {
                exitWithTimer()
            } else {
                navController.navigateUp()
            }
        }
    }

    private fun handleDeepLinkOnStart() {
        logger.log(TAG, "OnStart: $intent with activity: $this")

        if ((intent.flags == 0x13000000 || intent.flags == FLAG_ACTIVITY_NEW_TASK)
            && intent.action == Intent.ACTION_VIEW
        ) {
            handleDeepLink(navController, intent)
            intent.data = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        logger.log(TAG, "onNewIntent: $intent with activity: $this")
        intent?.let {
            handleDeepLink(navController, it)
            it.data = null
        }
    }

    private fun observers() {
    }

    private fun listeners() {
    }

    private fun handleLoginBack(navController: NavController) {
        val previousState = navController.previousBackStackEntry?.savedStateHandle
        val loginState = previousState?.get<Boolean>(LOGIN_SUCCESSFUL)

        logger.log(TAG, "previousState loginState: $loginState")

        if (loginState == true) {
            navController.navigateUp()
            return
        }

        exitWithTimer()
    }

    private var lastPress: Long = 0

    private fun exitWithTimer() {
        val currentTime = System.currentTimeMillis()
        val delay = 2000

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
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                    .show()
            }

            lastPress = currentTime
        } else {
            finishAffinity()
        }
    }
}