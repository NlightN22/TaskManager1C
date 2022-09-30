package space.active.taskmanager1c.presentation.screens.mainactivity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TaskWithUsersDatabase
import space.active.taskmanager1c.data.remote.DemoData
import space.active.taskmanager1c.data.utils.GsonParserImpl
import space.active.taskmanager1c.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

//        lifecycleScope.launchWhenStarted {
//            viewModel.taskList.collectLatest {
//                Log.e(TAG, "$it")
//            }
//        }

    }
}