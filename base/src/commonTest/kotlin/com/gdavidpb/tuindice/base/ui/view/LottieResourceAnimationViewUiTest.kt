package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.base.generated.resources.Res
import kotlin.test.Test
import kotlin.test.assertEquals

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
	fun when_animationsAreDisabledInTheComposition_then_theViewDecidesNotToPlay() = runTuIndiceUiTest {
		val decisions = mutableListOf<LottiePlayback>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				ObservedLottieResourceAnimationView(
					readBytes = { Res.readBytes(ANIMATION_PATH) },
					modifier = Modifier.size(96.dp),
					testTag = AnimationTag,
					iterations = 2,
					onPlayback = { decisions += it }
				)
			}
		}

		waitForIdle()

		assertEquals(LottiePlayback(isPlaying = false, iterations = 2), decisions.last())
	}

	@Test
	fun when_animationsAreEnabledInTheComposition_then_theViewDecidesToPlay() = runTuIndiceUiTest {
		val decisions = mutableListOf<LottiePlayback>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides true) {
				ObservedLottieResourceAnimationView(
					readBytes = { Res.readBytes(ANIMATION_PATH) },
					modifier = Modifier.size(96.dp),
					testTag = AnimationTag,
					iterations = 2,
					onPlayback = { decisions += it }
				)
			}
		}

		waitForIdle()

		assertEquals(LottiePlayback(isPlaying = true, iterations = 2), decisions.last())
	}

	private companion object {
		const val AnimationTag = "lottie_animation"
		const val HostTag = "lottie_host"
		const val ANIMATION_PATH = "files/an_empty.json"
		const val LOAD_TIMEOUT_MILLIS = 5_000L
	}
}
