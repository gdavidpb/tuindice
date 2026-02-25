package com.gdavidpb.tuindice.summary.presentation.resource

interface SummaryItemsTextProvider {
	fun subjectsHeader(count: Int): String

	fun subjectsApprovedLabel(): String

	fun subjectsFailedLabel(): String

	fun subjectsRetiredLabel(): String

	fun creditsHeader(count: Int): String

	fun creditsApprovedLabel(): String

	fun creditsFailedLabel(): String

	fun creditsRetiredLabel(): String
}

class DefaultSummaryItemsTextProvider : SummaryItemsTextProvider {
	override fun subjectsHeader(count: Int): String {
		return if (count == 1) {
			"$count materia inscrita"
		} else {
			"$count materias inscritas"
		}
	}

	override fun subjectsApprovedLabel() = "Aprobadas"

	override fun subjectsFailedLabel() = "Reprobadas"

	override fun subjectsRetiredLabel() = "Retiradas"

	override fun creditsHeader(count: Int): String {
		return if (count == 1) {
			"$count crédito inscrito"
		} else {
			"$count créditos inscritos"
		}
	}

	override fun creditsApprovedLabel() = "Aprobados"

	override fun creditsFailedLabel() = "Reprobados"

	override fun creditsRetiredLabel() = "Retirados"
}
