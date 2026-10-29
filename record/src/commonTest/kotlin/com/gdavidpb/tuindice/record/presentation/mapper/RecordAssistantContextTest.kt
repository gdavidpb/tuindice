package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RecordAssistantContextTest {
	@Test
	fun content_describesTheSelectedTermWithItsAttemptsSubjectsAndFigures() {
		val context = content(selectedTermId = "term-now").assistantContext

		assertEquals(AssistantScreen.RECORD, context.screen)
		assertEquals(
			listOf(
				AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-now"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "now-fis"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "now-mat"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "FIS201"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "MAT101")
			),
			context.entities
		)
		assertEquals(
			mapOf(
				"termLabel" to "Abril - Julio 2025",
				"termIndex" to "4.4285",
				"cumulativeIndex" to "3.9090",
				"credits" to "7",
				"viewMode" to "projection",
				"isSynthetic" to "false"
			),
			context.visibleFields
		)
		assertEquals(emptyMap<String, String>(), context.filters)
	}

	@Test
	fun changingTheSelectedTerm_changesTheEntitiesAndFiguresToThatTerm() {
		val past = content(selectedTermId = "term-past").assistantContext
		val now = content(selectedTermId = "term-now").assistantContext

		assertEquals(
			listOf(
				AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-past"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "past-his"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "HIS100")
			),
			past.entities
		)
		assertEquals("Enero - Marzo 2024", past.visibleFields["termLabel"])
		assertEquals("3.0000", past.visibleFields["termIndex"])
		assertNotEquals(now.entities, past.entities)
	}

	@Test
	fun aSubjectTakenTwiceInTheSelectedTermIsListedOnce() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "term-now",
					kind = TermKind.CURRENT,
					periodYear = 2025,
					periodCode = AcademicTermPeriod.APR_JUL,
					attempts = listOf(
						scoredAttempt(id = "now-mat-1", subjectCode = "MAT101", credits = 4, grade = 4),
						scoredAttempt(id = "now-mat-2", subjectCode = "MAT101", credits = 4, grade = 4)
					)
				)
			)
		)

		val entities = content(selectedTermId = "term-now", record = record).assistantContext.entities

		assertEquals(
			listOf(
				AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-now"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "now-mat-1"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "now-mat-2"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "MAT101")
			),
			entities
		)
	}

	@Test
	fun visibleFields_carryTheViewModeAndWhetherTheSelectedTermIsSynthetic() {
		val historical = content(
			selectedTermId = "term-past",
			viewMode = RecordViewMode.Historical
		).assistantContext
		val syntheticRecord = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "term-made",
					kind = TermKind.SYNTHETIC,
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					attempts = listOf(
						scoredAttempt(id = "made-alg", subjectCode = "ALG300", credits = 3, grade = 5)
					)
				)
			)
		)
		val synthetic = content(
			selectedTermId = "term-made",
			record = syntheticRecord,
			isSelectedTermSynthetic = true
		).assistantContext

		assertEquals("historical", historical.visibleFields["viewMode"])
		assertEquals("false", historical.visibleFields["isSynthetic"])
		assertEquals("projection", synthetic.visibleFields["viewMode"])
		assertEquals("true", synthetic.visibleFields["isSynthetic"])
	}

	@Test
	fun otherStates_describeOnlyTheScreen() {
		val onlyTheScreen = AssistantScreenContext(screen = AssistantScreen.RECORD)

		assertEquals(onlyTheScreen, Record.State.Idle.assistantContext)
		assertEquals(onlyTheScreen, Record.State.Loading.assistantContext)
		assertEquals(
			onlyTheScreen,
			Record.State.Empty(title = UiText.Empty, message = UiText.Empty).assistantContext
		)
		assertEquals(
			onlyTheScreen,
			Record.State.Failed(
				title = UiText.Empty,
				message = UiText.Empty,
				art = RecordFailedArt.Error
			).assistantContext
		)
	}

	private fun recordWithPastAndCurrentTerm(): AcademicRecord {
		return AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "term-past",
					kind = TermKind.HISTORICAL,
					periodYear = 2024,
					periodCode = AcademicTermPeriod.JAN_MAR,
					attempts = listOf(
						scoredAttempt(id = "past-his", subjectCode = "HIS100", credits = 4, grade = 3)
					)
				),
				academicTerm(
					id = "term-now",
					kind = TermKind.CURRENT,
					periodYear = 2025,
					periodCode = AcademicTermPeriod.APR_JUL,
					attempts = listOf(
						scoredAttempt(id = "now-fis", subjectCode = "FIS201", credits = 3, grade = 5),
						scoredAttempt(id = "now-mat", subjectCode = "MAT101", credits = 4, grade = 4)
					)
				)
			)
		)
	}

	private fun scoredAttempt(id: String, subjectCode: String, credits: Int, grade: Int) = academicAttempt(
		id = id,
		subjectCode = subjectCode,
		credits = credits,
		score = AttemptScore.numeric(grade)
	)

	private fun content(
		selectedTermId: String,
		record: AcademicRecord = recordWithPastAndCurrentTerm(),
		viewMode: RecordViewMode = RecordViewMode.Projection,
		isSelectedTermSynthetic: Boolean = false
	): Record.State.Content {
		return Record.State.Content(
			viewMode = viewMode,
			record = record,
			selectedTermId = selectedTermId,
			isSelectedTermSynthetic = isSelectedTermSynthetic
		)
	}
}
