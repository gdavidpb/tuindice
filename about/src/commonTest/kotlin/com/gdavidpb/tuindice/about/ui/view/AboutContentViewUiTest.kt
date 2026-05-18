package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AboutContentViewUiTest {
	@Test
	fun when_primaryActionsTapped_then_dispatchesCallbacks() = runTuIndiceUiTest {
		var creativeCommonsClicks = 0
		var termsClicks = 0
		var privacyClicks = 0
		var supportClicks = 0
		var xClicks = 0
		var shareClicks = 0
		var rateClicks = 0

		setTuIndiceTestContent {
			AboutContentView(
				state = About.State.Content(versionText = "1.2.3"),
				onCreativeCommonsClick = { creativeCommonsClicks++ },
				onXClick = { xClicks++ },
				onGithubClick = {},
				onKotlinClick = {},
				onComposeClick = {},
				onFirebaseClick = {},
				onKoinClick = {},
				onKtorClick = {},
				onDstClick = {},
				onTermsAndConditionsClick = { termsClicks++ },
				onPrivacyPolicyClick = { privacyClicks++ },
				onSupportClick = { supportClicks++ },
				onShareAppClick = { shareClicks++ },
				onRateOnPlayStoreClick = { rateClicks++ },
				onContactDeveloperClick = {},
				onReportBugClick = {}
			)
		}

		assertNodeVisible(AboutUiTags.OpenCreativeCommons)
		assertNodeVisible(AboutUiTags.OpenTerms)
		assertNodeVisible(AboutUiTags.OpenPrivacy)
		assertNodeVisible(AboutUiTags.OpenSupport)
		assertNodeVisible(AboutUiTags.OpenX)
		assertNodeVisible(AboutUiTags.ShareApp)
		assertNodeVisible(AboutUiTags.RateOnStore)

		onNodeWithTag(AboutUiTags.OpenCreativeCommons).performClick()
		onNodeWithTag(AboutUiTags.OpenTerms).performClick()
		onNodeWithTag(AboutUiTags.OpenPrivacy).performClick()
		onNodeWithTag(AboutUiTags.OpenSupport).performClick()
		onNodeWithTag(AboutUiTags.OpenX).performClick()
		onNodeWithTag(AboutUiTags.ShareApp).performClick()
		onNodeWithTag(AboutUiTags.RateOnStore).performClick()

		assertEquals(1, creativeCommonsClicks)
		assertEquals(1, termsClicks)
		assertEquals(1, privacyClicks)
		assertEquals(1, supportClicks)
		assertEquals(1, xClicks)
		assertEquals(1, shareClicks)
		assertEquals(1, rateClicks)
	}

	@Test
	fun when_secondaryActionsTapped_then_dispatchesCallbacks() = runTuIndiceUiTest {
		var githubClicks = 0
		var contactClicks = 0
		var reportBugClicks = 0
		var kotlinClicks = 0
		var composeClicks = 0
		var firebaseClicks = 0
		var koinClicks = 0
		var ktorClicks = 0
		var dstClicks = 0

		setTuIndiceTestContent {
			AboutContentView(
				state = About.State.Content(versionText = "1.2.3"),
				onCreativeCommonsClick = {},
				onXClick = {},
				onGithubClick = { githubClicks++ },
				onKotlinClick = { kotlinClicks++ },
				onComposeClick = { composeClicks++ },
				onFirebaseClick = { firebaseClicks++ },
				onKoinClick = { koinClicks++ },
				onKtorClick = { ktorClicks++ },
				onDstClick = { dstClicks++ },
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				onSupportClick = {},
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = { contactClicks++ },
				onReportBugClick = { reportBugClicks++ }
			)
		}

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenGithub))
		onNodeWithTag(AboutUiTags.OpenGithub).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.ContactDeveloper))
		onNodeWithTag(AboutUiTags.ContactDeveloper).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.ReportBug))
		onNodeWithTag(AboutUiTags.ReportBug).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenKotlin))
		onNodeWithTag(AboutUiTags.OpenKotlin).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenCompose))
		onNodeWithTag(AboutUiTags.OpenCompose).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenFirebase))
		onNodeWithTag(AboutUiTags.OpenFirebase).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenKoin))
		onNodeWithTag(AboutUiTags.OpenKoin).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenKtor))
		onNodeWithTag(AboutUiTags.OpenKtor).performClick()

		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenDst))
		onNodeWithTag(AboutUiTags.OpenDst).performClick()

		assertEquals(1, githubClicks)
		assertEquals(1, contactClicks)
		assertEquals(1, reportBugClicks)
		assertEquals(1, kotlinClicks)
		assertEquals(1, composeClicks)
		assertEquals(1, firebaseClicks)
		assertEquals(1, koinClicks)
		assertEquals(1, ktorClicks)
		assertEquals(1, dstClicks)
	}

	@Test
	fun when_stateContainsVersion_then_displaysVersionDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutContentView(
				state = About.State.Content(versionText = "9.9.9"),
				onCreativeCommonsClick = {},
				onXClick = {},
				onGithubClick = {},
				onKotlinClick = {},
				onComposeClick = {},
				onFirebaseClick = {},
				onKoinClick = {},
				onKtorClick = {},
				onDstClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				onSupportClick = {},
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = {},
				onReportBugClick = {}
			)
		}

		onNodeWithText("9.9.9", substring = true).assertIsDisplayed()
	}
}
