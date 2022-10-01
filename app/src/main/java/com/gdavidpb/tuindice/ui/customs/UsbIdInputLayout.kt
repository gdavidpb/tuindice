package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.extensions.onTextChanged

class UsbIdInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int {
		return R.layout.view_usb_id_input
	}

	override fun onViewInflated() {
		textInputEditText.onTextChanged { s, _, before, _ ->
			if (s.length >= 2 && before == 0 && !s.contains("-")) {
				textInputEditText.keyListener = DigitsKeyListener.getInstance("0123456789-")
				textInputEditText.text?.insert(2, "-")
			} else
				textInputEditText.keyListener = DigitsKeyListener.getInstance("0123456789")
		}
	}

	override fun isValid(): Boolean {
		return "${textInputEditText.text}".matches("^\\d{2}-\\d{5}$".toRegex())
	}

	fun getUsbId(): String {
		return "${textInputEditText.text}"
	}
}