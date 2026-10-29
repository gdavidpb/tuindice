package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_percent
import kotlin.math.roundToInt

private const val PERCENT = 100
private const val PERCENT_SIGN = "%"

// A percentage as a card writes it: «47 %». The sign is an argument, so the format holds no literal percent sign.
fun chatCardPercentText(percent: Int): UiText =
	UiText.Resource(Res.string.assistant_card_b_percent, listOf(percent, PERCENT_SIGN))

// A part of a whole as a whole percentage, rounded to the nearest; 0 when the whole is empty.
fun chatCardPercentOf(part: Int, whole: Int): Int =
	if (whole == 0) 0 else (part * PERCENT.toDouble() / whole).roundToInt()

// A fraction of the whole (0.72) as a whole percentage (72).
fun Double.toCardPercent(): Int = (this * PERCENT).roundToInt()

// The part of a whole as the fraction a bar fills: 0 when the whole is empty, and never past the whole.
fun chatCardFraction(part: Double, whole: Double): Float =
	if (whole <= 0.0) 0f else (part / whole).toFloat().coerceIn(0f, 1f)
