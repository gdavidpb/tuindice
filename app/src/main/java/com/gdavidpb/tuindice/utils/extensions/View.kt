package com.gdavidpb.tuindice.utils.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.ui.customs.EvaluationDatePicker
import com.gdavidpb.tuindice.utils.NO_GETTER
import com.gdavidpb.tuindice.utils.TIME_DELAY_CLICK_ONCE
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
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
    setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            view.setOnClickListener(null)

            also { listener ->
                CoroutineScope(Dispatchers.Main).launch {
                    onClick()

                    withContext(Dispatchers.IO) { delay(TIME_DELAY_CLICK_ONCE) }

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

fun RecyclerView.onScrollStateChanged(listener: (newState: Int) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            listener(newState)
        }
    })
}

fun TextInputLayout.selectAll() = editText?.selectAll()

fun TextView.drawables(
        start: Drawable? = null,
        top: Drawable? = null,
        end: Drawable? = null,
        bottom: Drawable? = null) = setCompoundDrawables(start, top, end, bottom)

fun TextView.drawables(
        start: Int = 0,
        top: Int = 0,
        end: Int = 0,
        bottom: Int = 0) = setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)

fun TextView.wrapEvaluationDatePicker() = EvaluationDatePicker(this)

fun EditText.onTextChanged(event: (CharSequence, Int, Int, Int) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            event(s ?: "", start, before, count)
        }

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    })
}

fun InputMethodManager.showSoftKeyboard(view: View) {
    if (view.requestFocus()) showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun InputMethodManager.hideSoftKeyboard(view: View) {
    view.clearFocus()
    hideSoftInputFromWindow(view.windowToken, 0)
}


fun TextInputLayout.text(value: String? = null) = value?.also { editText?.setText(it) }
        ?: "${editText?.text}"

fun TextView.strikeThrough() {
    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

fun TextView.clearStrikeThrough() {
    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleIf(value: Boolean, elseValue: Int = View.GONE) {
    visibility = if (value) View.VISIBLE else elseValue
}

var View.backgroundColor: Int
    @Deprecated(message = NO_GETTER, level = ERROR) get() = throw NotImplementedError()
    set(value) {
        setBackgroundColor(value)
    }

var ChipGroup.checkedChipIndex: Int
    get() = indexOfChild(findViewById<Chip>(checkedChipId))
    set(value) {
        check(getChildAt(value).id)
    }