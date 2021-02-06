package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.onTextChanged
import kotlinx.android.synthetic.main.view_usb_id_input.view.*

class UsbIdInput(context: Context, attrs: AttributeSet)
    : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_usb_id_input, this)

        initEvents()
    }

    fun isBlank() = "${textInputEditText.text}".isBlank()

    fun isValid() = "${textInputEditText.text}".isUsbId()

    fun getUsbId() = "${textInputEditText.text}"

    fun setError(@StringRes resource: Int) {
        textInputLayout.error = context.getString(resource)
    }

    private fun String.isUsbId() = matches("^\\d{2}-\\d{5}$".toRegex())

    private fun initEvents() {
        textInputEditText.onTextChanged { s, _, before, _ ->
            if (textInputLayout.error != null)
                textInputLayout.error = null

            if (s.length >= 2 && before == 0 && !s.contains("-")) {
                textInputEditText.keyListener = DigitsKeyListener.getInstance("0123456789-")
                textInputEditText.text?.insert(2, "-")
            } else
                textInputEditText.keyListener = DigitsKeyListener.getInstance("0123456789")
        }
    }
}