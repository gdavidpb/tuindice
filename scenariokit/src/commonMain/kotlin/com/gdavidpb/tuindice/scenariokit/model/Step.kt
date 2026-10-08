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
		val mode: TextEntryMode,
		override val site: Site? = null
	) : Step {
		override val target: String get() = q.describe()
	}

	@Serializable
	@SerialName("clearText")
	data class ClearText(val q: Query, override val site: Site? = null) : Step {
		override val target: String get() = q.describe()
	}

	@Serializable
	@SerialName("finishTextEntry")
	data class FinishTextEntry(override val site: Site? = null) : Step

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

	/** Waits until the element (or the screen) stops moving. */
	@Serializable
	@SerialName("settle")
	data class Settle(val q: Query?, val timeoutMs: Long, override val site: Site? = null) : Step {
		override val target: String get() = q?.describe() ?: "screen"
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

	/** Runs [steps] when [q] is gone within [withinMs]; never fails by itself. */
	@Serializable
	@SerialName("ifGone")
	data class IfGone(
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

	/** Re-runs [steps] after an assertion or timeout, at most [MAX_ATTEMPTS] times in all. */
	@Serializable
	@SerialName("retry")
	data class Retry(
		val maxAttempts: Int,
		val reason: String,
		override val steps: List<Step>,
		override val site: Site? = null
	) : Container {
		override val target: String get() = reason

		companion object {
			const val MAX_ATTEMPTS = 3
		}
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
