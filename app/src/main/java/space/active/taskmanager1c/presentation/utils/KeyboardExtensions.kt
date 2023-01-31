package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

fun hideKeyboardFrom(context: Context, view: MaterialAutoCompleteTextView) {
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

fun hideKeyboardFrom(context: Context, view: TextInputEditText) {
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}