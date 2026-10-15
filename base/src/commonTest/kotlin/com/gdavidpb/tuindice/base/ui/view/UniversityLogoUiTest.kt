package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class UniversityLogoUiTest {
	@Test
	fun when_theLogoIsPainted_then_itIsDisplayedAtTheSizeOfItsDrawable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Image(
				painter = universityLogoPainter(),
				contentDescription = null,
				modifier = Modifier.testTag(LogoTag)
			)
		}

		// ic_usb declares 48dp x 48dp: an empty or missing drawable would measure 0 and never be displayed.
		assertNodeVisible(LogoTag)
		onNodeWithTag(LogoTag)
			.assertWidthIsEqualTo(48.dp)
			.assertHeightIsEqualTo(48.dp)
	}

	private companion object {
		const val LogoTag = "university_logo"
	}
}
