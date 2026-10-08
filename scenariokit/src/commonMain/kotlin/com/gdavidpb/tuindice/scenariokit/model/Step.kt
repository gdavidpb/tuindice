package com.gdavidpb.tuindice.scenariokit.model

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

	@Serializable
	@SerialName("waitAnyVisible")
	data class WaitAnyVisible(val queries: List<Query>, val timeoutMs: Long, override val site: Site? = null) : Step {
		override val target: String get() = queries.joinToString(" | ") { it.describe() }
	}

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

	@Serializable
	@SerialName("expectRequest")
	data class ExpectRequest(
		val method: String,
		val path: String,
		val basicAuth: String?,
		val timeoutMs: Long,
		override val site: Site? = null
	) : Step {
		override val target: String get() = "$method $path"
	}

	/** A named sub-scenario; shows up in logs. */
	@Serializable
	@SerialName("group")
	data class Group(val name: String, override val steps: List<Step>, override val site: Site? = null) : Container {
		override val target: String get() = name
	}
}
