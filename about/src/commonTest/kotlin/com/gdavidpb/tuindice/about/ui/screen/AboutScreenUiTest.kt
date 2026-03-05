package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AboutScreenUiTest {
	@Test
	fun when_stateIsContent_then_rendersContentView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Content(versionText = "1.2.3"),
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
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = {},
				onReportBugClick = {}
			)
		}

		assertNodeVisible(AboutUiTags.OpenCreativeCommons)
	}

	@Test
	fun when_stateIsIdle_then_hidesContentItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Idle,
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
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = {},
				onReportBugClick = {}
			)
		}

		assertNodeHidden(AboutUiTags.OpenCreativeCommons)
	}

	@Test
	fun when_stateIsContentAndItemsTapped_then_propagatesAllCallbacks() = runTuIndiceUiTest {
		var creativeCommonsClicks = 0
		var xClicks = 0
		var githubClicks = 0
		var kotlinClicks = 0
		var composeClicks = 0
		var firebaseClicks = 0
		var koinClicks = 0
		var ktorClicks = 0
		var dstClicks = 0
		var termsClicks = 0
		var privacyClicks = 0
		var shareClicks = 0
		var rateClicks = 0
		var contactClicks = 0
		var reportBugClicks = 0

		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Content(versionText = "1.2.3"),
				onCreativeCommonsClick = { creativeCommonsClicks++ },
				onXClick = { xClicks++ },
				onGithubClick = { githubClicks++ },
				onKotlinClick = { kotlinClicks++ },
				onComposeClick = { composeClicks++ },
				onFirebaseClick = { firebaseClicks++ },
				onKoinClick = { koinClicks++ },
				onKtorClick = { ktorClicks++ },
				onDstClick = { dstClicks++ },
				onTermsAndConditionsClick = { termsClicks++ },
				onPrivacyPolicyClick = { privacyClicks++ },
				onShareAppClick = { shareClicks++ },
				onRateOnPlayStoreClick = { rateClicks++ },
				onContactDeveloperClick = { contactClicks++ },
				onReportBugClick = { reportBugClicks++ }
			)
		}

		onNodeWithTag(AboutUiTags.OpenCreativeCommons).performClick()
		onNodeWithTag(AboutUiTags.OpenTerms).performClick()
		onNodeWithTag(AboutUiTags.OpenPrivacy).performClick()
		onNodeWithTag(AboutUiTags.OpenX).performClick()
		onNodeWithTag(AboutUiTags.ShareApp).performClick()
		onNodeWithTag(AboutUiTags.RateOnStore).performClick()

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

		assertEquals(1, creativeCommonsClicks)
		assertEquals(1, xClicks)
		assertEquals(1, githubClicks)
		assertEquals(1, kotlinClicks)
		assertEquals(1, composeClicks)
		assertEquals(1, firebaseClicks)
		assertEquals(1, koinClicks)
		assertEquals(1, ktorClicks)
		assertEquals(1, dstClicks)
		assertEquals(1, termsClicks)
		assertEquals(1, privacyClicks)
		assertEquals(1, shareClicks)
		assertEquals(1, rateClicks)
		assertEquals(1, contactClicks)
		assertEquals(1, reportBugClicks)
	}
}
