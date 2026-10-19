package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportDeltaTone
import com.gdavidpb.tuindice.summary.presentation.model.TermReportHeroItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportLegendItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSectionItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectsItem

// A report as the screen draws it: a term with a subject passed, one failed and one withdrawn, the pace of three
// regular terms with the last one in the accent, and two sections and two actions of "Qué sigue".
fun termReportItemFixture(
	insightId: String = "term-1:2",
	termId: String? = "term-1",
	sections: List<TermReportSectionItem> = listOf(
		TermReportSectionItem(title = "Cerraste con 4,18", text = "Aprobaste las 3 materias."),
		TermReportSectionItem(title = "Tu ritmo", text = "Este trimestre aprobaste 11 créditos.")
	),
	isShareable: Boolean = true,
	confirmedFeedback: Boolean? = null
) = TermReportItem(
	insightId = insightId,
	termId = termId,
	termKey = "2026-APR_JUL",
	periodLabel = "Abr - Jul 2026",
	title = "Cerraste con 4,18",
	hero = TermReportHeroItem(
		title = UiText.Raw("Índice del trimestre · Abr - Jul 2026"),
		index = "4.1750",
		indexDelta = TermReportDeltaItem(
			text = UiText.Raw("▲ 1.5000"),
			description = UiText.Raw("Subió 1.5000 frente a Ene\u00A0-\u00A0Mar\u00A02025"),
			tone = TermReportDeltaTone.POSITIVE,
			reference = UiText.Raw("frente a Ene\u00A0-\u00A0Mar\u00A02025")
		),
		cumulativeIndex = "4.0331",
		cumulativeDelta = TermReportDeltaItem(UiText.Raw("▲ 0,02"), UiText.Raw("Subió 0.02"), TermReportDeltaTone.POSITIVE)
	),
	subjects = TermReportSubjectsItem(
		credits = UiText.Raw("11 de 14 créditos"),
		legend = listOf(
			TermReportLegendItem(SummaryTermReportOutcome.APPROVED, UiText.Raw("1 aprobada")),
			TermReportLegendItem(SummaryTermReportOutcome.FAILED, UiText.Raw("1 reprobada")),
			TermReportLegendItem(SummaryTermReportOutcome.WITHDRAWN, UiText.Raw("1 retirada"))
		),
		rows = listOf(
			TermReportSubjectItem("CI5311", "Ingeniería de Software", "5", SummaryTermReportOutcome.APPROVED),
			TermReportSubjectItem("CI5437", "Inteligencia Artificial", "2", SummaryTermReportOutcome.FAILED),
			TermReportSubjectItem("EG1114", "Electiva General", null, SummaryTermReportOutcome.WITHDRAWN)
		)
	),
	pace = TermReportPaceItem.Data(
		bars = listOf(
			paceBar(axis = "A-J 24", credits = 12f, isHighlighted = false),
			paceBar(axis = "E-M 25", credits = 15f, isHighlighted = false),
			paceBar(axis = "A-J 26", credits = 11f, isHighlighted = true)
		),
		reference = SummaryStatisticsReferenceItem(value = 12.7f, label = null),
		currentLegend = UiText.Raw("Este trimestre: 11"),
		averageLegend = UiText.Raw("Promedio: 12,7")
	),
	sections = sections,
	isShareable = isShareable,
	confirmedFeedback = confirmedFeedback,
	nextActions = listOf(
		TermReportActionItem(
			label = "Proyectar tu índice para Jul - Ago",
			target = AssistantNavTarget.RECORD_PROJECTION,
			entityRefs = emptyList()
		),
		TermReportActionItem(
			label = "Compartir tu trimestre",
			target = AssistantNavTarget.SHARE_TERM,
			entityRefs = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1"))
		)
	)
)

private fun paceBar(axis: String, credits: Float, isHighlighted: Boolean) = SummaryStatisticsBarItem(
	axisLabel = UiText.Raw(axis),
	value = credits,
	tooltip = SummaryStatisticsTooltipItem(title = UiText.Raw("$axis: $credits UC")),
	isHighlighted = isHighlighted
)
