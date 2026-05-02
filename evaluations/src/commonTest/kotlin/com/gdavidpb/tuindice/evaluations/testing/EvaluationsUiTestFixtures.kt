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
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

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
	isOverdue: Boolean = false
): EvaluationContract.State.Content {
	val selectedAttempt = DEFAULT_EVALUATION_SUBJECT
	val type = EvaluationType.QUIZ
	val grade = if (isOverdue) 18.5 else null
	val maxGrade = 20.0

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

	return Evaluations.State.Content(
		evaluationGroups = filteredEvaluations.toFixtureEvaluationGroups(),
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
		grade = if (isOverdue) null else 17.5,
		maxGrade = 20.0,
		nameText = "Quiz #1",
		subjectCodeText = DEFAULT_EVALUATION_SUBJECT.code,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = if (isOverdue) {
			EvaluationHighlightTone.Error
		} else {
			EvaluationHighlightTone.Neutral
		},
		typeText = "Quiz",
		typeIcon = EvaluationType.QUIZ.asIcon(),
		dateText = if (isOverdue) "Vencida" else "Manana",
		dateIcon = Icons.Outlined.Event,
		gradesText = if (isOverdue) "Sin nota / 20,00" else "17,50 / 20,00",
		gradeActionText = if (isOverdue) "Sin nota" else "17,50",
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
	gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
	maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
	showsGradeChip = isOverdue
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
		subjectCodeText = subjectCode,
		subjectCodeColor = subjectColors.color,
		subjectCodeContainerColor = subjectColors.containerColor,
		highlightTone = when (state) {
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			else -> EvaluationHighlightTone.Neutral
		},
		typeText = uiTypeLabels().getValue(type),
		typeIcon = type.asIcon(),
		dateText = "Fecha",
		dateIcon = Icons.Outlined.Event,
		gradesText = if (currentGrade != null) {
			"${currentGrade.formatGrade(decimals = 2)} / ${maxGrade.formatGrade(decimals = 2)}"
		} else {
			"Pendiente / ${maxGrade.formatGrade(decimals = 2)}"
		},
		gradeActionText = currentGrade?.formatGrade(decimals = 2) ?: "Sin nota",
		showsGradeAction = (state != EvaluationState.PENDING),
		gradesIcon = Icons.Outlined.AssignmentTurnedIn,
		isOverdue = (state == EvaluationState.OVERDUE),
		isClickable = (state != EvaluationState.PENDING)
	)
}
