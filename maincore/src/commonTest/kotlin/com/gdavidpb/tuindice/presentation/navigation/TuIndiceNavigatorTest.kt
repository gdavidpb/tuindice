package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
import com.gdavidpb.tuindice.base.data.source.InMemoryRecordAttemptFocusDataSource
import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.RecordAttemptFocusRequest
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.presentation.mapper.toTabRootDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

private data class TestNavResult(val value: String) : NavResult

class TuIndiceNavigatorTest {

	private fun createNavigator(
		rootMode: TuIndiceRootMode = TuIndiceRootMode.SIGNED_IN,
		startTab: MainSection = MainSection.SUMMARY,
		currentTab: MainSection = startTab,
		entryStores: NavEntryStores = NavEntryStores(),
		resultStore: NavResultStore = NavResultStore()
	): TuIndiceNavigator = TuIndiceNavigator(
		startTab = startTab,
		rootModeState = mutableStateOf(rootMode),
		currentTabState = mutableStateOf(currentTab),
		stacks = TuIndiceNavStacks(
			authStack = NavBackStack(AuthDestination.SignIn),
			tabStacks = MainSection.entries.associateWith { section ->
				NavBackStack<NavKey>(section.toTabRootDestination())
			}
		),
		entryStores = entryStores,
		resultStore = resultStore
	)

	@Test
	fun when_pushed_then_backStackGrowsOnActiveTab() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		navigator.push(RecordDestination.CreateSyntheticTerm(termId = null))

