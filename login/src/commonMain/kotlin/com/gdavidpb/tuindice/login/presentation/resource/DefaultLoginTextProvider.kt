package com.gdavidpb.tuindice.login.presentation.resource

class DefaultLoginTextProvider : LoginTextProvider {
	override fun privacyPolicyTitle(): String = "TuIndice - Política de privacidad"

	override fun termsAndConditionsTitle(): String = "TuIndice - Términos y condiciones"

	override fun invalidCredentials(): String = "USBID o contraseña inválida"

	override fun userDisabled(): String = "Cuenta inhabilitada"

	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun retry(): String = "Reintentar"

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun passwordUpdated(): String = "Contraseña actualizada"

	override fun invalidPassword(): String = "Contraseña inválida"

	override fun defaultError(): String = "¡Ha ocurrido un error!"
}
