package space.active.taskmanager1c.presentation.screens.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentAboutBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

private const val TAG = "AboutFragment"

class AboutFragment : BaseFragment(R.layout.fragment_about) {

    lateinit var binding: FragmentAboutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAboutBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        observers()
        listeners()
    }

    private fun listeners() {
        binding.aboutAppContactsET.setOnClickListener {
            logger.log(TAG, "aboutAppContactsET click")
            sendEmailToAuthor(
                emailTo = arrayOf(binding.aboutAppContactsET.text.toString()),
                "${binding.aboutAppNameET.text} Feedback v.${binding.aboutAppVersionET.text}")
        }
        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about_ok -> {
                    findNavController().popBackStack()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun sendEmailToAuthor(emailTo: Array<String>, subject: String) {
        if (emailTo.isNotEmpty() && subject.isNotBlank()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, emailTo)
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            context?.packageManager?.let { packageManager ->
                intent.resolveActivity(packageManager)?.let {
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observers() {
        binding.aboutAppNameET.setText(context?.packageName)
        binding.aboutAppVersionET.setText(context?.packageName?.let {
            context?.packageManager?.getPackageInfo(
                it, 0
            )?.versionName
        })
        binding.aboutAppContactsET.setText("oncharterliz@gmail.com")
    }

    override fun navigateToLogin() {
        //nothing
    }

    override fun successLogin() {
        //nothing
    }


}