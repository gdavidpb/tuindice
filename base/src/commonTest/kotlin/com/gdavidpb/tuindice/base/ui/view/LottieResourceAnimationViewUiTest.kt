package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieAnimationState
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.base.generated.resources.Res
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalResourceApi::class)
class LottieResourceAnimationViewUiTest {
	@Test
	fun when_testTagProvided_then_tagsTheAnimationAndReadsItsBytesOnce() = runTuIndiceUiTest {
		val animationSize = mutableStateOf(120.dp)
		var readCount = 0

		setTuIndiceTestContent {
			LottieResourceAnimationView(
				readBytes = {
					readCount++
					Res.readBytes(ANIMATION_PATH)
				},
				modifier = Modifier.size(animationSize.value),
				testTag = AnimationTag
			)
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readCount == 1 }
		assertNodeVisible(AnimationTag)
		onNodeWithTag(AnimationTag)
			.assertWidthIsEqualTo(120.dp)
			.assertHeightIsEqualTo(120.dp)

		runOnIdle {
			animationSize.value = 80.dp
		}
		waitForIdle()

		// Recomposing with another modifier resizes the node without loading the file again.
		onNodeWithTag(AnimationTag)
			.assertWidthIsEqualTo(80.dp)
			.assertHeightIsEqualTo(80.dp)
		assertEquals(1, readCount)
	}

	@Test
	fun when_testTagIsOmitted_then_keepsTheCallerModifierAndStillLoadsTheAnimation() = runTuIndiceUiTest {
		var readCount = 0

		setTuIndiceTestContent {
			LottieResourceAnimationView(
				readBytes = {
					readCount++
					Res.readBytes(ANIMATION_PATH)
				},
				modifier = Modifier
					.testTag(HostTag)
					.size(64.dp)
			)
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readCount == 1 }
		assertNodeVisible(HostTag)
		assertNodeHidden(AnimationTag)
		onNodeWithTag(HostTag)
			.assertWidthIsEqualTo(64.dp)
			.assertHeightIsEqualTo(64.dp)
	}

	@Test
	fun when_bytesCannotBeRead_then_keepsTheAnimationNodeInPlace() = runTuIndiceUiTest {
		var readCount = 0

		setTuIndiceTestContent {
			LottieResourceAnimationView(
				readBytes = {
					readCount++
					error("animation file is missing")
				},
				modifier = Modifier.size(64.dp),
				testTag = AnimationTag
			)
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readCount >= 1 }
		waitForIdle()

		// A failed load leaves an empty painter, not a crash: the layout slot is still there.
		assertNodeVisible(AnimationTag)
		onNodeWithTag(AnimationTag)
			.assertWidthIsEqualTo(64.dp)
			.assertHeightIsEqualTo(64.dp)
	}

	@Test
	fun when_animationsAreDisabled_then_theAnimationNodeStaysInPlace() = runTuIndiceUiTest {
		var readCount = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				LottieResourceAnimationView(
					readBytes = {
						readCount++
						Res.readBytes(ANIMATION_PATH)
					},
					modifier = Modifier.size(96.dp),
					testTag = AnimationTag
				)
			}
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readCount == 1 }
		advanceAnimationsBy(5_000)

		// A frozen animation still loads and still occupies its slot; only the playback stops.
		assertNodeVisible(AnimationTag)
		onNodeWithTag(AnimationTag)
			.assertWidthIsEqualTo(96.dp)
			.assertHeightIsEqualTo(96.dp)
		assertEquals(1, readCount)
	}

	@Test
	fun when_animationsAreDisabled_then_theAnimationStaysAtItsFirstFrame() = runTuIndiceUiTest {
		val progress = playedProgress(animationsEnabled = false)

		assertEquals(0f, progress, "the animation must not advance")
	}

	@Test
	fun when_animationsAreEnabled_then_theAnimationAdvances() = runTuIndiceUiTest {
		val progress = playedProgress(animationsEnabled = true)

		assertTrue(progress > 0f, "the animation should have advanced, progress was $progress")
	}

	// Plays the real animation (it lasts longer than the time advanced) and reads where it got to.
	private fun ComposeUiTest.playedProgress(animationsEnabled: Boolean): Float {
		var state: LottieAnimationState? = null

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animationsEnabled) {
				val composition by rememberLottieComposition {
					LottieCompositionSpec.JsonString(Res.readBytes(ANIMATION_PATH).decodeToString())
				}

				state = rememberLottieAnimationState(composition = composition, iterations = Compottie.IterateForever)
			}
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { state?.composition != null }
		advanceAnimationsBy(2_000)

		return requireNotNull(state).progress
	}

	private companion object {
		const val AnimationTag = "lottie_animation"
		const val HostTag = "lottie_host"
		const val ANIMATION_PATH = "files/an_empty.json"
		const val LOAD_TIMEOUT_MILLIS = 5_000L
	}
}
