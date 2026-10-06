package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.time.Duration.Companion.milliseconds

private const val IOS_SHEET_SWIPE_MS = 600L

val summaryProfilePicture = scenario(
	"summary-profile-picture",
	"summary",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"summary.Summary.OpenProfilePictureSettings",
		"summary.Summary.PickProfilePicture",
		"summary.Summary.TakeProfilePicture",
		"summary.Summary.RemoveProfilePicture",
		"summary.Summary.ConfirmRemoveProfilePicture"
	)
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(SummaryUiTags.ProfilePictureContainer, Within.Assert)
	waitVisible(SummaryUiTags.ProfilePictureEditButton, Within.Assert)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePicturePickAction, Within.Action)
	waitVisible(SummaryUiTags.ProfilePictureTakeAction, Within.Assert)
	waitVisible(SummaryUiTags.ProfilePictureRemoveAction, Within.Assert)
	tap(SummaryUiTags.ProfilePictureRemoveAction)
	waitVisible(SummaryUiTags.RemoveProfilePictureMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePictureRemoveAction, Within.Action)
	tap(SummaryUiTags.ProfilePictureRemoveAction)
	waitVisible(SummaryUiTags.RemoveProfilePictureMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(SummaryUiTags.ProfilePictureContainer, Within.Assert)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePicturePickAction, Within.Action)
	waitGone(SummaryUiTags.ProfilePictureRemoveAction, Within.Assert)
	onPlatform(Platform.Android) {
		back()
	}
	onPlatform(Platform.Ios) {
		swipeScreen(SwipeDirection.Down, IOS_SHEET_SWIPE_MS.milliseconds)
	}
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}
