package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step

/** What an `EnterText` that did not end well is called: a refusal, a driver that stopped, or text nobody sent. */
internal object TypedTextFailures {
	/**
	 * The driver answered false. A driver that stopped is not a corrupted text, so the kind depends on what the field
	 * holds and on whether any key went in:
	 * - a system dialog in front, or the app out of the foreground: the failure stays an assertion, and the refiner
	 *   turns it into `SYSTEM_DIALOG` or `APP_NOT_RUNNING`;
	 * - an empty or unreadable field, or one that already holds all the text: an assertion, nothing was typed;
	 * - no key injected but the field changed (the touch that focused it put something in): `DRIVER_ERROR`;
	 * - a field that holds the beginning of the text (a secure field: fewer characters than asked): `DRIVER_ERROR`,
	 *   "the driver stopped after N of M characters";
	 * - anything else is text the field holds that nobody sent: `TYPED_TEXT_MISMATCH`.
	 * A secure field is judged by its length alone, and its text never reaches a message.
	 */
	fun refused(driver: ScenarioDriver, step: Step.EnterText, message: String): StepResult.Failed {
		val refused = StepResult.Failed(FailureKind.ASSERTION, message)
		val held = driver.readText(step.q)?.takeIf { it.isNotEmpty() }
		val wanted = step.expect ?: step.text
		val complete = if (step.secure) held?.length == step.text.length else held == wanted

		return when {
			held == null || environmentInTheWay(driver) || (complete && driver.keysInjected() == 0) -> refused
			complete -> StepResult.Failed(
				FailureKind.DRIVER_ERROR,
				"the driver answered false although the field holds all ${wanted.length} characters: " +
					(runCatching { driver.lastRefusal() }.getOrNull() ?: "it gave no reason")
			)
			driver.keysInjected() == 0 -> StepResult.Failed(
				FailureKind.DRIVER_ERROR,
				"$message; no key was injected and the field holds ${held.length} characters"
			)
			isBeginning(step, held, wanted) -> StepResult.Failed(
				FailureKind.DRIVER_ERROR,
				"the driver stopped after ${held.length} of ${wanted.length} characters: " +
					(runCatching { driver.lastRefusal() }.getOrNull() ?: "it gave no reason")
			)
			else -> mismatch(step, held, wanted, "$message; ${held.length} of ${wanted.length} characters went in")
		}
	}

	private fun environmentInTheWay(driver: ScenarioDriver): Boolean =
		runCatching { driver.systemDialogInFront() != null || !driver.isForeground() }.getOrDefault(false)

	private fun isBeginning(step: Step.EnterText, held: String, wanted: String): Boolean =
		if (step.secure) held.length < step.text.length else wanted.startsWith(held)

	private fun mismatch(step: Step.EnterText, held: String, wanted: String, prefix: String): StepResult.Failed =
		if (step.secure) {
			StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				prefix,
				expected = "${step.text.length} characters",
				actual = "${held.length} characters"
			)
		} else {
			StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"$prefix and the field shows \"$held\" instead of \"$wanted\"",
				expected = wanted,
				actual = held
			)
		}

	fun unmatched(step: Step.EnterText, seen: String?, wanted: String): StepResult.Failed = when {
		seen == null -> StepResult.Failed(FailureKind.ASSERTION, "${step.target} could not be read back after typing")
		step.secure -> StepResult.Failed(
			FailureKind.TYPED_TEXT_MISMATCH,
			"typed ${step.text.length} characters into ${step.target} but the field holds ${seen.length}",
			expected = "${step.text.length} characters",
			actual = "${seen.length} characters"
		)
		else -> StepResult.Failed(
			FailureKind.TYPED_TEXT_MISMATCH,
			"typed \"$wanted\" into ${step.target} but the field shows \"$seen\"",
			expected = wanted,
			actual = seen
		)
	}

	/** The text matched and then the confirming read did not: it changed between two reads. */
	fun unheld(step: Step.EnterText, confirmed: String?, wanted: String): StepResult.Failed = when {
		confirmed == null -> StepResult.Failed(
			FailureKind.ASSERTION,
			"${step.target} could not be read back to confirm the typed text"
		)
		step.secure -> StepResult.Failed(
			FailureKind.TYPED_TEXT_MISMATCH,
			"the text of ${step.target} did not hold as typed: it changed between two reads " +
				"(${step.text.length} characters, then ${confirmed.length})",
			expected = "${step.text.length} characters",
			actual = "${confirmed.length} characters"
		)
		else -> StepResult.Failed(
			FailureKind.TYPED_TEXT_MISMATCH,
			"the text of ${step.target} did not hold as typed: it changed between two reads",
			expected = wanted,
			actual = confirmed
		)
	}
}
