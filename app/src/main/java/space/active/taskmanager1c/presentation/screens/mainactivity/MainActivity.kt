package space.active.taskmanager1c.presentation.screens.mainactivity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.ActivityMainBinding
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


//        lifecycleScope.launchWhenStarted {
//            viewModel.listTasks.collectLatest {
//                binding.jobContent.text = it
//            }
//        }
//        lifecycleScope.launchWhenStarted {
//            viewModel.testCaseText.collectLatest {
//                binding.testCaseText.text = it
//            }
//        }
    }

    private fun listeners() {
//        binding.loginButton.setOnClickListener { viewModel.updateJob() }
//        binding.logoutButton.setOnClickListener { viewModel.stopUpdateJob() }
//
//        // "3bb37cb5-a9a6-11e7-9d3f-00155d28010b"
//        val taskId = "3bb37cb5-a9a6-11e7-9d3f-00155d28010b"
//        binding.readTask.setOnClickListener { viewModel.readTask(taskId) }
//        binding.editButton.setOnClickListener { viewModel.editTask() }
//        binding.saveNewButton.setOnClickListener { viewModel.newTask() }
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