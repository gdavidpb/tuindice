package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
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
import kotlin.test.assertFalse
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

	// The decision is observed on what is painted: a square that crosses the frame in two seconds. With the
	// animations on the frame changes as the clock moves; with them off it stays on the first one.
	@Test
	fun when_animationsAreEnabled_then_thePaintedFrameChangesWithTheClock() = runTuIndiceUiTest {
		val (first, later) = paintedFramesAround(animationsEnabled = true)

		assertTrue(first.hasTheSquare(), "the animation must have painted its square")
		assertFalse(first.contentEquals(later), "the animation must have moved between the two frames")
	}

	@Test
	fun when_animationsAreDisabled_then_thePaintedFrameStaysOnTheFirstOne() = runTuIndiceUiTest {
		val (first, later) = paintedFramesAround(animationsEnabled = false)

		// Two equal captures would also be two empty ones: the frame it stays on has to show the square.
		assertTrue(first.hasTheSquare(), "the frozen animation must still paint its square")
		assertTrue(first.contentEquals(later), "the animation must not have moved between the two frames")
	}

	// Any opaque red pixel: the interior of the square is pure red, whatever the edges blend into.
	private fun IntArray.hasTheSquare() = any { pixel ->
		(pixel ushr 24) == OPAQUE &&
			((pixel shr 16) and OPAQUE) > 200 &&
			((pixel shr 8) and OPAQUE) < 50 &&
			(pixel and OPAQUE) < 50
	}

	private fun ComposeUiTest.paintedFramesAround(animationsEnabled: Boolean): Pair<IntArray, IntArray> {
		var readCount = 0

		// The harness cancels infinite animations that start while the clock auto-advances.
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animationsEnabled) {
				LottieResourceAnimationView(
					readBytes = {
						readCount++
						MOVING_SQUARE.encodeToByteArray()
					},
					modifier = Modifier.size(100.dp),
					testTag = AnimationTag
				)
			}
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readCount == 1 }

		// The painter gets its composition a little after the bytes are read: the first frame is the first one
		// that shows the square, never an empty one that would make "the frame moved" true by itself.
		var first = IntArray(0)

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
			advanceAnimationsBy(50)
			first = onNodeWithTag(AnimationTag).captureToImage().toPixelMap().buffer.copyOf()
			first.hasTheSquare()
		}

		advanceAnimationsBy(1_000)

		return first to onNodeWithTag(AnimationTag).captureToImage().toPixelMap().buffer.copyOf()
	}

	private companion object {
		const val AnimationTag = "lottie_animation"
		const val HostTag = "lottie_host"
		const val ANIMATION_PATH = "files/an_empty.json"
		const val LOAD_TIMEOUT_MILLIS = 5_000L
		const val OPAQUE = 255

		// A red square, 40 by 40, that goes from the left to the right of a 100 by 100 frame in two seconds.
		const val MOVING_SQUARE = """{"v":"5.5.7","fr":30,"ip":0,"op":60,"w":100,"h":100,"nm":"moving","ddd":0,""" +
			""""assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"square","sr":1,"ao":0,"ip":0,"op":60,"st":0,""" +
			""""bm":0,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[50,50,0]},""" +
			""""a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"gr","nm":"g","it":[""" +
			"""{"ty":"rc","nm":"r","d":1,"s":{"a":0,"k":[40,40]},"r":{"a":0,"k":0},"p":{"a":1,"k":[""" +
			"""{"t":0,"s":[-30,0],"i":{"x":[0.5],"y":[0.5]},"o":{"x":[0.5],"y":[0.5]}},{"t":60,"s":[30,0]}]}},""" +
			"""{"ty":"fl","nm":"f","c":{"a":0,"k":[1,0,0,1]},"o":{"a":0,"k":100},"r":1},""" +
			"""{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},""" +
			""""r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}]}]}"""
	}
}
