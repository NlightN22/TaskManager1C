package space.active.taskmanager1c.presentation.screens.mainactivity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import space.active.taskmanager1c.data.remote.DemoData
import space.active.taskmanager1c.data.utils.GsonParserImpl
import space.active.taskmanager1c.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)


        val data = DemoData().getDemoData(context = this, jsonParser = GsonParserImpl(Gson()))
        Log.e(TAG, "data: $data")
    }
}