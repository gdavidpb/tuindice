package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.animation.core.Easing

val DecelerateEasing = Easing { x -> (1.0f - (1.0f - x) * (1.0f - x)) }