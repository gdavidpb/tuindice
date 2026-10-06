package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.presentation.mapper.resolveSyncAttention
import com.gdavidpb.tuindice.summary.presentation.model.SyncAttention
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SYNC_STATUS_TEXT
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryItemsFor
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryContentViewUiTest {
	@Test
	fun when_editProfilePictureTapped_then_invokesEditCallback() = runTuIndiceUiTest {
		var editClicks = 0
		val contentState = summaryContentState()
		val items = summaryItemsFor(contentState)

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
				summaryItems = items,
				onEditProfilePictureClick = { editClicks++ },
				onStatusIconClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.GradeText)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeVisible(SummaryUiTags.CareerText)
		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.ItemsList)
		assertNodeVisible(SummaryUiTags.statusCard(0))
		assertNodeVisible(SummaryUiTags.statusCard(1))

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, editClicks)
	}

	@Test
	fun when_userRefreshIsRunning_then_profilePictureEditIsDisabled() = runTuIndiceUiTest {
		var editClicks = 0
		val contentState = summaryContentState(isUserRefreshing = true)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = { editClicks++ },
					onStatusIconClick = {}
				)
			}
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).assertIsNotEnabled()
		assertEquals(0, editClicks)
	}

	@Test
	fun when_summaryHasNoItems_then_rendersHeaderSectionWithoutStatusCards() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
				summaryItems = emptyList(),
				onEditProfilePictureClick = {},
				onStatusIconClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeHidden(SummaryUiTags.statusCard(0))
	}

	@Test
	fun when_summaryContentIsVisible_then_displaysStatusIconAndText() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		onNodeWithTag(
			SummaryUiTags.StatusIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.StatusText)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithText(DEFAULT_SYNC_STATUS_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_syncIsRunning_then_statusIconRemainsVisibleBesideLastSync() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
					isSyncing = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = {}
				)
			}
		}

		onNodeWithTag(
			SummaryUiTags.StatusIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithText(DEFAULT_SYNC_STATUS_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_syncIsRunningWithHealthyStatus_then_statusIconUsesLoadingRotation() {
		assertEquals(
			expected = -180f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = true,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_userRefreshIsRunning_then_statusIconDoesNotUseLoadingRotation() {
		assertEquals(
			expected = 0f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = false,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_syncStatusIsNotHealthyButRefreshing_then_statusIconUsesLoadingRotation() {
		listOf(
			SyncStatus.Failed,
			SyncStatus.Unavailable,
			SyncStatus.OutdatedCredentials
		).forEach { syncStatus ->
			assertEquals(
				expected = Icons.Outlined.Sync,
				actual = syncStatusIcon(
					syncAttention = resolveSyncAttention(syncStatus, SyncReport.success()),
					isStatusRefreshing = true
				)
			)
			assertEquals(
				expected = -180f,
				actual = syncStatusIconRotation(
					isStatusRefreshing = true,
					currentRotation = -180f
				)
			)
		}
	}

	@Test
	fun when_syncReportHasUnavailableSource_then_statusShowsHaloAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.partialEnrollmentUnavailable()),
					showSyncAttentionHalo = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = { statusIconClicks++ }
				)
			}
		}

		assertNodeVisible(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_syncReportHasUnavailableSourceButSyncIsRunning_then_statusUsesLoadingWithoutHalo() =
		runTuIndiceUiTest {
			val contentState = summaryContentState()

			setTuIndiceTestContent {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
					SummaryContentView(
						state = contentState,
						syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.partialEnrollmentUnavailable()),
						isSyncing = true,
						showSyncAttentionHalo = true,
						summaryItems = summaryItemsFor(contentState),
						onEditProfilePictureClick = {},
						onStatusIconClick = {}
					)
				}
			}

			assertNodeHidden(SummaryUiTags.StatusIconHalo)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		}

	@Test
	fun when_statusIsNotRefreshing_then_statusIconDoesNotUseLoadingRotationOrOverrideErrorIcon() {
		assertEquals(
			expected = Icons.Outlined.SyncProblem,
			actual = syncStatusIcon(
				syncAttention = SyncAttention.Problem,
				isStatusRefreshing = false
			)
		)
		assertEquals(
			expected = Icons.Outlined.SyncProblem,
			actual = syncStatusIcon(
				syncAttention = resolveSyncAttention(
					SyncStatus.Healthy,
					SyncReport.partialEnrollmentUnavailable()
				),
				isStatusRefreshing = false
			)
		)
		assertEquals(
			expected = 0f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = false,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_syncHasFailedButSyncIsRunning_then_statusShowsLoadingAndDetailsAreTemporarilyDisabled() =
		runTuIndiceUiTest {
			val contentState = summaryContentState()
			var statusIconClicks = 0

			setTuIndiceTestContent {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
					SummaryContentView(
						state = contentState,
						syncAttention = resolveSyncAttention(SyncStatus.Failed, SyncReport.success()),
						isSyncing = true,
						summaryItems = summaryItemsFor(contentState),
						onEditProfilePictureClick = {},
						onStatusIconClick = { statusIconClicks++ }
					)
				}
			}

			assertNodeVisible(SummaryUiTags.StatusRow)
			assertNodeVisible(SummaryUiTags.StatusIconButton)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
			onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(DEFAULT_SYNC_STATUS_TEXT)
			assertEquals(0, statusIconClicks)
		}

	@Test
	fun when_userRefreshIsRunningAndSyncHasFailed_then_statusKeepsErrorAndCanOpenDetails() =
		runTuIndiceUiTest {
			val contentState = summaryContentState(isUserRefreshing = true)
			var statusIconClicks = 0

			setTuIndiceTestContent {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
					SummaryContentView(
						state = contentState,
						syncAttention = resolveSyncAttention(SyncStatus.Failed, SyncReport.success()),
						isSyncing = false,
						summaryItems = summaryItemsFor(contentState),
						onEditProfilePictureClick = {},
						onStatusIconClick = { statusIconClicks++ }
					)
				}
			}

			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
			onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(DEFAULT_SYNC_STATUS_TEXT)
			onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
			assertEquals(1, statusIconClicks)
		}

	@Test
	fun when_syncHasFailed_then_statusKeepsLastSyncAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.Failed, SyncReport.success()),
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(DEFAULT_SYNC_STATUS_TEXT)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_syncIsUnavailable_then_statusKeepsLastSyncAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.Unavailable, SyncReport.success()),
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(DEFAULT_SYNC_STATUS_TEXT)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_syncStatusIsOutdatedCredentials_then_statusKeepsLastSyncAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncAttention = resolveSyncAttention(SyncStatus.OutdatedCredentials, SyncReport.success()),
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(DEFAULT_SYNC_STATUS_TEXT)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_enrollmentIsAnnulled_then_statusAsksForNothingAndCannotOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, annulledReport()),
					// Even told to pulse, a row with no problem to announce has no halo.
					showSyncAttentionHalo = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = { statusIconClicks++ }
				)
			}
		}

		// Record and Evaluations carry the annulment notice; the sync itself had no problem.
		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		assertNodeVisible(SummaryUiTags.StatusIcon, useUnmergedTree = true)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(0, statusIconClicks)
	}

	@Test
	fun when_enrollmentIsNotEnrolled_then_statusAsksForNothingAndCannotOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(
						SyncStatus.Healthy,
						enrollmentReport(SyncSourceStatus.NotEnrolled)
					),
					showSyncAttentionHalo = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = { statusIconClicks++ }
				)
			}
		}

		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(0, statusIconClicks)
	}

	@Test
	fun when_newStudentHasNoRecord_then_statusAsksForNothingAndCannotOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncAttention = resolveSyncAttention(
						SyncStatus.NewStudentNoRecord,
						SyncReport.failedRecordUnavailable()
					),
					showSyncAttentionHalo = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = { statusIconClicks++ }
				)
			}
		}

		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(0, statusIconClicks)
	}

	@Test
	fun when_enrollmentIsUnavailableAndAnAnnulmentIsCarriedOver_then_statusShowsTheProblem() =
		runTuIndiceUiTest {
			val contentState = summaryContentState()
			var statusIconClicks = 0

			setTuIndiceTestContent {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
					SummaryContentView(
						state = contentState,
						syncAttention = resolveSyncAttention(
							SyncStatus.Healthy,
							SyncReport.partialEnrollmentUnavailable().carryingEnrollmentFrom(annulledReport())
						),
						showSyncAttentionHalo = true,
						summaryItems = summaryItemsFor(contentState),
						onEditProfilePictureClick = {},
						onStatusIconClick = { statusIconClicks++ }
					)
				}
			}

			// The source that could not be read is a problem of the sync, annulment or not.
			assertNodeVisible(SummaryUiTags.StatusIconHalo)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
			onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
			assertEquals(1, statusIconClicks)
		}

	@Test
	fun when_universityReportsAnEnrollmentState_then_iconIsThePlainSyncOne() {
		listOf(
			resolveSyncAttention(SyncStatus.Healthy, annulledReport()),
			resolveSyncAttention(SyncStatus.Healthy, enrollmentReport(SyncSourceStatus.NotEnrolled)),
			resolveSyncAttention(SyncStatus.NewStudentNoRecord, SyncReport.failedRecordUnavailable())
		).forEach { syncAttention ->
			assertEquals(
				expected = Icons.Outlined.Sync,
				actual = syncStatusIcon(syncAttention = syncAttention, isStatusRefreshing = false)
			)
		}
	}

	private fun annulledReport(): SyncReport {
		return enrollmentReport(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
	}

	private fun enrollmentReport(
		enrollment: SyncSourceStatus,
		situation: EnrollmentSituation? = null
	): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(enrollment, situation)
			)
		)
	}
}
