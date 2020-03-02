package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.utils.DECIMALS_DIV

fun Double.toProgress() = div(DECIMALS_DIV).toInt()

fun Int.toGrade() = times(DECIMALS_DIV)