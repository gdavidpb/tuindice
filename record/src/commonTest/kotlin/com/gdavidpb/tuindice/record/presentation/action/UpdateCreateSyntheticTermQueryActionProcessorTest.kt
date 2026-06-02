package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UpdateCreateSyntheticTermQueryActionProcessorTest {
	@Test
	fun process_preservesQueryCursorSelectionInState() = runTest {
		val mutation = UpdateCreateSyntheticTermQueryActionProcessor()
			.process(
				action = CreateSyntheticTerm.Action.UpdateQuery(
					query = "ep2308",
					selectionStart = 2,
					selectionEnd = 2
				),
				sideEffect = {}
			)
			.first()

		val state = mutation(CreateSyntheticTerm.State())

		assertEquals("ep2308", state.query)
		assertEquals(2, state.querySelectionStart)
		assertEquals(2, state.querySelectionEnd)
		assertFalse(state.hasSearchError)
	}

	@Test
	fun process_clampsSelectionAndClearsResultsForShortQuery() = runTest {
		val previousResult = SyntheticTermSubject(
			subjectCode = "EP2308",
			name = "Proyecto de Grado II",
			credits = 3
		)
		val mutation = UpdateCreateSyntheticTermQueryActionProcessor()
			.process(
				action = CreateSyntheticTerm.Action.UpdateQuery(
					query = "e",
					selectionStart = -1,
					selectionEnd = 10
				),
				sideEffect = {}
			)
			.first()

		val state = mutation(
			CreateSyntheticTerm.State(
				searchResults = listOf(previousResult.toCreateTermSubjectItem()),
				isRefreshingSearch = true,
				hasSearchError = true
			)
		)

		assertEquals("e", state.query)
		assertEquals(0, state.querySelectionStart)
		assertEquals(1, state.querySelectionEnd)
		assertEquals(emptyList(), state.searchResults)
		assertFalse(state.isRefreshingSearch)
		assertFalse(state.hasSearchError)
	}
}
