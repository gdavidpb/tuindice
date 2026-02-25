package com.gdavidpb.tuindice.about.presentation.resource

class DefaultAboutTextProvider : AboutTextProvider {
	override fun privacyPolicyTitle(): String = "TuIndice - Política de privacidad"

	override fun termsAndConditionsTitle(): String = "TuIndice - Términos y condiciones"

	override fun shareMessage(): String {
		return "TuIndice: Una nueva forma de administrar tus notas"
	}

	override fun shareSubject(): String = "TuIndice"
}
