package com.gdavidpb.tuindice.enrollmentproof.presentation.resource

class DefaultEnrollmentProofTextProvider : EnrollmentProofTextProvider {
	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun enrollmentNotFound(): String = "Comprobante no disponible"

	override fun enrollmentUnsupported(): String = "Archivo no soportado ;("

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun defaultError(): String = "¡Ha ocurrido un error!"
}
