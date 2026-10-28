package com.gdavidpb.tuindice.record.presentation.route

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecordRouteAssistantContextTest {
	@Test
	fun when_theRouteStateIsMade_then_itDeclaresTheContextWithoutReadingTheRecord() {
		val routeState = assertIs<RecordRouteViewState>(unreadContent().toRouteViewState())

		assertTrue(routeState.hasAssistantContext, "the record declares a context for the assistant")
	}

	@Test
	fun when_theRecordIsIdleOrLoading_then_itDeclaresTheContext() {
		assertTrue(Record.State.Idle.toRouteViewState().hasAssistantContext)
		assertTrue(Record.State.Loading.toRouteViewState().hasAssistantContext)
	}

	@Test
	fun when_theRouteStateDeclaresNothing_then_theBarOffersNoAssistant() {
		val routeState = RecordRouteViewState()

		assertFalse(routeState.hasAssistantContext)
		assertNull(routeState.assistantContext)
	}

	@Test
	fun when_theBarAsksForTheContext_then_itIsTheContextOfTheTermOnScreen() {
		val content = Record.State.Content(
			viewMode = RecordViewMode.Historical,
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "term-a",
						kind = TermKind.HISTORICAL,
						periodYear = 2025,
						attempts = listOf(
							academicAttempt(subjectCode = "MA1112", id = "attempt-a", score = AttemptScore.numeric(3))
						)
					)
				)
			),
			selectedTermId = "term-a"
		)

		val routeState = assertIs<RecordRouteViewState>(content.toRouteViewState())

		assertEquals(content.assistantContext, routeState.assistantContext)
	}

	// Its terms cannot be read: a projection made while the route state is built would stop here.
	private fun unreadContent(): Record.State.Content {
		val unreadTerms = object : AbstractList<AcademicTerm>() {
			override val size: Int
				get() = error("the record was read")

			override fun get(index: Int): AcademicTerm = error("the record was read")
		}

		return Record.State.Content(
			viewMode = RecordViewMode.Historical,
			record = AcademicRecord(id = "record", terms = unreadTerms),
			selectedTermId = "term-a"
		)
	}
}
