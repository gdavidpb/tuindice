package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationAssistantContextMappingTest {
	private companion object {
		val DATE = LocalDate(2026, 10, 12)
	}

	@Test
	fun assistantContext_ofADatedRowAsksAboutItsEvaluationSubjectAndAttempt() {
		val item = listOf(DEFAULT_PENDING_EVALUATION.copy(date = DATE.toEvaluationEpochMillis()))
			.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = listOf(DEFAULT_EVALUATION_SUBJECT))
			.single()
			.items
			.single()

		assertEquals(
			AssistantScreenContext(
				screen = AssistantScreen.EVALUATION_DETAIL,
				entities = listOf(
					AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = "evaluation-1"),
					AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "INF-101"),
					AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "subject-1")
				),
				visibleFields = mapOf(
					"title" to "QUIZ 1",
					"date" to "2026-10-12",
					"points" to "0.0 / 35.0",
					"maxGrade" to "35.00",
					"state" to "PENDING"
				)
			),
			item.assistantContext
		)
	}

	@Test
	fun assistantContext_ofAGradedRowNamesItsGradeAndMaxGradeWithTwoDecimals() {
		val item = listOf(DEFAULT_COMPLETED_EVALUATION.copy(date = DATE.toEvaluationEpochMillis()))
			.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = listOf(SECOND_EVALUATION_SUBJECT))
			.single()
			.items
			.single()

		assertEquals(
			AssistantScreenContext(
				screen = AssistantScreen.EVALUATION_DETAIL,
				entities = listOf(
					AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = "evaluation-2"),
					AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "MAT-101"),
					AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "subject-2")
				),
				visibleFields = mapOf(
					"title" to "TEST 1",
					"date" to "2026-10-12",
					"points" to "18.0 / 25.0",
					"grade" to "18.00",
					"maxGrade" to "25.00",
					"state" to "COMPLETED"
				)
			),
			item.assistantContext
		)
	}

	@Test
	fun assistantContext_ofAContinuousRowNamesItsModeInsteadOfADate() {
		val continuous = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			state = EvaluationState.CONTINUOUS,
			date = null
		)

		val item = listOf(continuous)
			.toEvaluationItemList(mapping = testEvaluationItemMapping(), attempts = listOf(DEFAULT_EVALUATION_SUBJECT))
			.single()
			.items
			.single()

		assertEquals(
			mapOf(
				"title" to "QUIZ 1",
				"mode" to "CONTINUOUS",
				"points" to "0.0 / 35.0",
				"maxGrade" to "35.00",
				"state" to "CONTINUOUS"
			),
			item.assistantContext.visibleFields
		)
	}
}
