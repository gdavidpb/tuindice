package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.extensions.getCompatColor
import com.google.android.material.checkbox.MaterialCheckBox
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.sqrt

class TintedCheckBox(context: Context, attrs: AttributeSet)
    : MaterialCheckBox(context, attrs), ViewHook {

    companion object {
        private const val backgroundColor = Color.WHITE

        private val onDrawLocker = AtomicBoolean(false)

        private lateinit var checkedColor: ColorStateList
        private lateinit var uncheckedColor: ColorStateList
    }

    private var checkedChangeListener: OnCheckedChangeListener? = null

    override fun onDraw(canvas: Canvas) {
        onDrawHook(canvas) { canvasHook -> super.onDraw(canvasHook) }

        buttonTintList = if (isChecked)
            checkedColor
        else
            uncheckedColor

        super.onDraw(canvas)
    }

    override fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit) {
        if (!onDrawLocker.compareAndSet(false, true)) return

        val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvasHook = Canvas(bitmapHook)
        val isCheckedHook = isChecked

        canvasHook.drawColor(backgroundColor)

        isChecked = false

        superOnDraw(canvasHook)

        isChecked = isCheckedHook

        val x = width / 2

        uncheckedColor = (0 until height)
                .map { y -> bitmapHook.getPixel(x, y) }
                .distinct()
                .maxByOrNull { target -> target distanceTo backgroundColor }
                .let { selectedColor ->
                    ColorStateList.valueOf(selectedColor ?: backgroundColor)
                }

        checkedColor = ColorStateList.valueOf(context.getCompatColor(R.color.color_retired))

        bitmapHook.recycle()
    }

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        checkedChangeListener = listener
        super.setOnCheckedChangeListener(listener)
    }

    fun setChecked(checked: Boolean, notify: Boolean) {
        if (notify) {
            isChecked = checked
        } else {
            super.setOnCheckedChangeListener(null)
            isChecked = checked
            super.setOnCheckedChangeListener(checkedChangeListener)
        }
    }

    private infix fun Int.distanceTo(x: Int): Double {
        val (a, b, c) = arrayOf(
                (Color.red(x) - Color.red(this)).toDouble(),
                (Color.green(x) - Color.green(this)).toDouble(),
                (Color.blue(x) - Color.blue(this)).toDouble()
        )

        return sqrt(a.pow(2.0) + b.pow(2.0) + c.pow(2.0))
    }
}