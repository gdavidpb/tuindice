package com.gdavidpb.tuindice.base.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.view
import com.google.android.material.textfield.TextInputLayout

class PasswordInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int = R.layout.view_password_input

	override val textInputLayout: TextInputLayout by view(R.id.textInputLayout)

	override fun isValid(): Boolean {
		return !textInputLayout.editText?.text.isNullOrBlank()
	}

	fun getPassword(): String {
		return "${textInputLayout.editText?.text}"
	}

	fun setAction(action: () -> Unit) {
		textInputLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				action()
				false
			} else
				true
		}
	}
}