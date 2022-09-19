package space.active.taskmanager1c.presentation.screens.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import space.active.taskmanager1c.R
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


    }
}