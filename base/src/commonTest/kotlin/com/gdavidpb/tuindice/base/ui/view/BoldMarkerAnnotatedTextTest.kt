package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.utils.extension.withoutBoldMarkers
import kotlin.test.Test
import kotlin.test.assertEquals

class BoldMarkerAnnotatedTextTest {
	@Test
	fun toBoldMarkerAnnotatedText_removesMarkersAndStylesMarkedText() {
		val text = "Ayudar a mejorar **TuIndice** compartiendo datos"

		val annotatedText = text.toBoldMarkerAnnotatedText()

		assertEquals("Ayudar a mejorar TuIndice compartiendo datos", annotatedText.text)
		assertEquals(1, annotatedText.spanStyles.size)
		assertEquals(17, annotatedText.spanStyles.first().start)
		assertEquals(25, annotatedText.spanStyles.first().end)
		assertEquals(FontWeight.SemiBold, annotatedText.spanStyles.first().item.fontWeight)
	}

	@Test
	fun multipleBoldSpans_keepTheirOwnRangesAndRequestedWeight() {
		val annotatedText = "Notas: **16,2** y **18**.".toBoldMarkerAnnotatedText(FontWeight.Bold)

		assertEquals("Notas: 16,2 y 18.", annotatedText.text)
		assertEquals(
			listOf("16,2", "18"),
			annotatedText.spanStyles.map { span -> annotatedText.text.substring(span.start, span.end) }
		)
		assertEquals(listOf(FontWeight.Bold, FontWeight.Bold), annotatedText.spanStyles.map { it.item.fontWeight })
	}

	@Test
	fun anUnmatchedMarker_isPreservedAsText() {
		val annotatedText = "**Bien** y **sin cerrar".toBoldMarkerAnnotatedText()

		assertEquals("Bien y **sin cerrar", annotatedText.text)
		assertEquals(1, annotatedText.spanStyles.size)
		assertEquals("Bien", annotatedText.text.substring(annotatedText.spanStyles[0].start, annotatedText.spanStyles[0].end))
	}

	@Test
	fun withoutBoldMarkers_readsAsTheAnnotatedText() {
		val texts = listOf("**TuIndice** ya **está**", "sin **cerrar", "****", "", "**", "a ** b ** c **")

		texts.forEach { text ->
			assertEquals(text.toBoldMarkerAnnotatedText().text, text.withoutBoldMarkers())
		}
	}
}
