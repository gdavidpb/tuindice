package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs

object CourseCodeColorGenerator {
	data class CourseColors(
		val color: Color,
		val containerColor: Color
	)

	private val twoLettersPattern = Regex("^([A-Z]{2})(\\d{4})$")
	private val threeLettersPattern = Regex("^([A-Z]{3})(\\d{3})$")

	fun fromCode(code: String): CourseColors {
		val normalized = code.uppercase().trim()
		val match = twoLettersPattern.matchEntire(normalized)
			?: threeLettersPattern.matchEntire(normalized)

		val codeColor = if (match != null) {
			val letters = match.groupValues[1]
			val digits = match.groupValues[2]

			val hue = lettersToHue(letters)
			val saturation = digitsToSaturation(digits)
			val lightness = digitsToLightness(digits)

			hslToColor(hue, saturation, lightness)
		} else {
			Color.Black
		}

		return CourseColors(
			color = codeColor.brightness(amount = -0.25f),
			containerColor = codeColor.brightness(amount = 0.50f)
		)
	}

	private fun Color.brightness(amount: Float): Color {
		val clamped = amount.coerceIn(-1f, 1f)

		return when {
			clamped < 0f -> lerp(this, Color.Black, -clamped)
			clamped > 0f -> lerp(this, Color.White, clamped)
			else -> this
		}
	}

	private fun lettersToHue(letters: String): Float {
		val value = letters.fold(0) { accumulator, letter ->
			(accumulator * 26) + (letter.code - 'A'.code)
		}
		return (value % 360).toFloat()
	}

	private fun digitsToSaturation(digits: String): Float {
		val number = digits.toIntOrNull() ?: 0
		return 0.60f + (number % 16) / 100f
	}

	private fun digitsToLightness(digits: String): Float {
		val number = digits.toIntOrNull() ?: 0
		return 0.48f + (number % 10) / 100f
	}

	private fun hslToColor(h: Float, s: Float, l: Float): Color {
		val c = (1f - abs(2 * l - 1f)) * s
		val x = c * (1f - abs((h / 60f) % 2 - 1f))
		val m = l - c / 2f

		val (r1, g1, b1) = when {
			h < 60f -> Triple(c, x, 0f)
			h < 120f -> Triple(x, c, 0f)
			h < 180f -> Triple(0f, c, x)
			h < 240f -> Triple(0f, x, c)
			h < 300f -> Triple(x, 0f, c)
			else -> Triple(c, 0f, x)
		}

		return Color(r1 + m, g1 + m, b1 + m, 1f)
	}
}
