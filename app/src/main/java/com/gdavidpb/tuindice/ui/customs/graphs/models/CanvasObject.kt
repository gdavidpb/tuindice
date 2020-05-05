package com.gdavidpb.tuindice.ui.customs.graphs.models

import android.graphics.Paint
import android.graphics.Path

class CanvasObject(
        val path: Path,
        val paint: Paint,
        val isClickable: Boolean
)