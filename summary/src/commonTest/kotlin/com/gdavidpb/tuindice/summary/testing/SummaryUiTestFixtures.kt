package com.gdavidpb.tuindice.summary.testing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileCopy
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryAttentionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryClassRowItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryWeekItem
import kotlinx.datetime.LocalDate

fun summaryContentState(
	profilePictureUrl: String = "https://cdn.tuindice.app/profile.jpg",
	isProfilePictureLoading: Boolean = false,
	isUserRefreshing: Boolean = false,
	syncStatusText: UiText = UiText.Raw(DEFAULT_SYNC_STATUS_TEXT),
	hub: SummaryHubItem = SummaryHubItem()
): Summary.State.Content = Summary.State.Content(
	name = "Ana Diaz",
	careerName = "Ingenieria Informatica",
	grade = 4.25f,
	profilePictureUrl = profilePictureUrl,
	isProfilePictureLoading = isProfilePictureLoading,
	isUserRefreshing = isUserRefreshing,
	syncStatusText = syncStatusText,
	hub = hub
)

const val DEFAULT_SYNC_STATUS_TEXT = "Última sincronización: Hoy"

// The day the mock of the hub is about (a Thursday); "Vie 16" is the day after.
val HUB_FIXTURE_TODAY = LocalDate(2026, 10, 15)

val HUB_FIXTURE_ATTENTION_WEEK_START = LocalDate(2026, 10, 5)

fun summaryClassRowItem(
	isInProgress: Boolean = true,
	leadText: String = "En curso",
	subjectCode: String = "CI5437"
) = SummaryClassRowItem(
	leadText = UiText.Raw(leadText),
	isInProgress = isInProgress,
	subjectCode = subjectCode,
	place = ClassPlaceItem(
		placeText = UiText.Raw("3-4 · MYS-210"),
		timeText = UiText.Raw("9:30–11:30 a. m.")
	)
)

fun summaryEvaluationRowItem(
	evaluationId: String = "evaluation-1",
	leadText: String = "Hoy",
	titleText: String = "PARCIAL 1",
	subjectCode: String = "CI5437",
	hasPlace: Boolean = true
) = SummaryEvaluationRowItem(
	evaluationId = evaluationId,
	leadText = UiText.Raw(leadText),
	typeIcon = Icons.Outlined.FileCopy,
	titleText = UiText.Raw(titleText),
	statusText = UiText.Raw("Programada"),
	isOverdue = false,
	subjectCode = subjectCode,
	place = if (hasPlace) {
		ClassPlaceItem(placeText = UiText.Raw("3-4 · MYS-210"), timeText = null)
	} else {
		null
	},
	// The Monday of the week the fixtures are about.
	weekStart = LocalDate(2026, 10, 12)
)

fun summaryTermWeekItem(week: Int = 3) = SummaryTermWeekItem(
	week = week,
	totalWeeks = 12,
	valueText = UiText.Raw("Semana $week"),
	restText = UiText.Raw("de 12"),
	description = UiText.Raw("Semana $week de 12"),
	remainingText = UiText.Raw(
		when (val left = 12 - week) {
			0 -> "última semana"
			1 -> "falta 1 semana"
			else -> "faltan $left semanas"
		}
	)
)

// "evaluated" is the maximum of the graded evaluations: the bar shows what is evaluated of the 100 points, which is
// nothing before a grade and 100 at most, and the line says it only once a grade is in.
fun summaryPointsItem(
	subjectCode: String,
	earned: String? = null,
	evaluated: String = "60",
	hasEvaluations: Boolean = true,
	nextText: String? = null
): SummarySubjectPointsItem {
	val evaluatedShown = when {
		earned == null -> "0"
		evaluated.toDouble() > 100.0 -> "100"
		else -> evaluated
	}

	return SummarySubjectPointsItem(
		subjectCode = subjectCode,
		valueText = earned,
		restText = UiText.Raw(
			when {
				!hasEvaluations -> "Sin evaluaciones"
				earned == null -> "Sin notas aún"
				else -> "de $evaluated puntos"
			}
		),
		attemptId = "attempt-$subjectCode",
		evaluatedText = UiText.Raw("$evaluatedShown de 100 evaluados").takeIf { earned != null },
		evaluatedFraction = if (hasEvaluations) evaluatedShown.toFloat() / 100f else null,
		nextText = nextText?.let { text -> UiText.Raw(text) }
	)
}

fun summaryCareerProgressItem() = SummaryCareerItem.Progress(
	valueText = "156",
	restText = UiText.Raw("de 240 créditos"),
	percentText = UiText.Raw("65 %"),
	fraction = 0.65f,
	description = UiText.Raw("Avance del pensum: 156 de 240 créditos, 65 %")
)

// The hub of the mock of a day with something to attend to and a class in progress.
fun summaryHubItem(
	attention: List<SummaryAttentionItem> = listOf(
		SummaryAttentionItem(
			key = "ungraded",
			text = UiText.Raw("2 evaluaciones sin nota"),
			target = SummaryHubTarget.EvaluationsWeek(
					weekStart = HUB_FIXTURE_ATTENTION_WEEK_START,
					evaluationId = "evaluation-1"
				)
		),
		SummaryAttentionItem(
			key = "enrollment-CI5311",
			text = UiText.Raw("Choque de horario en CI5311"),
			target = SummaryHubTarget.Record
		)
	),
	week: SummaryWeekItem? = SummaryWeekItem(
		classRow = summaryClassRowItem(),
		evaluations = listOf(
			summaryEvaluationRowItem(),
			summaryEvaluationRowItem(
				evaluationId = "evaluation-2",
				leadText = "Vie 16",
				titleText = "ENSAYO 1",
				subjectCode = "EG1114",
				hasPlace = false
			)
		),
		hasSchedule = true,
		card = null
	),
	term: SummaryTermItem? = SummaryTermItem(
		linkText = UiText.Raw("Sep - Dic 2026"),
		week = summaryTermWeekItem(),
		// The three subjects of the rows below, with 4 + 4 + 3 credits.
		summaryText = UiText.Raw("3 materias · 11 UC"),
		subjects = listOf(
			summaryPointsItem(subjectCode = "CI5311", earned = "41", nextText = "Próx.: Parcial 2 · lun 26"),
			summaryPointsItem(subjectCode = "CI5437", earned = "18", evaluated = "25"),
			summaryPointsItem(subjectCode = "EG1114")
		),
		finished = null,
		card = null
	),
	career: SummaryCareerItem? = summaryCareerProgressItem()
) = SummaryHubItem(attention = attention, week = week, term = term, career = career)
