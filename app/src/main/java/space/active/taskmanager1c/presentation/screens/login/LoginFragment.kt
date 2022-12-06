package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

    }



}