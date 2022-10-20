package com.gdavidpb.tuindice.base.ui.customs

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.gdavidpb.tuindice.base.R

class PasswordInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int {
		return R.layout.view_password_input
	}

	override fun isValid(): Boolean {
		return editText?.text.isNullOrBlank()
	}

	fun getPassword(): String {
		return "${editText?.text}"
	}

	fun setAction(action: () -> Unit) {
		editText?.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				action()
				false
			} else
				true
		}
	}
}