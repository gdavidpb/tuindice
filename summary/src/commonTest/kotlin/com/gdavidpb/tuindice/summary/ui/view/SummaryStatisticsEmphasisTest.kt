package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SummaryStatisticsEmphasisTest {
	private val style = SpanStyle(fontWeight = FontWeight.Medium)

	@Test
	fun when_theEmphasisIsInTheText_then_onlyItsFirstOccurrenceIsSetApart() {
		val text = "A este ritmo te faltan unos 4 trimestres para los 205 créditos."
		val start = text.indexOf("4 trimestres")

		val emphasized = emphasizedText(text = text, emphasis = "4 trimestres", emphasisStyle = style)

		assertEquals(text, emphasized.text)
		assertEquals(listOf(AnnotatedString.Range(style, start, start + "4 trimestres".length)), emphasized.spanStyles)
	}

	@Test
	fun when_theEmphasisIsNotInTheText_then_theTextStandsWhole() {
		val emphasized = emphasizedText(text = "Sin proyección.", emphasis = "4 trimestres", emphasisStyle = style)

		assertEquals("Sin proyección.", emphasized.text)
		assertTrue(emphasized.spanStyles.isEmpty())
	}
}
