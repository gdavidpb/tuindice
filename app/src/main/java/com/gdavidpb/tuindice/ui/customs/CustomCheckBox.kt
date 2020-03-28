package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.utils.mappers.distanceTo

class CustomCheckBox(context: Context, attrs: AttributeSet)
    : AppCompatCheckBox(context, attrs) {

    companion object {
        private const val backgroundColor = Color.WHITE

        private var onDrawLocker = false

        private lateinit var checkedColor: ColorStateList
        private lateinit var uncheckedColor: ColorStateList
        private lateinit var disabledColor: ColorStateList
    }

    override fun onDraw(canvas: Canvas) {
        if (!onDrawLocker) {
            onDrawLocker = true

            val bitmapHook = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvasHook = Canvas(bitmapHook)
            val isCheckedHook = isChecked

            canvasHook.drawColor(backgroundColor)

            isChecked = false

            super.onDraw(canvasHook)

            isChecked = isCheckedHook

            val x = width / 2

            uncheckedColor = (0 until height)
                    .map { y -> bitmapHook.getPixel(x, y) }
                    .distinct()
                    .maxBy { target -> target distanceTo backgroundColor }
                    .let { selectedColor ->
                        ColorStateList.valueOf(selectedColor ?: backgroundColor)
                    }

            checkedColor = ColorStateList.valueOf(context.getCompatColor(R.color.color_retired))
            disabledColor = ColorStateList.valueOf(context.getCompatColor(R.color.color_disabled))
        }

        buttonTintList = when {
            !isEnabled -> disabledColor
            isChecked -> checkedColor
            else -> uncheckedColor
        }

        super.onDraw(canvas)
    }
}