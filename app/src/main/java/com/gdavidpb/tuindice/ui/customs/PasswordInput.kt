package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.onTextChanged
import kotlinx.android.synthetic.main.view_usb_id_input.view.*

class PasswordInput(context: Context, attrs: AttributeSet)
    : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_password_input, this)

        initListeners()
    }

    fun isBlank() = "${textInputEditText.text}".isBlank()

    fun getPassword() = "${textInputEditText.text}"

    fun setError(@StringRes resource: Int) {
        textInputLayout.error = context.getString(resource)
    }

    fun setAction(action: () -> Unit) {
        textInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                action()
                false
            } else
                true
        }
    }

    private fun initListeners() {
        textInputEditText.onTextChanged { _, _, _, _ ->
            if (textInputLayout.error != null)
                textInputLayout.error = null
        }
    }
}