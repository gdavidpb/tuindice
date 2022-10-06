package com.gdavidpb.tuindice.base.ui.customs

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.base.utils.extensions.onTextChanged
import com.google.android.material.textfield.TextInputLayout

abstract class InputLayout(context: Context, attrs: AttributeSet) :
	TextInputLayout(context, attrs) {

	private val primaryColor = context.getCompatColor(R.color.color_primary)
	private val secondaryColor = context.getCompatColor(R.color.color_secondary_text)

	private val primaryTint = ColorStateList.valueOf(primaryColor)
	private val secondaryTint = ColorStateList.valueOf(secondaryColor)

	@LayoutRes
	abstract fun onInflateView(): Int
	abstract fun isValid(): Boolean

	open fun onViewInflated() {}

	override fun onFinishInflate() {
		super.onFinishInflate()

		inflate(context, onInflateView(), this)

		editText?.onTextChanged { _, _, _, _ ->
			if (error != null) {
				error = null
				isErrorEnabled = false
			}

			val isValid = isValid()

			setStartIconTintList(if (isValid) primaryTint else secondaryTint)
		}

		onViewInflated()
	}

	fun isEmpty(): Boolean {
		return editText?.text.isNullOrBlank()
	}

	fun setError(resource: Int) {
		isErrorEnabled = true
		error = context.getString(resource)
	}
}