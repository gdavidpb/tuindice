package com.gdavidpb.tuindice.ui.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

/** Below this width the app isn't laid out for landscape/multi-pane yet, so lock portrait. */
private const val COMPACT_SCREEN_WIDTH_DP = 600

open class MainActivity : ComponentActivity() {
	private val systemAnimationsEnabled = mutableStateOf(true)
	private var animatorScaleObserver: AnimatorScaleObserver? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		if (resources.configuration.smallestScreenWidthDp < COMPACT_SCREEN_WIDTH_DP) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}

		FileKit.init(this)
		onBeforeContent(isColdStart = savedInstanceState == null)
		refreshSystemAnimations()

		setContent {
			Box(
				modifier = Modifier.semantics {
					testTagsAsResourceId = true
				}
			) {
				TuIndiceTheme {
					CompositionLocalProvider(
						LocalTuIndiceAnimationsEnabled provides systemAnimationsEnabled.value
					) {
						TuIndiceAppHostRoute(
							onConfirmExitClick = ::finish
						)
					}
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()

		// The animator duration scale lives in developer options: read what changed while the app
		// was away, then follow it while the app is visible.
		refreshSystemAnimations()

		animatorScaleObserver = AnimatorScaleObserver(contentResolver, ::refreshSystemAnimations)
			.also(AnimatorScaleObserver::start)
	}

	override fun onStop() {
		animatorScaleObserver?.stop()
		animatorScaleObserver = null

		super.onStop()
	}

	/** [isColdStart] is false when the activity is recreated or restored after the process died. */
	protected open fun onBeforeContent(isColdStart: Boolean) = Unit

	/** Hosts that must run without looping animations (E2E) say so here. */
	protected open fun areAnimationsForcedOff(): Boolean = false

	private fun refreshSystemAnimations() {
		systemAnimationsEnabled.value = animationsEnabled(
			animatorDurationScale = Settings.Global.getFloat(
				contentResolver,
				Settings.Global.ANIMATOR_DURATION_SCALE,
				1f
			),
			forcedOff = areAnimationsForcedOff()
		)
	}
}
