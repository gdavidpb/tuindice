package com.gdavidpb.tuindice.base.ui.customs

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.onTextChanged
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_usb_id_input.view.textInputLayout as input

class UsbIdInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int = R.layout.view_usb_id_input

	override val textInputLayout: TextInputLayout by lazy { input }

	override fun onViewInflated() {
		textInputLayout.editText?.apply {
			onTextChanged { s, _, before, _ ->
				if (s.length >= 2 && before == 0 && !s.contains("-")) {
					keyListener = DigitsKeyListener.getInstance("0123456789-")
					text.insert(2, "-")
				} else
					keyListener = DigitsKeyListener.getInstance("0123456789")
			}
		}

	}

	override fun isValid(): Boolean {
		return "${textInputLayout.editText?.text}".matches("^\\d{2}-\\d{5}$".toRegex())
	}


	fun getUsbId(): String {
		return "${textInputLayout.editText?.text}"
	}
}