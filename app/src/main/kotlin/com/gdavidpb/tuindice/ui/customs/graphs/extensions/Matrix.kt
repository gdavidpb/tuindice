package com.gdavidpb.tuindice.ui.customs.graphs.extensions

import android.graphics.Matrix
import androidx.core.graphics.values

val Matrix.scaleX: Float
    get() = values()[Matrix.MSCALE_X]

val Matrix.scaleY: Float
    get() = values()[Matrix.MSCALE_Y]

val Matrix.translateX: Float
    get() = values()[Matrix.MTRANS_X]

val Matrix.translateY: Float
    get() = values()[Matrix.MTRANS_Y]