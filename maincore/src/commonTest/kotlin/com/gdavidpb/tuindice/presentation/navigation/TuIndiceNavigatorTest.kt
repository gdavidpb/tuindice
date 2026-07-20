package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.presentation.mapper.toTabRootDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
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
}
