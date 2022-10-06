package com.gdavidpb.tuindice.login.ui.customs

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.ui.customs.InputLayout
import com.gdavidpb.tuindice.base.utils.extensions.onTextChanged
import com.gdavidpb.tuindice.login.R

class UsbIdInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int {
		return R.layout.view_usb_id_input
	}

	override fun onViewInflated() {
		editText?.onTextChanged { s, _, before, _ ->
			if (s.length >= 2 && before == 0 && !s.contains("-")) {
				editText?.keyListener = DigitsKeyListener.getInstance("0123456789-")
				editText?.text?.insert(2, "-")
			} else
				editText?.keyListener = DigitsKeyListener.getInstance("0123456789")
		}
	}

	override fun isValid(): Boolean {
		return "${editText?.text}".matches("^\\d{2}-\\d{5}$".toRegex())
	}

	fun getUsbId(): String {
		return "${editText?.text}"
	}
}