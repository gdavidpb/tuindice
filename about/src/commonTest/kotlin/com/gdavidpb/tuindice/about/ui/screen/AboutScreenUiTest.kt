package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.testing.noAboutActions
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
	fun when_stateIsContent_then_rendersTheVersionAndTheLicense() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Content(versionText = "9.9.9"),
				actions = noAboutActions()
			)
		}

		assertNodeVisible(AboutUiTags.ContentContainer)
		assertNodeVisible(AboutUiTags.OpenCreativeCommons)
		onNodeWithText("9.9.9", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_stateIsIdle_then_hidesContentItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Idle,
				actions = noAboutActions()
			)
		}

		assertNodeHidden(AboutUiTags.OpenCreativeCommons)
	}

	@Test
	fun when_stateIsContentAndItemsTapped_then_propagatesAllCallbacks() = runTuIndiceUiTest {
		val clicks = mutableListOf<String>()

		setTuIndiceTestContent {
			AboutScreen(
				state = About.State.Content(versionText = "1.2.3"),
				actions = noAboutActions().copy(
					onCreativeCommonsClick = { clicks += "cc" },
					onGithubClick = { clicks += "github" },
					onContactDeveloperClick = { clicks += "contact" },
					onKotlinClick = { clicks += "kotlin" },
					onComposeClick = { clicks += "compose" },
					onFirebaseClick = { clicks += "firebase" },
					onKoinClick = { clicks += "koin" },
					onKtorClick = { clicks += "ktor" },
					onDstClick = { clicks += "dst" }
				)
			)
		}

		val tapped = listOf(
			AboutUiTags.OpenCreativeCommons to "cc",
			AboutUiTags.OpenGithub to "github",
			AboutUiTags.ContactDeveloper to "contact",
			AboutUiTags.OpenKotlin to "kotlin",
			AboutUiTags.OpenCompose to "compose",
			AboutUiTags.OpenFirebase to "firebase",
			AboutUiTags.OpenKoin to "koin",
			AboutUiTags.OpenKtor to "ktor",
			AboutUiTags.OpenDst to "dst"
		)

		tapped.forEach { (tag, _) ->
			onNodeWithTag(AboutUiTags.ContentContainer).performScrollToNode(hasTestTag(tag))
			onNodeWithTag(tag).performClick()
		}

		assertEquals(tapped.map { (_, name) -> name }, clicks)
	}
}
