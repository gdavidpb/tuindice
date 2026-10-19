package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.testing.ANA_PROFILE_IDENTITY
import com.gdavidpb.tuindice.about.testing.noSettingsActions
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SettingsContentViewUiTest {
	@Test
	fun when_primaryActionsTapped_then_dispatchesCallbacks() = runTuIndiceUiTest {
		var termsClicks = 0
		var privacyClicks = 0
		var supportClicks = 0
		var reportBugClicks = 0
		var xClicks = 0
		var shareClicks = 0
		var rateClicks = 0
		var aboutClicks = 0

		setTuIndiceTestContent {
			SettingsContentView(
				state = content(),
				actions = noSettingsActions().copy(
					onXClick = { xClicks++ },
					onTermsAndConditionsClick = { termsClicks++ },
					onPrivacyPolicyClick = { privacyClicks++ },
					onSupportClick = { supportClicks++ },
					onReportBugClick = { reportBugClicks++ },
					onShareAppClick = { shareClicks++ },
					onRateOnPlayStoreClick = { rateClicks++ },
					onAboutClick = { aboutClicks++ }
				),
				profilePhoto = {}
			)
		}

		listOf(
			SettingsUiTags.OpenPrivacy,
			SettingsUiTags.OpenTerms,
			SettingsUiTags.OpenSupport,
			SettingsUiTags.ReportBug,
			SettingsUiTags.ShareApp,
			SettingsUiTags.RateOnStore,
			SettingsUiTags.OpenX,
			SettingsUiTags.OpenAbout
		).forEach { tag ->
			scrollTo(tag)
			onNodeWithTag(tag).performClick()
		}

		assertEquals(1, privacyClicks)
		assertEquals(1, termsClicks)
		assertEquals(1, supportClicks)
		assertEquals(1, reportBugClicks)
		assertEquals(1, shareClicks)
		assertEquals(1, rateClicks)
		assertEquals(1, xClicks)
		assertEquals(1, aboutClicks)
	}

	@Test
	fun when_usageDataSwitchTapped_then_requestsTheToggledChoice() = runTuIndiceUiTest {
		val changes = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			SettingsContentView(
				state = content(),
				actions = noSettingsActions().copy(onUsageDataCollectionEnabledChange = { enabled -> changes += enabled }),
				profilePhoto = {}
			)
		}

		scrollTo(SettingsUiTags.UsageDataConsentToggle)
		onNodeWithTag(SettingsUiTags.UsageDataConsentToggle).performClick()

		assertEquals(listOf(true), changes)
	}

	@Test
	fun when_signOutTapped_then_dispatchesTheCallback() = runTuIndiceUiTest {
		var signOutClicks = 0

		setTuIndiceTestContent {
			SettingsContentView(
				state = content(),
				actions = noSettingsActions().copy(onSignOutClick = { signOutClicks++ }),
				profilePhoto = {}
			)
		}

		scrollTo(SettingsUiTags.SignOut)
		onNodeWithTag(SettingsUiTags.SignOut).performClick()

		assertEquals(1, signOutClicks)
	}

	@Test
	fun when_theAboutRowIsShown_then_itsSubtitleSaysTheVersion() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsContentView(
				state = content(versionText = "9.9.9"),
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		scrollTo(SettingsUiTags.OpenAbout)
		onNodeWithText("Versión 9.9.9, licencias y créditos", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_theStudentIsKnown_then_theIdentityCardIsFirstAndShowsThePhotoTheHostDraws() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsContentView(
				state = content(identity = ANA_PROFILE_IDENTITY),
				actions = noSettingsActions(),
				profilePhoto = { identity -> Text(text = "photo-of-${identity.name}") }
			)
		}

		assertNodeVisible(SettingsUiTags.IdentityCard)
		onNodeWithText("photo-of-Ana Diaz").assertIsDisplayed()
		onNodeWithText("Ana Diaz").assertIsDisplayed()
		onNodeWithText("Carnet 20261234").assertIsDisplayed()
	}

	@Test
	fun when_theIdentityCardIsTapped_then_nothingHappens() = runTuIndiceUiTest {
		val clicks = mutableListOf<String>()

		setTuIndiceTestContent {
			SettingsContentView(
				state = content(identity = ANA_PROFILE_IDENTITY),
				actions = noSettingsActions().copy(
					onAboutClick = { clicks += "about" },
					onSignOutClick = { clicks += "sign-out" },
					onSupportClick = { clicks += "support" },
					onPrivacyPolicyClick = { clicks += "privacy" }
				),
				profilePhoto = {}
			)
		}

		onNodeWithTag(SettingsUiTags.IdentityCard).performClick()

		assertTrue(clicks.isEmpty())
	}

	@Test
	fun when_theStudentIsNotKnown_then_theCardIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsContentView(
				state = content(identity = null),
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		assertTrue(onAllNodesWithTag(SettingsUiTags.IdentityCard).fetchSemanticsNodes().isEmpty())
		assertNodeVisible(SettingsUiTags.OpenPrivacy)
	}

	@Test
	fun when_theAssistantIsAvailable_then_theGroupsComeInOrderAndSignOutIsLast() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsContentView(
				state = content(assistantAvailable = true),
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		val orderedTags = listOf(
			SettingsUiTags.AssistantConsentSwitch,
			SettingsUiTags.AssistantMemoryItem,
			SettingsUiTags.AssistantConversationsItem,
			SettingsUiTags.UsageDataConsentToggle,
			SettingsUiTags.OpenPrivacy,
			SettingsUiTags.OpenTerms,
			SettingsUiTags.OpenSupport,
			SettingsUiTags.ReportBug,
			SettingsUiTags.ShareApp,
			SettingsUiTags.RateOnStore,
			SettingsUiTags.OpenX,
			SettingsUiTags.OpenAbout,
			SettingsUiTags.SignOut
		)
		// The column is not lazy, so every row is laid out before any scrolling, and its position says where it sits (the
		// bounds of a row outside the screen are clipped, its position is not).
		val tops = orderedTags.map { tag -> onNodeWithTag(tag).fetchSemanticsNode().positionInRoot.y }

		assertEquals(tops.sorted(), tops)
		assertEquals(
			4,
			onAllNodesWithTag(SettingsUiTags.SectionTitle).fetchSemanticsNodes().size
		)
	}

	@Test
	fun when_theAssistantIsNotAvailable_then_itsGroupIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsContentView(
				state = content(assistantAvailable = false),
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		assertEquals(3, onAllNodesWithTag(SettingsUiTags.SectionTitle).fetchSemanticsNodes().size)
		assertTrue(onAllNodesWithTag(SettingsUiTags.AssistantConsentSwitch).fetchSemanticsNodes().isEmpty())
	}

	private fun ComposeUiTest.scrollTo(tag: String) {
		onNodeWithTag(SettingsUiTags.ContentContainer).performScrollToNode(hasTestTag(tag))
	}

	private fun content(
		identity: ProfileIdentity? = null,
		versionText: String = "1.2.3",
		assistantAvailable: Boolean = false
	) = Settings.State.Content(
		identity = identity,
		versionText = versionText,
		usageDataCollectionEnabled = false,
		assistantAvailable = assistantAvailable,
		assistantConsentGranted = false
	)
}