		assertEquals(
			listOf(
				SummaryDestination.Summary,
				RecordDestination.Record,
				RecordDestination.CreateSyntheticTerm(termId = null)
			),
			navigator.backStack
		)
	}

	@Test
	fun when_pushedSameTopKey_then_pushIsIgnored() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		navigator.push(RecordDestination.CreateSyntheticTerm(termId = null))
		navigator.push(RecordDestination.CreateSyntheticTerm(termId = null))

		assertEquals(3, navigator.backStack.size)
	}

	@Test
	fun when_tabSwitched_then_previousTabStackIsPreserved() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		navigator.push(SubjectsDestination.SubjectDetail(subjectCode = "MA1111"))
		navigator.switchTab(MainSection.PENSUM)

		assertEquals(MainSection.PENSUM, navigator.currentTab)
		assertEquals(
			listOf(SummaryDestination.Summary, PensumDestination.Pensum),
			navigator.backStack
		)

		navigator.switchTab(MainSection.RECORD)

		assertEquals(
			listOf(
				SummaryDestination.Summary,
				RecordDestination.Record,
				SubjectsDestination.SubjectDetail(subjectCode = "MA1111")
			),
			navigator.backStack
		)
	}

	@Test
	fun when_poppedAtNonStartTabRoot_then_returnsToStartTabKeepingParkedStack() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		assertTrue(navigator.pop())
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary), navigator.backStack)

		navigator.switchTab(MainSection.RECORD)

		assertEquals(
			listOf(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
	}

	@Test
	fun when_poppedAtStartTabRoot_then_popIsRejected() {
		val navigator = createNavigator()

		assertFalse(navigator.pop())
		assertEquals(listOf(SummaryDestination.Summary), navigator.backStack)
	}

	@Test
	fun when_authMode_then_backStackIsAuthStack() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)

		assertEquals(listOf(AuthDestination.SignIn), navigator.backStack)

		navigator.push(AuthDestination.UpdatePasswordDialog)

		assertEquals(
			listOf(AuthDestination.SignIn, AuthDestination.UpdatePasswordDialog),
			navigator.backStack
		)
	}

	@Test
	fun when_replacedAllForSignIn_then_stacksResetAndStoresClear() {
		val entryStores = NavEntryStores()
		val resultStore = NavResultStore()
		val navigator = createNavigator(
			currentTab = MainSection.RECORD,
			entryStores = entryStores,
			resultStore = resultStore
		)

		navigator.push(SubjectsDestination.SubjectDetail(subjectCode = "MA1111"))
		val storeOwner = entryStores.viewModelStoreOwner(navigator.currentStoreKey)
		resultStore.publish(receiverStoreKey = navigator.currentStoreKey, result = TestNavResult("x"))

		navigator.replaceAllForSignIn()

		val detailStoreKey = "RECORD:${SubjectsDestination.SubjectDetail(subjectCode = "MA1111")}"

		assertEquals(TuIndiceRootMode.AUTH, navigator.rootMode)
		assertEquals(listOf(AuthDestination.SignIn), navigator.backStack)
		assertEquals(null, resultStore.peek(storeKey = detailStoreKey))
		assertNotSame(storeOwner, entryStores.viewModelStoreOwner(detailStoreKey))
	}

	@Test
	fun when_replacedAllForSignedIn_then_landsOnRequestedSection() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)

		navigator.replaceAllForSignedIn(MainSection.SUMMARY)

		assertEquals(TuIndiceRootMode.SIGNED_IN, navigator.rootMode)
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary), navigator.backStack)
	}

	@Test
	fun when_poppedWithResult_then_resultReachesPreviousEntrySlot() {
		val resultStore = NavResultStore()
		val navigator = createNavigator(currentTab = MainSection.RECORD, resultStore = resultStore)

		navigator.push(RecordDestination.DeleteSyntheticTermConfirmationDialog(termId = "term-1"))

		assertTrue(navigator.popWithResult(TestNavResult("confirmed")))
		assertEquals(
			TestNavResult("confirmed"),
			resultStore.peek(storeKey = "RECORD:${RecordDestination.Record}")
		)
		assertEquals(
			listOf(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
	}

	@Test
	fun when_poppedEntry_then_itsStoreAndPendingResultAreRemoved() {
		val entryStores = NavEntryStores()
		val resultStore = NavResultStore()
		val navigator = createNavigator(
			currentTab = MainSection.RECORD,
			entryStores = entryStores,
			resultStore = resultStore
		)

		navigator.push(SubjectsDestination.SubjectDetail(subjectCode = "MA1111"))

		val detailStoreKey = navigator.currentStoreKey
		val storeOwner = entryStores.viewModelStoreOwner(detailStoreKey)
		resultStore.publish(receiverStoreKey = detailStoreKey, result = TestNavResult("stale"))

		navigator.pop()

		assertEquals(null, resultStore.peek(detailStoreKey))
		assertNotSame(storeOwner, entryStores.viewModelStoreOwner(detailStoreKey))
	}

	@Test
	fun when_sameKeyLivesInTwoTabs_then_storeKeysAreTabScoped() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)
		val subjectDetail = SubjectsDestination.SubjectDetail(subjectCode = "MA1111")

		navigator.push(subjectDetail)
		val recordStoreKey = navigator.currentStoreKey

		navigator.switchTab(MainSection.PENSUM)
		navigator.push(subjectDetail)
		val pensumStoreKey = navigator.currentStoreKey

		assertEquals("RECORD:$subjectDetail", recordStoreKey)
		assertEquals("PENSUM:$subjectDetail", pensumStoreKey)
	}

	@Test
	fun when_switchTabInAuthMode_then_switchIsIgnored() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)

		navigator.switchTab(MainSection.RECORD)

		assertEquals(listOf(AuthDestination.SignIn), navigator.backStack)
	}

	@Test
	fun when_viewModelStoreOwnerRequestedTwice_then_sameOwnerIsReused() {
		val entryStores = NavEntryStores()

		assertSame(
			entryStores.viewModelStoreOwner("SUMMARY:key"),
			entryStores.viewModelStoreOwner("SUMMARY:key")
		)
	}

	@Test
	fun when_removalListenerRegistered_then_itFiresOnRemovalAndUnregisters() {
		val entryStores = NavEntryStores()
		val removedKeys = mutableListOf<String>()
		val unregister = entryStores.addRemovalListener { storeKey -> removedKeys.add(storeKey) }

		entryStores.viewModelStoreOwner("SUMMARY:key")
		entryStores.onRemoved("SUMMARY:key")

		assertEquals(listOf("SUMMARY:key"), removedKeys)

		unregister()
		entryStores.viewModelStoreOwner("RECORD:key")
		entryStores.onRemoved("RECORD:key")

		assertEquals(listOf("SUMMARY:key"), removedKeys)
	}

	@Test
	fun when_popThenSwitchTab_then_theSheetIsClosedAndTheTabChangedInOneObservableChange() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		navigator.openSchedule()
		Snapshot.sendApplyNotifications()

		val observed = mutableListOf<Pair<MainSection, List<NavKey>>>()
		val handle = Snapshot.registerApplyObserver { _, _ ->
			observed += navigator.currentTab to navigator.backStack
		}

		try {
			navigator.popThenSwitchTab(MainSection.EVALUATIONS)

			// Already announced, without anyone flushing the global snapshot: a single mutation.
			val expected: List<NavKey> = listOf(SummaryDestination.Summary, EvaluationsDestination.Evaluations)

			assertEquals(listOf(MainSection.EVALUATIONS to expected), observed)
		} finally {
			handle.dispose()
		}
	}

	@Test
	fun when_popThenSwitchTab_then_theSheetIsNotLeftParkedOnTheTabItCameFrom() {
		val entryStores = NavEntryStores()
		val resultStore = NavResultStore()
		val navigator = createNavigator(
			currentTab = MainSection.RECORD,
			entryStores = entryStores,
			resultStore = resultStore
		)

		navigator.openSchedule()
		val sheetStoreKey = navigator.currentStoreKey
		entryStores.viewModelStoreOwner(sheetStoreKey)
		resultStore.publish(receiverStoreKey = sheetStoreKey, result = TestNavResult("stale"))
		val removedKeys = mutableListOf<String>()
		entryStores.addRemovalListener { storeKey -> removedKeys.add(storeKey) }

		navigator.popThenSwitchTab(MainSection.EVALUATIONS)
		navigator.switchTab(MainSection.RECORD)

		assertEquals(null, resultStore.peek(sheetStoreKey))

		assertEquals(
			listOf(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
		assertEquals(listOf(sheetStoreKey), removedKeys)
	}

	@Test
	fun when_backIsPressedAfterTheJump_then_itReturnsToTheStartTab() {
		val navigator = createNavigator(startTab = MainSection.SUMMARY, currentTab = MainSection.RECORD)

		navigator.openSchedule()
		navigator.popThenSwitchTab(MainSection.EVALUATIONS)

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertTrue(navigator.pop())
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary), navigator.backStack)
	}

	@Test
	fun when_popThenSwitchTab_then_theStartTabIsNotChanged() {
		val navigator = createNavigator(startTab = MainSection.PENSUM, currentTab = MainSection.PENSUM)

		navigator.openSchedule()
		navigator.popThenSwitchTab(MainSection.EVALUATIONS)

		assertEquals(MainSection.PENSUM, navigator.startTab)
		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
	}

	@Test
	fun when_popThenSwitchTabToTheActiveTab_then_onlyTheSheetIsClosed() {
		val navigator = createNavigator(currentTab = MainSection.EVALUATIONS)

		navigator.openSchedule()
		navigator.popThenSwitchTab(MainSection.EVALUATIONS)

		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(
			listOf(SummaryDestination.Summary, EvaluationsDestination.Evaluations),
			navigator.backStack
		)
	}

	@Test
	fun when_popThenSwitchTabWithoutAnEntryOnTop_then_onlyTheTabChanges() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)

		navigator.popThenSwitchTab(MainSection.EVALUATIONS)

		assertEquals(
			listOf(SummaryDestination.Summary, EvaluationsDestination.Evaluations),
			navigator.backStack
		)

		navigator.switchTab(MainSection.RECORD)

		assertEquals(
			listOf(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
	}

	@Test
	fun when_popThenSwitchTabInAuthMode_then_itIsIgnored() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)

		navigator.popThenSwitchTab(MainSection.EVALUATIONS)

		assertEquals(listOf(AuthDestination.SignIn), navigator.backStack)
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
	}

	@Test
	fun when_theScheduleIsOpened_then_itSitsOnTheActiveTabWhicheverItIs() {
		listOf(MainSection.SUMMARY, MainSection.RECORD, MainSection.EVALUATIONS).forEach { section ->
			val navigator = createNavigator(currentTab = section)

			navigator.openSchedule()
			navigator.openSchedule()

			assertEquals(RecordDestination.ScheduleDialog, navigator.currentKey, "on $section")
			assertEquals(section, navigator.currentTab)
			assertEquals(1, navigator.backStack.count { key -> key == RecordDestination.ScheduleDialog }, "on $section")
		}
	}

	@Test
	fun when_theScheduleIsOpenedInAuthMode_then_itIsIgnored() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)

		navigator.openSchedule()

		assertEquals(listOf(AuthDestination.SignIn), navigator.backStack)
	}

	@Test
	fun when_aWeekOfEvaluationsIsShownFromTheSheet_then_theWeekIsRequestedAndOnlyTheSheetIsLeft() {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val navigator = createNavigator(startTab = MainSection.SUMMARY, currentTab = MainSection.RECORD)
		val monday = LocalDate(2026, 10, 5)

		navigator.openSchedule()
		navigator.showEvaluationsWeek(weekFocus = weekFocus, weekStart = monday, evaluationId = EVALUATION_ID)

		assertEquals(EvaluationsWeekFocusRequest(monday, EVALUATION_ID), weekFocus.pending.value)
		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(MainSection.SUMMARY, navigator.startTab)
		assertEquals(
			listOf<NavKey>(SummaryDestination.Summary, EvaluationsDestination.Evaluations),
			navigator.backStack
		)
	}

	@Test
	fun when_aWeekOfEvaluationsIsShownFromSummary_then_theWeekIsRequestedAndOnlyTheTabChanges() {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val navigator = createNavigator(startTab = MainSection.SUMMARY, currentTab = MainSection.SUMMARY)
		val monday = LocalDate(2026, 10, 12)

		navigator.showEvaluationsWeek(weekFocus = weekFocus, weekStart = monday, evaluationId = EVALUATION_ID)

		assertEquals(EvaluationsWeekFocusRequest(monday, EVALUATION_ID), weekFocus.pending.value)
		assertEquals(MainSection.EVALUATIONS, navigator.currentTab)
		assertEquals(MainSection.SUMMARY, navigator.startTab)
		assertEquals(
			listOf<NavKey>(SummaryDestination.Summary, EvaluationsDestination.Evaluations),
			navigator.backStack
		)
	}

	@Test
	fun when_anAttemptIsShownFromSummary_then_itIsRequestedAndOnlyTheTabChanges() {
		val attemptFocus = InMemoryRecordAttemptFocusDataSource()
		val navigator = createNavigator(startTab = MainSection.SUMMARY, currentTab = MainSection.SUMMARY)

		navigator.showRecordAttempt(attemptFocus = attemptFocus, attemptId = ATTEMPT_ID)

		assertEquals(RecordAttemptFocusRequest(ATTEMPT_ID), attemptFocus.pending.value)
		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertEquals(MainSection.SUMMARY, navigator.startTab)
		assertEquals(
			listOf<NavKey>(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
	}

	@Test
	fun when_anAttemptIsShownFromTheSheet_then_theAttemptIsRequestedAndOnlyTheSheetIsLeft() {
		val attemptFocus = InMemoryRecordAttemptFocusDataSource()
		val navigator = createNavigator(startTab = MainSection.SUMMARY, currentTab = MainSection.EVALUATIONS)

		navigator.openSchedule()
		navigator.showRecordAttempt(attemptFocus = attemptFocus, attemptId = ATTEMPT_ID)

		assertEquals(RecordAttemptFocusRequest(ATTEMPT_ID), attemptFocus.pending.value)
		assertEquals(MainSection.RECORD, navigator.currentTab)
		assertEquals(MainSection.SUMMARY, navigator.startTab)
		assertEquals(
			listOf<NavKey>(SummaryDestination.Summary, RecordDestination.Record),
			navigator.backStack
		)
	}

	@Test
	fun when_assistantIsOpened_then_theChatSitsOnTheTabOnScreen() {
		val context = AssistantScreenContext(screen = AssistantScreen.PENSUM)

		MainSection.entries.forEach { section ->
			val navigator = createNavigator(currentTab = section)

			navigator.openAssistant(context)

			assertEquals(section, navigator.currentTab)
			assertEquals(AssistantDestination.Chat(context = context), navigator.currentKey)
		}
	}

	@Test
	fun when_assistantIsOpenedWithoutAContext_then_theChatCarriesNoContext() {
		val navigator = createNavigator(currentTab = MainSection.ABOUT)

		navigator.openAssistant(context = null)

		assertEquals(AssistantDestination.Chat(context = null), navigator.currentKey)
	}

	@Test
	fun when_assistantIsOpenedWithTheSameContextAgain_then_itIsNotPushedTwice() {
		val navigator = createNavigator(currentTab = MainSection.RECORD)
		val context = AssistantScreenContext(screen = AssistantScreen.RECORD)

		navigator.openAssistant(context)
		navigator.openAssistant(context)

		assertEquals(1, navigator.backStack.count { key -> key is AssistantDestination.Chat })
	}

	@Test
	fun when_assistantIsOpenedWithoutASignedInSession_then_nothingIsPushed() {
		val navigator = createNavigator(rootMode = TuIndiceRootMode.AUTH)
		val before = navigator.backStack

		navigator.openAssistant(context = null)

		assertEquals(before, navigator.backStack)
	}

	private companion object {
		const val EVALUATION_ID = "evaluation-1"
		const val ATTEMPT_ID = "attempt-1"
	}
}
