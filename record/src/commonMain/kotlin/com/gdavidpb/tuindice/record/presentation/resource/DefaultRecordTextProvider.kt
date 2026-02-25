package com.gdavidpb.tuindice.record.presentation.resource

class DefaultRecordTextProvider : RecordTextProvider {
	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun defaultError(): String = "¡Ha ocurrido un error!"
}
