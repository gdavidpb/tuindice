package com.gdavidpb.tuindice.ui.customs

import android.graphics.Canvas

interface ViewHook {
    fun onDrawHook(canvas: Canvas, superOnDraw: (Canvas) -> Unit)
}