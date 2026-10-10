package com.gdavidpb.tuindice.subjects.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchDraft
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine
import com.gdavidpb.tuindice.subjects.presentation.mapper.toSubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The table of the subject search machine, one row at a time and without the pipeline: every
 * event that belongs to a query is ignored when that query is no longer the typed one, and
 * applied when it only differs by what the normalizer drops.
 */
class SubjectSearchTransitionsTest {
	private val host = object : MachineHost<SubjectSearch.Effect> {
		override fun sendEffect(effect: SubjectSearch.Effect) = Unit

		override fun processInternalEvent(event: Any) = Unit

		override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job = Job()
	}

	private val definition = run {
		val repository = ControllableSubjectCatalogRepository()
		val reportingRepository = RecordingReportingRepository()

		SubjectSearchMachine(
			draft = SubjectSearchDraft(),
			observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
				subjectCatalogRepository = repository,
				reportingRepository = reportingRepository
			),
			refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
				subjectCatalogRepository = repository,
				reportingRepository = reportingRepository
			)
		).define(host)
	}

	private val physics = subjectSearchResult(subjectCode = "FIS101").toSubjectSearchResultItem()

	private val typingPhysics = SubjectSearch.State(
		query = "fisica",
		results = listOf(physics),
		isRefreshing = true,
		hasRemoteError = false
	)

	private suspend fun next(state: SubjectSearch.State, event: Any): SubjectSearch.State {
		val result = definition.process(state, event)

		assertIs<TransitionResult.Transitioned<SubjectSearch.State>>(result)

		return result.toState
	}

	@Test
	fun eventsOfAnotherQuery_leaveTheStateUntouched() = runTest {
		val supersededEvents = listOf(
			SubjectSearchInternalEvent.LocalResultsChanged(query = "calculo", results = emptyList()),
			SubjectSearchInternalEvent.ShortQueryCleared(query = "c"),
			SubjectSearchInternalEvent.RemoteSearchStarted(query = "calculo"),
			SubjectSearchInternalEvent.RemoteSearchSucceeded(query = "calculo"),
			SubjectSearchInternalEvent.RemoteSearchFailed(query = "calculo"),
			SubjectSearchInternalEvent.RetryStarted(query = "calculo"),
			SubjectSearchInternalEvent.RetryCleared(query = "calculo")
		)

		for (event in supersededEvents) {
			assertEquals(typingPhysics, next(typingPhysics, event), "$event must be ignored")
		}
	}

	@Test
	fun aLateFailureOfAnotherQuery_doesNotRaiseTheErrorOfTheTypedOne() = runTest {
		val waiting = typingPhysics.copy(results = emptyList())

		val after = next(waiting, SubjectSearchInternalEvent.RemoteSearchFailed(query = "calculo"))

		assertEquals(false, after.hasRemoteError)
		assertEquals(true, after.isRefreshing)
	}

	@Test
	fun eventsOfTheTypedQuery_areApplied() = runTest {
		assertEquals(
			typingPhysics.copy(isRefreshing = false),
			next(typingPhysics, SubjectSearchInternalEvent.RemoteSearchSucceeded(query = "fisica"))
		)
		assertEquals(
			typingPhysics.copy(isRefreshing = false, hasRemoteError = false),
			next(typingPhysics, SubjectSearchInternalEvent.RetryCleared(query = "fisica"))
		)
		assertEquals(
			typingPhysics.copy(results = emptyList(), isRefreshing = false),
			next(typingPhysics, SubjectSearchInternalEvent.ShortQueryCleared(query = "fisica"))
		)
		assertEquals(
			true,
			next(
				typingPhysics.copy(results = emptyList(), isRefreshing = false),
				SubjectSearchInternalEvent.RemoteSearchFailed(query = "fisica")
			).hasRemoteError
		)
		assertEquals(
			true,
			next(
				typingPhysics.copy(isRefreshing = false),
				SubjectSearchInternalEvent.RetryStarted(query = "fisica")
			).isRefreshing
		)
	}

	@Test
	fun eventsOfANormalisationEqualQuery_areApplied() = runTest {
		val typed = typingPhysics.copy(query = "FÍSICA ")

		assertEquals(
			typed.copy(isRefreshing = false),
			next(typed, SubjectSearchInternalEvent.RemoteSearchSucceeded(query = "fisica"))
		)
		assertEquals(
			listOf(physics),
			next(
				typed.copy(results = emptyList()),
				SubjectSearchInternalEvent.LocalResultsChanged(query = "fisica", results = listOf(physics))
			).results
		)
	}
}
