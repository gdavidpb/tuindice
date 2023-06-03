package com.gdavidpb.tuindice.base.utils.extension

import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StyleableRes
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.base.utils.NO_GETTER
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.DeprecationLevel.ERROR

data class SeekBarChangeListenerBuilder(
	var onProgressChanged: (progress: Int, fromUser: Boolean) -> Unit = { _, _ -> },
	var onStopTrackingTouch: (progress: Int) -> Unit = { _ -> }
) {
	fun onProgressChanged(listener: (progress: Int, fromUser: Boolean) -> Unit) {
		onProgressChanged = listener
	}

	fun onStopTrackingTouch(listener: (progress: Int) -> Unit) {
		onStopTrackingTouch = listener
	}
}

fun CompoundButton.onCheckedChange(listener: (isChecked: Boolean) -> Unit) {
	setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
}

fun View.onClickOnce(onClick: () -> Unit) {
	setOnClickListener(object : OnClickListener {
		override fun onClick(view: View) {
			view.setOnClickListener(null)

			also { listener ->
				CoroutineScope(Dispatchers.Main).launch {
					onClick()

					withContext(Dispatchers.IO) { delay(500L) }

					view.setOnClickListener(listener)
				}
			}
		}
	})
}

fun SeekBar.onSeekBarChange(builder: SeekBarChangeListenerBuilder.() -> Unit) {
	val built = SeekBarChangeListenerBuilder().apply(builder)

	setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
		override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
			built.onProgressChanged(progress, fromUser)
		}

		override fun onStartTrackingTouch(seekBar: SeekBar) {
		}

		override fun onStopTrackingTouch(seekBar: SeekBar) {
			built.onStopTrackingTouch(seekBar.progress)
		}
	})
}

fun View.hideSoftKeyboard() {
	clearFocus()
	context
		.getSystemService<InputMethodManager>()
		?.hideSoftInputFromWindow(windowToken, 0)
}

fun TextView.drawables(
	start: Drawable? = null,
	top: Drawable? = null,
	end: Drawable? = null,
	bottom: Drawable? = null
) = setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)

fun EditText.onTextChanged(event: (CharSequence, Int, Int, Int) -> Unit) {
	addTextChangedListener(object : TextWatcher {
		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			event(s ?: "", start, before, count)
		}

		override fun afterTextChanged(s: Editable?) {}

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
	})
}

fun TextInputLayout.text(value: String? = null) = value?.also { editText?.setText(it) }
	?: "${editText?.text}"

fun TextView.strikeThrough() {
	paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

fun TextView.clearStrikeThrough() {
	paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

fun View.loadAttributes(
	@StyleableRes
	styleId: IntArray,
	attrs: AttributeSet
): TypedArray = context.theme.obtainStyledAttributes(attrs, styleId, 0, 0)

var View.backgroundColor: Int
	@Deprecated(message = NO_GETTER, level = ERROR) get() = throw NotImplementedError()
	set(value) {
		setBackgroundColor(value)
	}

var ChipGroup.checkedChipIndex: Int
	get() = if (checkedChipId != -1) indexOfChild(findViewById<Chip>(checkedChipId)) else -1
	set(value) {
		check(getChildAt(value).id)
	}