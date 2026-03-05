package com.gdavidpb.tuindice.evaluations.testing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation as EvaluationContract
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

private const val PENDING_DATE = 1_900_000_000_000L
private const val COMPLETED_DATE = 1_700_000_000_000L

fun uiSubjects(): List<Subject> = listOf(
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
	return EvaluationContract.State.Content(
		availableSubjects = uiSubjects(),
		selectedSubject = DEFAULT_EVALUATION_SUBJECT,
		type = EvaluationType.QUIZ,
		date = if (isOverdue) COMPLETED_DATE else PENDING_DATE,
		isOverdue = isOverdue,
		grade = if (isOverdue) 18.5 else null,
		maxGrade = 20.0
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
		originalEvaluations = originalEvaluations,
		filteredEvaluations = filteredEvaluations,
		availableFilters = uiAvailableFilters(),
		activeFilters = activeFilters
	)
}

fun evaluationItemFixture(
	evaluationId: String = "evaluation-item-1",
	isClickable: Boolean = true,
	isOverdue: Boolean = false
): EvaluationItem {
	return EvaluationItem(
		evaluationId = evaluationId,
		grade = if (isOverdue) null else 17.5,
		maxGrade = 20.0,
		nameText = "Quiz #1",
		subjectCodeText = DEFAULT_EVALUATION_SUBJECT.code,
		highlightIconColor = if (isOverdue) Color.Red else Color.Unspecified,
		highlightTextColor = if (isOverdue) Color.Red else Color.Unspecified,
		typeAndSubjectCodeText = "Quiz ${DEFAULT_EVALUATION_SUBJECT.code}",
		typeIcon = Icons.Outlined.Quiz,
		dateText = if (isOverdue) "Vencida" else "Manana",
		dateIcon = Icons.Outlined.Event,
		gradesText = if (isOverdue) "Sin nota / 20,00" else "17,50 / 20,00",
		gradesIcon = Icons.Outlined.AssignmentTurnedIn,
		isOverdue = isOverdue,
		isClickable = isClickable
	)
}

fun evaluationsGroupItemsFixture(): List<EvaluationsGroupItem> = listOf(
	EvaluationsGroupItem(
		title = "Esta semana",
		items = listOf(
			evaluationItemFixture(evaluationId = "evaluation-item-1", isClickable = true)
		)
	)
)
