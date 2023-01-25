package com.gdavidpb.tuindice.base.utils.extension

import android.graphics.Canvas
import android.graphics.Path
import android.os.Build

@Suppress("DEPRECATION")
fun Canvas.supportQuickReject(path: Path): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        quickReject(path)
    else
        quickReject(path, Canvas.EdgeType.AA)
}