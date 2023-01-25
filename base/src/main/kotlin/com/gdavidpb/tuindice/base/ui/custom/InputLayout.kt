package com.gdavidpb.tuindice.base.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.animateLookAtMe
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.base.utils.extension.onTextChanged
import com.google.android.material.textfield.TextInputLayout

abstract class InputLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

	private val primaryColor by lazy {
		context.getCompatColor(R.color.color_primary)
	}

	private val secondaryColor by lazy {
		context.getCompatColor(R.color.color_secondary_text)
	}

	private val primaryTint = ColorStateList.valueOf(primaryColor)
	private val secondaryTint = ColorStateList.valueOf(secondaryColor)

	abstract fun isValid(): Boolean

	protected abstract val textInputLayout: TextInputLayout

	@LayoutRes
	abstract fun onInflateView(): Int
	open fun onViewInflated() {}

	override fun onFinishInflate() {
		super.onFinishInflate()

		inflate(context, onInflateView(), this)

		with(textInputLayout) {
			editText?.onTextChanged { _, _, _, _ ->
				if (error != null) {
					error = null
					isErrorEnabled = false
				}

				val isValid = isValid()

				setStartIconTintList(if (isValid) primaryTint else secondaryTint)
			}
		}

		onViewInflated()
	}

	fun isEmpty(): Boolean {
		return textInputLayout.editText?.text.isNullOrBlank()
	}

	fun setError(@StringRes stringRes: Int, visualAlert: Boolean = true) {
		with(textInputLayout) {
			isErrorEnabled = true
			error = context.getString(stringRes)
		}

		if (visualAlert) {
			requestFocus()

			animateLookAtMe()
		}
	}
}