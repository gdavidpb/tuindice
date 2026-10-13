package com.gdavidpb.tuindice.base.utils.extension

import kotlin.test.Test
import kotlin.test.assertEquals

class SentenceCaseTest {
	@Test
	fun toSentenceCase_lowercasesTheWords_andCapitalizesTheFirstOne() {
		assertEquals("Choque de horario", "CHOQUE DE HORARIO".toSentenceCase())
	}

	@Test
	fun toSentenceCase_keepsSubjectCodesAndAcronymsInCapitals() {
		assertEquals("Choque de horario con MA1111", "CHOQUE DE HORARIO CON MA1111".toSentenceCase())
		assertEquals("Sin cupo en DACE", "  sin cupo   en dace ".toSentenceCase())
	}

	@Test
	fun toSentenceCase_collapsesWhitespace_andKeepsTheFinalPunctuation() {
		assertEquals("Consulte en DACE.", "CONSULTE EN DACE.".toSentenceCase())
		assertEquals("Límite de créditos", "  LÍMITE   DE CRÉDITOS ".toSentenceCase())
	}

	@Test
	fun toSentenceCase_ofALongSentence_readsAsOneSentence() {
		assertEquals(
			"El estudiante excede el límite de créditos permitido para el trimestre en curso según DACE",
			"EL ESTUDIANTE EXCEDE EL LÍMITE DE CRÉDITOS PERMITIDO PARA EL TRIMESTRE EN CURSO SEGÚN DACE"
				.toSentenceCase()
		)
	}

	@Test
	fun toSentenceCase_ofAnEmptyString_isEmpty() {
		assertEquals("", "   ".toSentenceCase())
	}
}
