package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.gdavidpb.tuindice.R

class PasswordInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int {
		return R.layout.view_password_input
	}

	override fun isValid(): Boolean {
		return "${textInputEditText.text}".isNotBlank()
	}

	fun getPassword(): String {
		return "${textInputEditText.text}"
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
}