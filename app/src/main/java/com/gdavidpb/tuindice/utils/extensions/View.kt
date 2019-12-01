package com.gdavidpb.tuindice.utils.extensions

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.utils.STATE_COLLAPSED
import com.gdavidpb.tuindice.utils.STATE_EXPANDED
import com.gdavidpb.tuindice.utils.STATE_IDLE
import com.gdavidpb.tuindice.utils.TIME_DELAY_CLICK_ONCE
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import kotlin.math.absoluteValue

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

fun SeekBar.onSeekBarChange(listener: (progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            listener(progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    })
}

fun AppBarLayout.onStateChanged(listener: (newState: Int) -> Unit) {
    var mCurrentState = STATE_IDLE

    addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        when {
            verticalOffset == 0 -> {
                if (mCurrentState != STATE_EXPANDED)
                    listener(STATE_EXPANDED)

                mCurrentState = STATE_EXPANDED
            }
            verticalOffset.absoluteValue >= appBarLayout.totalScrollRange -> {
                if (mCurrentState != STATE_COLLAPSED)
                    listener(STATE_COLLAPSED)

                mCurrentState = STATE_COLLAPSED
            }
            else -> {
                if (mCurrentState != STATE_IDLE)
                    listener(STATE_IDLE)

                mCurrentState = STATE_IDLE
            }
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
        left: Drawable? = null,
        top: Drawable? = null,
        right: Drawable? = null,
        bottom: Drawable? = null) = setCompoundDrawables(left, top, right, bottom)

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