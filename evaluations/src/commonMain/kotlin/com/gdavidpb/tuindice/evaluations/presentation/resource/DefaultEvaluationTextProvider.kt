package com.gdavidpb.tuindice.evaluations.presentation.resource

class DefaultEvaluationTextProvider : EvaluationTextProvider {
	override fun defaultError(): String = "¡Ha ocurrido un error!"

	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun evaluationAdded(): String = "Evaluación agregada"

	override fun evaluationUpdated(): String = "Evaluación actualizada"

	override fun evaluationRemoved(): String = "Evaluación removida"

	override fun evaluationGradeUpdated(): String = "Nota actualizada"

	override fun evaluationSubjectMissed(): String = "Debes seleccionar una materia"

	override fun evaluationTypeMissed(): String = "Debes seleccionar un tipo"

	override fun evaluationMaxGradeMissed(): String = "Debes asignar una nota máxima"
}
