package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.text.font.FontWeight
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
}
