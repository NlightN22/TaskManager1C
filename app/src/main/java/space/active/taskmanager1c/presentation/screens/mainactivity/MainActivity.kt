package space.active.taskmanager1c.presentation.screens.mainactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.ActivityMainBinding
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var logger: Logger


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
            viewModel.listTasks.collectLatest {
                binding.jobContent.text = it
            }
        }
    }

    private fun listeners() {
        binding.loginButton.setOnClickListener { viewModel.updateJob() }
        binding.logoutButton.setOnClickListener { viewModel.stopUpdateJob() }
    }
}