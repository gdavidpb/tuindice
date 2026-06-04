package com.gdavidpb.tuindice.evaluations.testing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Event
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation as EvaluationContract
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationAttemptPickerItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationTypePickerItemList
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem

private const val PENDING_DATE = 1_900_000_000_000L
private const val COMPLETED_DATE = 1_700_000_000_000L
private const val FIXTURE_GROUP_TITLE = "Esta semana"

fun uiSubjects(): List<EditableAttemptDescriptor> = listOf(
	DEFAULT_EVALUATION_SUBJECT,
	SECOND_EVALUATION_SUBJECT
)

fun uiDomainEvaluations(): List<Evaluation> = listOf(
	DEFAULT_COMPLETED_EVALUATION,
	DEFAULT_PENDING_EVALUATION
)

fun uiAvailableFilters(): List<EvaluationFilter> = listOf(
	EvaluationStateFilter(label = "Pendientes") { evaluation ->
		evaluation.state == EvaluationState.PENDING
	},
	EvaluationStateFilter(label = "Completadas") { evaluation ->
		evaluation.state == EvaluationState.COMPLETED
	}
)

fun evaluationContentState(
	isOverdue: Boolean = false,
	grade: Double? = if (isOverdue) 18.5 else null,
	maxGrade: Double? = 20.0
): EvaluationContract.State.Content {
	val selectedAttempt = DEFAULT_EVALUATION_SUBJECT
	val type = EvaluationType.QUIZ

	return EvaluationContract.State.Content(
		attemptItems = uiSubjects().toEvaluationAttemptPickerItems(
			selectedAttempt = selectedAttempt
		),
		selectedAttempt = selectedAttempt,
		type = type,
		typeItems = EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = type,
			typeLabels = uiTypeLabels()
		),
		scheduleMode = EvaluationScheduleMode.DATED,
		date = if (isOverdue) COMPLETED_DATE else PENDING_DATE,
		isOverdue = isOverdue,
		grade = grade,
		maxGrade = maxGrade,
		gradeSection = uiGradeSection(
			isOverdue = isOverdue,
			grade = grade,
			maxGrade = maxGrade
		)
	)
}

fun evaluationsContentState(
	originalEvaluations: List<Evaluation> = uiDomainEvaluations(),
	activeFilters: List<EvaluationFilter> = emptyList()
): Evaluations.State.Content {
	val filteredEvaluations = if (activeFilters.isEmpty()) {
		originalEvaluations
	} else {
		originalEvaluations.filter { evaluation ->
			activeFilters.all { filter -> filter.match(evaluation) }
		}
	}
	val evaluationGroups = filteredEvaluations.toFixtureEvaluationGroups()

	return Evaluations.State.Content(
		weekItem = evaluationsWeekItemFixture(),
		evaluationGroups = evaluationGroups,
		evaluationWeekGroups = evaluationsWeekGroupItemsFixture(evaluationGroups),
		filterGroups = uiAvailableFilters().toEvaluationFilterGroupItemList(
			activeFilters = activeFilters
		),
		activeFilters = activeFilters
	)
}

fun evaluationItemFixture(
	evaluationId: String = "evaluation-item-1",
	isClickable: Boolean = true,
	isOverdue: Boolean = false,
	showsGradeAction: Boolean = true
): EvaluationItem {
	val subjectColors = CourseCodeColorGenerator.fromCode(DEFAULT_EVALUATION_SUBJECT.code)

	return EvaluationItem(
		evaluationId = evaluationId,
		grade = if (isOverdue) null else 10.0,
		maxGrade = 35.0,
		nameText = "Quiz #1",
		subjectNameText = DEFAULT_EVALUATION_SUBJECT.name,
		subjectCodeText = DEFAULT_EVALUATION_SUBJECT.code,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = if (isOverdue) {
			EvaluationHighlightTone.Error
		} else {
			EvaluationHighlightTone.Neutral
		},
		statusText = if (isOverdue) "Pendiente" else "Programada",
		statusTone = if (isOverdue) EvaluationHighlightTone.Error else EvaluationHighlightTone.Neutral,
		typeText = "Quiz",
		typeNameText = "Quiz #1",
		typeIcon = EvaluationType.QUIZ.asIcon(),
		dateText = if (isOverdue) "Vencida" else "Manana",
		dateIcon = Icons.Outlined.Event,
		gradeText = if (isOverdue) "-- / 35" else "10 / 35",
		gradesText = if (isOverdue) "Sin nota / 35,00" else "10,00 / 35,00",
		gradeActionText = if (isOverdue) "-- / 35" else "10 / 35",
		showsGradeAction = showsGradeAction,
		gradesIcon = Icons.Outlined.AssignmentTurnedIn,
		isOverdue = isOverdue,
		isClickable = isClickable
	)
}

fun evaluationsGroupItemsFixture(): List<EvaluationsGroupItem> = listOf(
	EvaluationsGroupItem(
		title = FIXTURE_GROUP_TITLE,
		items = listOf(
			evaluationItemFixture(evaluationId = "evaluation-item-1", isClickable = true)
		)
	)
)

