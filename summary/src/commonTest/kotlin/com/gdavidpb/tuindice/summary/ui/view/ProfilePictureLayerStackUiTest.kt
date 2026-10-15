package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.il_profile_picture_placeholder_owl
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfilePictureLayerStackUiTest {
	@Test
	fun when_thereAreNoLayers_then_theCircleCarriesItsDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureLayerStack(
				layers = emptyList(),
				imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
				placeholderPainter = painterResource(Res.drawable.il_profile_picture_placeholder_owl),
				contentDescription = DESCRIPTION
			)
		}

		onAllNodesWithContentDescription(DESCRIPTION).assertCountEquals(1)
	}

	@Test
	fun when_aLayerIsGiven_then_itIsDrawnUnderItsTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val context = LocalPlatformContext.current

			ProfilePictureLayerStack(
				layers = listOf(
					ProfilePictureLayer(
						tag = LAYER_TAG,
						request = ImageRequest.Builder(context).data(UNREACHABLE_URL).build(),
						showsPlaceholder = true
					)
				),
				imageLoader = SingletonImageLoader.get(context),
				placeholderPainter = painterResource(Res.drawable.il_profile_picture_placeholder_owl),
				contentDescription = DESCRIPTION
			)
		}

		onAllNodesWithTag(LAYER_TAG).assertCountEquals(1)
	}

	private companion object {
		const val DESCRIPTION = "Foto de perfil"
		const val LAYER_TAG = "profile-picture-layer-test"
		const val UNREACHABLE_URL = "https://invalid.example/profile.png"
	}
}
