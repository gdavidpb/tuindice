package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One instruction of a scenario. Every case carries the [site] of the DSL call that
 * built it, which is excluded from the catalog's `stepsHash`.
 */
@Serializable
sealed interface Step {
	val site: Site?

	/** What the step acts on, for logs and `result.json`. */
	val target: String get() = ""

	/** A step that runs other steps. */
	@Serializable
	sealed interface Container : Step {
		val steps: List<Step>
	}

	/**
	 * Starts the app again keeping its state; the first launch comes from `Scenario.start`. No [arguments] means the
	 * launch arguments of `Scenario.start`.
	 */
	@Serializable
	@SerialName("relaunch")
	data class Relaunch(val arguments: Map<String, String>, override val site: Site? = null) : Step

	@Serializable
	@SerialName("foreground")
	data class Foreground(override val site: Site? = null) : Step

	@Serializable
	@SerialName("tap")
	data class Tap(val q: Query, val requireEnabled: Boolean = true, override val site: Site? = null) : Step {
		override val target: String get() = q.describe()
	}

	/** Taps at a screen fraction, relative to [q] when it is set. */
	@Serializable
	@SerialName("tapAt")
	data class TapAt(val q: Query?, val fx: Double, val fy: Double, override val site: Site? = null) : Step {
		override val target: String get() = q?.describe() ?: "screen"
	}

	@Serializable
	@SerialName("doubleTap")
	data class DoubleTap(val q: Query, override val site: Site? = null) : Step {
		override val target: String get() = q.describe()
	}

	@Serializable
	@SerialName("back")
	data class Back(override val site: Site? = null) : Step

	@Serializable
	@SerialName("enterText")
	data class EnterText(
		val q: Query,
		val text: String,
		val expect: String?,
		val secure: Boolean,
		val replace: Boolean,
		override val site: Site? = null
	) : Step {
		override val target: String get() = q.describe()
	}

	/**
	 * Sends the IME action of the field that has the focus (search, done, go): the same thing the action key of the
	 * keyboard does, with the effect the app gives it. It does not pick the field: type into it first.
	 */
	@Serializable
	@SerialName("submitTextEntry")
	data class SubmitTextEntry(override val site: Site? = null) : Step

	@Serializable
	@SerialName("waitVisible")
	data class WaitVisible(val q: Query, val timeoutMs: Long, override val site: Site? = null) : Step {
		override val target: String get() = q.describe()
	}

	@Serializable
	@SerialName("waitGone")
	data class WaitGone(val q: Query, val timeoutMs: Long, override val site: Site? = null) : Step {
		override val target: String get() = q.describe()
	}

	/**
	 * Waits until the app has left the foreground and has stayed out for 3 reads in a row, one poll interval apart: for a
	 * step that sends the user out of the app (an external link). "Left the foreground" is `AppControl.isForeground`,
	 * which is not the same on both platforms: Android counts anything but the app's own window in front (another app,
	 * a system dialog, the app switcher, the notification shade, an unreadable screen); iOS counts only the app's
	 * state (`runningForeground` and no move to `runningBackground` within 0.3 s), so a system alert in front of the
	 * app does not count. An app whose process is gone does NOT pass: the step fails with `APP_NOT_RUNNING` on the
	 * read that finds it dead (`AppControl.isRunning`), so a link that crashes the app is not taken for one that
	 * opened a browser.
	 */
	@Serializable
	@SerialName("waitBackgrounded")
	data class WaitBackgrounded(val timeoutMs: Long, override val site: Site? = null) : Step

	@Serializable
	@SerialName("assertEnabled")
	data class AssertEnabled(
		val q: Query,
		val enabled: Boolean,
		val timeoutMs: Long,
		override val site: Site? = null
	) : Step {
		override val target: String get() = q.describe()
	}

	/**
	 * Polls until the checked state of the checkbox [q] (a `toggleable(role = Checkbox)`) is [checked]; a missing
	 * element, or one that is not a checkbox, never satisfies either state.
	 */
	@Serializable
	@SerialName("assertChecked")
	data class AssertChecked(
		val q: Query,
		val checked: Boolean,
		val timeoutMs: Long,
		override val site: Site? = null
	) : Step {
		override val target: String get() = q.describe()
	}

	/** Swipes from a screen fraction (relative to [from] when set) by a delta in screen fractions. */
	@Serializable
	@SerialName("swipe")
	data class Swipe(
		val from: Query?,
		val fx: Double,
		val fy: Double,
		val dx: Double,
		val dy: Double,
		val durationMs: Long,
		override val site: Site? = null
	) : Step {
		override val target: String get() = from?.describe() ?: "screen"
	}

	@Serializable
	@SerialName("scrollUntilVisible")
	data class ScrollUntilVisible(
		val q: Query,
		val direction: Scroll,
		val timeoutMs: Long,
		override val site: Site? = null
	) : Step {
		override val target: String get() = q.describe()
	}

	/** Runs [steps] when [q] shows up within [withinMs]; never fails by itself. */
	@Serializable
	@SerialName("ifVisible")
	data class IfVisible(
		val q: Query,
		val withinMs: Long,
		override val steps: List<Step>,
		override val site: Site? = null
	) : Container {
		override val target: String get() = q.describe()
	}

	@Serializable
	@SerialName("onPlatform")
	data class OnPlatform(
		val platform: Platform,
		override val steps: List<Step>,
		override val site: Site? = null
	) : Container {
		override val target: String get() = platform.name
	}

	/**
	 * Sets the state of a WireMock scenario in the middle of a scenario, with the same request the interpreter uses
	 * before the first launch for `LaunchSpec.mockStates`: what the app asks for from here on gets that state's answer.
	 */
	@Serializable
	@SerialName("mockState")
	data class SetMockState(val scenario: String, val state: String, override val site: Site? = null) : Step {
		override val target: String get() = "$scenario = $state"
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Serializable
	@SerialName("expectRequest")
	data class ExpectRequest(
		val method: String,
		val path: String,
		val basicAuth: String?,
		val timeoutMs: Long,
		/** When set, only a request the backend answered with this status counts. */
		val status: Int? = null,
		/**
		 * How many matching requests the step needs. It counts the requests of the backend journal since the start of
		 * the scenario (the journal is emptied only before the app launches), not since the step: a request the app
		 * sent earlier counts, so a step that must see a new request asks for one more than were already there.
		 * The default is not written, so the catalog and the hash of the scenarios that do not use it stay as they were.
		 */
		@EncodeDefault(EncodeDefault.Mode.NEVER)
		val atLeast: Int = 1,
		override val site: Site? = null
	) : Step {
		override val target: String
			get() = buildString {
				append("$method $path")
				status?.let { append(" answered $it") }
				if (atLeast != 1) append(", at least $atLeast")
			}
	}

	/** A named sub-scenario; shows up in logs. */
	@Serializable
	@SerialName("group")
	data class Group(val name: String, override val steps: List<Step>, override val site: Site? = null) : Container {
		override val target: String get() = name
	}
}