fun evaluationsWeekGroupItemsFixture(
	groups: List<EvaluationsGroupItem> = evaluationsGroupItemsFixture()
): List<EvaluationsWeekGroupItem> = listOf(
	EvaluationsWeekGroupItem(
		weekNumber = 8,
		title = "Semana 8",
		groups = groups
	)
)

fun evaluationsWeekItemFixture(): EvaluationsWeekItem = EvaluationsWeekItem(
	weekNumber = 8,
	labelText = "Semana 8",
	days = listOf(
		EvaluationWeekDayItem("LUN", "19", isSelected = false, hasEvaluations = false),
		EvaluationWeekDayItem("MAR", "20", isSelected = false, hasEvaluations = true),
		EvaluationWeekDayItem("MIE", "21", isSelected = true, hasEvaluations = true),
		EvaluationWeekDayItem("JUE", "22", isSelected = false, hasEvaluations = true),
		EvaluationWeekDayItem("VIE", "23", isSelected = false, hasEvaluations = false),
		EvaluationWeekDayItem("SAB", "24", isSelected = false, hasEvaluations = false),
		EvaluationWeekDayItem("DOM", "25", isSelected = false, hasEvaluations = false)
	)
)

private fun uiTypeLabels(): Map<EvaluationType, String> = mapOf(
	EvaluationType.TEST to "Prueba",
	EvaluationType.ESSAY to "Ensayo",
	EvaluationType.ATTENDANCE to "Asistencia",
	EvaluationType.INTERVENTIONS to "Intervenciones",
	EvaluationType.LABORATORY to "Laboratorio",
	EvaluationType.MODEL to "Modelo",
	EvaluationType.PRESENTATION to "Presentacion",
	EvaluationType.PROJECT to "Proyecto",
	EvaluationType.QUIZ to "Quiz",
	EvaluationType.REPORT to "Reporte",
	EvaluationType.WORKSHOP to "Taller",
	EvaluationType.WRITTEN_WORK to "Trabajo escrito",
	EvaluationType.OTHER to "Otro"
)

private fun uiGradeSection(
	isOverdue: Boolean,
	grade: Double?,
	maxGrade: Double?
) = EvaluationGradeSectionItem(
	maxGradeTitleText = "Nota maxima",
	overdueTitleText = "Notas",
	gradeText = "${(grade ?: 0.0).formatGrade(decimals = 2)} / ${(maxGrade ?: 0.0).formatGrade(decimals = 2)}",
	maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
	showsGradeChip = isOverdue && maxGrade != null && maxGrade > 0.0
)

private fun List<Evaluation>.toFixtureEvaluationGroups(): List<EvaluationsGroupItem> {
	if (isEmpty()) return emptyList()

	return listOf(
		EvaluationsGroupItem(
			title = FIXTURE_GROUP_TITLE,
			items = map { evaluation -> evaluation.toFixtureEvaluationItem() }
		)
	)
}

private fun Evaluation.toFixtureEvaluationItem(): EvaluationItem {
	val subjectColors = CourseCodeColorGenerator.fromCode(subjectCode)
	val currentGrade = grade

	return EvaluationItem(
		evaluationId = id,
		grade = grade,
		maxGrade = maxGrade,
		nameText = "${uiTypeLabels().getValue(type)} #1",
		subjectNameText = when (attemptId) {
			DEFAULT_EVALUATION_SUBJECT.id -> DEFAULT_EVALUATION_SUBJECT.name
			else -> SECOND_EVALUATION_SUBJECT.name
		},
		subjectCodeText = subjectCode,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = when (state) {
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			else -> EvaluationHighlightTone.Neutral
		},
		statusText = when (state) {
			EvaluationState.PENDING -> "Programada"
			EvaluationState.OVERDUE -> "Pendiente"
			EvaluationState.COMPLETED -> "Completada"
			EvaluationState.CONTINUOUS -> "Continua"
		},
		statusTone = when (state) {
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			else -> EvaluationHighlightTone.Neutral
		},
		typeText = uiTypeLabels().getValue(type),
		typeNameText = "${uiTypeLabels().getValue(type)} #1",
		typeIcon = type.asIcon(),
		dateText = "Fecha",
			dateIcon = Icons.Outlined.Event,
			gradeText = currentGrade?.let {
				"${it.formatGrade(decimals = 0)} / ${maxGrade.formatGrade(decimals = 0)}"
			} ?: "-- / ${maxGrade.formatGrade(decimals = 0)}",
		gradesText = if (currentGrade != null) {
			"${currentGrade.formatGrade(decimals = 2)} / ${maxGrade.formatGrade(decimals = 2)}"
		} else {
			"Pendiente / ${maxGrade.formatGrade(decimals = 2)}"
			},
			gradeActionText = currentGrade?.let {
				"${it.formatGrade(decimals = 0)} / ${maxGrade.formatGrade(decimals = 0)}"
			} ?: "-- / ${maxGrade.formatGrade(decimals = 0)}",
		showsGradeAction = true,
		gradesIcon = Icons.Outlined.AssignmentTurnedIn,
		isOverdue = (state == EvaluationState.OVERDUE),
		isClickable = true
	)
}
