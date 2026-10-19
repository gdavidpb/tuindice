package com.gdavidpb.tuindice.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.gdavidpb.tuindice.base.domain.model.TermReportPush
import com.gdavidpb.tuindice.base.domain.repository.TermReportFocusRepository
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.platform.android.NotificationPermissionLauncher
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Below this width the app isn't laid out for landscape/multi-pane yet, so lock portrait. */
private const val COMPACT_SCREEN_WIDTH_DP = 600

open class MainActivity : ComponentActivity(), KoinComponent {
	private val systemAnimationsEnabled = mutableStateOf(true)
	private val termReportFocus: TermReportFocusRepository by inject()
	private val notificationPermissionLauncher: NotificationPermissionLauncher by inject()
	private var animatorScaleObserver: AnimatorScaleObserver? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		if (resources.configuration.smallestScreenWidthDp < COMPACT_SCREEN_WIDTH_DP) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}

		FileKit.init(this)
		// Registered before the activity starts, as the result launcher requires; the chat's offer asks through it.
		notificationPermissionLauncher.attach(
			registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
		)
		intent
			?.takeIf { pushIntent ->
				shouldOpenTermReportOnCreate(
					isRestored = savedInstanceState != null,
					isLaunchedFromHistory = pushIntent.hasFlag(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
				)
			}
			?.let(::openTermReportFromPush)
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

	// A tap on the push of a term report, while the app is running, brings the app to the front with the push's intent.
	// The new intent becomes the activity's, so a recreation reads the one it was last given.
	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)

		setIntent(intent)
		openTermReportFromPush(intent)
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

	override fun onDestroy() {
		notificationPermissionLauncher.detach()

		super.onDestroy()
	}

	// The push's data arrives as the extras of the intent a tap opens. The type is taken from them once, so a recreated
	// activity does not ask for the same report again.
	private fun openTermReportFromPush(pushIntent: Intent) {
		val extras = pushIntent.extras ?: return
		val isTermReport = extras.getString(TermReportPush.TYPE_KEY) == TermReportPush.TERM_REPORT_TYPE
		val insightId = extras.getString(TermReportPush.INSIGHT_ID_KEY)

		if (isTermReport && insightId != null) {
			termReportFocus.request(insightId)
			pushIntent.removeExtra(TermReportPush.TYPE_KEY)
		}
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

private fun Intent.hasFlag(flag: Int): Boolean = (flags and flag) != 0
