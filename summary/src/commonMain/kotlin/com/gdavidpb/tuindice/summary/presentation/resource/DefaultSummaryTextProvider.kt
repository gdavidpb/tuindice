package com.gdavidpb.tuindice.summary.presentation.resource

import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate

class DefaultSummaryTextProvider : SummaryTextProvider {
	override fun lastUpdate(lastUpdate: Long): String {
		return "Última actualización: ${lastUpdate.formatLastUpdate()}"
	}

	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun noService(): String = "No pudimos actualizar, intentaremos más tarde"

	override fun defaultError(): String = "¡Ha ocurrido un error!"

	override fun profilePictureUpdated(): String = "Foto de perfil actualizada"

	override fun profilePictureRemoved(): String = "Foto de perfil removida"
}
