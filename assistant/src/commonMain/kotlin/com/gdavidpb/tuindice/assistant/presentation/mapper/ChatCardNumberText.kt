package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.utils.extension.formatGrade

private const val INDEX_DECIMALS = 4

// An index as a chat card writes it: four decimals and the decimal point, as the rest of the app, «3.8125».
fun Double.toCardIndexText(): String = toCardDecimalText(INDEX_DECIMALS)

// A number with [decimals] decimals and the decimal point, from the app's own formatter. As the app does, it
// cuts the digits past [decimals] instead of rounding them.
fun Double.toCardDecimalText(decimals: Int): String = formatGrade(decimals)
