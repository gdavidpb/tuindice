package com.gdavidpb.tuindice.ui.view

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TopBarBannerHostUiTest {
	@Test
	fun when_autoDismissBannerIsRequested_then_hidesAfterTimeout() = runTuIndiceUiTest {
		lateinit var requestBanner: (TopBarBannerBehavior) -> Unit

		setTuIndiceTestContent {
			val behavior = remember { mutableStateOf<TopBarBannerBehavior?>(null) }
			val requestKey = remember { mutableIntStateOf(0) }
			requestBanner = { nextBehavior ->
				behavior.value = nextBehavior
				requestKey.intValue += 1
			}

			TopBarBannerHost(
				isContentAvailable = true,
				requestKey = requestKey.intValue,
				behavior = behavior.value
			) {
				Text(
					text = "Banner",
					modifier = Modifier.testTag(TestBannerTag)
				)
			}
		}

		runOnIdle {
			requestBanner(TopBarBannerBehavior.AutoDismiss(millis = 5_000L))
		}
		advanceAnimationsBy(millis = 400)

		assertNodeVisible(TestBannerTag)

		advanceAnimationsBy(millis = 6_000)

		assertNodeHidden(TestBannerTag)
	}

	@Test
	fun when_persistentBannerIsRequested_then_remainsVisible() = runTuIndiceUiTest {
		lateinit var requestBanner: (TopBarBannerBehavior) -> Unit

		setTuIndiceTestContent {
			val behavior = remember { mutableStateOf<TopBarBannerBehavior?>(null) }
			val requestKey = remember { mutableIntStateOf(0) }
			requestBanner = { nextBehavior ->
				behavior.value = nextBehavior
				requestKey.intValue += 1
			}

			TopBarBannerHost(
				isContentAvailable = true,
				requestKey = requestKey.intValue,
				behavior = behavior.value
			) {
				Text(
					text = "Banner",
					modifier = Modifier.testTag(TestBannerTag)
				)
			}
		}

		runOnIdle {
			requestBanner(TopBarBannerBehavior.Persistent)
		}
		advanceAnimationsBy(millis = 400)

		assertNodeVisible(TestBannerTag)

		advanceAnimationsBy(millis = 6_000)

		assertNodeVisible(TestBannerTag)
	}

	@Test
	fun when_contentBecomesUnavailable_then_hidesVisibleBanner() = runTuIndiceUiTest {
		lateinit var requestBanner: (TopBarBannerBehavior) -> Unit
		lateinit var setContentAvailable: (Boolean) -> Unit

		setTuIndiceTestContent {
			val behavior = remember { mutableStateOf<TopBarBannerBehavior?>(null) }
			val isContentAvailable = remember { mutableStateOf(true) }
			val requestKey = remember { mutableIntStateOf(0) }
			requestBanner = { nextBehavior ->
				behavior.value = nextBehavior
				requestKey.intValue += 1
			}
			setContentAvailable = { nextValue ->
				isContentAvailable.value = nextValue
			}

			TopBarBannerHost(
				isContentAvailable = isContentAvailable.value,
				requestKey = requestKey.intValue,
				behavior = behavior.value
			) {
				Text(
					text = "Banner",
					modifier = Modifier.testTag(TestBannerTag)
				)
			}
		}

		runOnIdle {
			requestBanner(TopBarBannerBehavior.Persistent)
		}
		advanceAnimationsBy(millis = 400)

		assertNodeVisible(TestBannerTag)

		runOnIdle {
			setContentAvailable(false)
		}
		advanceAnimationsBy(millis = 400)

		assertNodeHidden(TestBannerTag)
	}
}

private const val TestBannerTag = "top_bar_banner_host_test_banner"
