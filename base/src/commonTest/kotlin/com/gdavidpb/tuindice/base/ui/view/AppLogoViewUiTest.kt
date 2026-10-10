package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppLogoViewUiTest {
	@Test
	fun when_contentDescriptionProvided_then_logoIsAnnouncedAsAnImage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AppLogoView(
				modifier = Modifier
					.testTag(LogoTag)
					.size(96.dp),
				contentDescription = "Logo de TuIndice"
			)
		}

		onNodeWithContentDescription("Logo de TuIndice").assertIsDisplayed()
		onNodeWithTag(LogoTag)
			.assertContentDescriptionEquals("Logo de TuIndice")
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
			.assertWidthIsEqualTo(96.dp)
			.assertHeightIsEqualTo(96.dp)
	}

	@Test
	fun when_contentDescriptionIsOmitted_then_logoIsDecorative() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AppLogoView(modifier = Modifier.testTag(LogoTag))
		}

		assertNodeVisible(LogoTag)
		onNodeWithTag(LogoTag)
			.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
			.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
	}

	@Test
	fun when_noSizeIsRequested_then_logoTakesTheSizeOfItsDrawable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AppLogoView(modifier = Modifier.testTag(LogoTag))
		}

		// ic_app_logo declares 100dp x 100dp: an empty or missing drawable would measure 0.
		onNodeWithTag(LogoTag)
			.assertWidthIsEqualTo(100.dp)
			.assertHeightIsEqualTo(100.dp)
	}

	private companion object {
		const val LogoTag = "app_logo"
	}
}
