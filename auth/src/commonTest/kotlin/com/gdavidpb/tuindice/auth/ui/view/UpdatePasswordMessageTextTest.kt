package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdatePasswordMessageTextTest {
	@Test
	fun withBoldName_whenTheMessageNamesTheApp_boldsOnlyThatName() {
		val text = "Debes actualizar la clave de TuIndice ahora".withBoldName(name = "TuIndice")

		val span = text.spanStyles.single()

		assertEquals("TuIndice", text.text.substring(span.start, span.end))
		assertEquals(FontWeight.Bold, span.item.fontWeight)
	}

	// The wording of the message changed to one that never names the app; the span used to start
	// at -1 and crash the text layout on Android the moment the dialog opened.
	@Test
	fun withBoldName_whenTheMessageDoesNotNameTheApp_addsNoSpanAndKeepsTheText() {
		val message = "Necesitamos tu contraseña USB para seguir sincronizando tus datos."

		val text = message.withBoldName(name = "TuIndice")

		assertEquals(message, text.text)
		assertTrue(text.spanStyles.isEmpty())
	}
}
