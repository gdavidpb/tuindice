package com.gdavidpb.tuindice.base.ui.custom

import android.graphics.Canvas

interface ViewHook {
	fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit)
}