package space.active.taskmanager1c.presentation.screens.mainactivity

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.CryptoManager
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.ActivityMainBinding
import space.active.taskmanager1c.domain.models.UserSettingsSerializer
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var logger: Logger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        listeners()
        observers()

    }

    private fun observers() {
        lifecycleScope.launchWhenStarted {
            viewModel.exitEvent.collectLatest {
                if (it) {finishAffinity()}
            }
        }

    }

    private fun listeners() {

    }

    private var lastPress: Long = 0
    override fun onBackPressed() {
        val navController = Navigation.findNavController(binding.fragmentContainerView)
        val backDestination =
            navController.previousBackStackEntry?.destination?.id
//        logger.log(TAG,"backDestination $backDestination currentDestination $currentDestination") // todo delete
        if (backDestination == null) {
            val currentTime: Long = System.currentTimeMillis()
            val delay: Long = 2000
            if (currentTime - lastPress > delay) {
                Toast.makeText(this, getString(R.string.exit_toast), delay.toInt()).show()
                lastPress = System.currentTimeMillis()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}