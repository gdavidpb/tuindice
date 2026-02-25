package com.gdavidpb.tuindice.about.presentation.resource

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAboutTextProviderTest {
	@Test
	fun providesExpectedStaticTextValues() {
		val provider = DefaultAboutTextProvider()

		assertEquals("TuIndice - Política de privacidad", provider.privacyPolicyTitle())
		assertEquals("TuIndice - Términos y condiciones", provider.termsAndConditionsTitle())
		assertEquals("TuIndice: Una nueva forma de administrar tus notas", provider.shareMessage())
		assertEquals("TuIndice", provider.shareSubject())
	}
}
