package space.active.taskmanager1c.presentation.screens.mainactivity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.ActivityMainBinding
import space.active.taskmanager1c.presentation.screens.LOGIN_SUCCESSFUL
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var logger: Logger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listeners()
        observers()

    }

    private fun observers() {

    }

    private fun listeners() {
    }

    override fun onBackPressed() {
        logger.log(TAG, "onBackPressed")
        val navController = Navigation.findNavController(binding.fragmentContainerView)
        val backDestination =
            navController.previousBackStackEntry?.destination?.id
        val currentDestination = navController.currentDestination
        logger.log(
            TAG,
            "currentFragment: ${currentDestination?.id} login ID: ${R.id.loginFragment}"
        )
//        logger.log(TAG, "loginState: ${viewModel.loginState.get()}")
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
        val delay: Long = 2000
        if (currentTime - lastPress > delay) {
            Toast.makeText(this, getString(R.string.exit_toast), delay.toInt()).show()
            lastPress = System.currentTimeMillis()
        } else {
            this.finishAffinity()
        }
    }
}