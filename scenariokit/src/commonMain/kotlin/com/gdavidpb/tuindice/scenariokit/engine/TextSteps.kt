package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

internal class TextSteps(private val driver: ScenarioDriver, private val poller: Poller) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.EnterText -> enterText(step)
		is Step.SubmitTextEntry -> passIf(driver.submitTextEntry(), FailureKind.ASSERTION) {
			driver.refused("the IME action could not be sent")
		}
		is Step.HideKeyboard -> passIf(driver.hideKeyboard(), FailureKind.ASSERTION) {
			driver.refused("the keyboard could not be hidden")
		}
		else -> unhandled(step)
	}

	/** Waits for the field, optionally clears it, enters the text once and re-reads it. */
	private fun enterText(step: Step.EnterText): StepResult =
		driver.awaitTarget(step.q)
			?: clearBeforeTyping(step)
			?: type(step)
			?: reread(step)
			?: StepResult.Passed

	private fun clearBeforeTyping(step: Step.EnterText): StepResult.Failed? =
		if (step.replace && !driver.clearText(step.q)) {
			StepResult.Failed(FailureKind.ASSERTION, driver.refused("clearText ${step.target} was refused"))
		} else {
			null
		}

	private fun type(step: Step.EnterText): StepResult.Failed? {
		val accepted = driver.typeKeys(step.q, step.text)
		return if (accepted) null else refusedTyping(step, driver.refused("typing into ${step.target} was refused"))
	}

	/**
	 * The driver answered false, and a refused typing can leave the field half written. A field that holds something
	 * other than what was asked is corrupted text, which is never retried; an empty or unreadable one was simply not
	 * typed into. A secure field is judged by its length alone, and the text of a secure field never reaches a message.
	 */
	private fun refusedTyping(step: Step.EnterText, message: String): StepResult.Failed {
		val refused = StepResult.Failed(FailureKind.ASSERTION, message)
		val held = driver.readText(step.q)?.takeIf { it.isNotEmpty() } ?: return refused
		val wanted = step.expect ?: step.text
		val entered = "${held.length} of ${wanted.length} characters went in"

		return when {
			step.secure && held.length == step.text.length -> refused
			step.secure -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"$message; $entered",
				expected = "${step.text.length} characters",
				actual = "${held.length} characters"
			)
			held == wanted -> refused
			else -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"$message; $entered and the field shows \"$held\" instead of \"$wanted\"",
				expected = wanted,
				actual = held
			)
		}
	}

	/**
	 * The field is good when two reads in a row, [Timeouts.PollInterval] apart, show what was typed: the first
	 * match alone would miss an echo of the keyboard that lands after it and overwrites the field. A secure field
	 * cannot show its text, but it shows one character per typed character, so its length is what is compared.
	 */
	private fun reread(step: Step.EnterText): StepResult.Failed? {
		val wanted = step.expect ?: step.text
		var seen: String? = null
		var matchesInARow = 0
		val settled = poller.until(Timeouts.TextReread) {
			seen = driver.readText(step.q)
			matchesInARow = if (matches(step, seen, wanted)) matchesInARow + 1 else 0
			matchesInARow >= CONSECUTIVE_READS
		}
		return when {
			settled -> null
			seen == null -> StepResult.Failed(FailureKind.ASSERTION, "${step.target} could not be read back after typing")
			step.secure -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"typed ${step.text.length} characters into ${step.target} but the field holds ${seen.orEmpty().length}",
				expected = "${step.text.length} characters",
				actual = "${seen.orEmpty().length} characters"
			)
			matches(step, seen, wanted) -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"the text of ${step.target} did not hold as typed: it changed between two reads",
				expected = wanted,
				actual = seen.orEmpty()
			)
			else -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"typed \"$wanted\" into ${step.target} but the field shows \"$seen\"",
				expected = wanted,
				actual = seen.orEmpty()
			)
		}
	}

	private fun matches(step: Step.EnterText, read: String?, wanted: String): Boolean =
		if (step.secure) read?.length == step.text.length else read == wanted

	private companion object {
		const val CONSECUTIVE_READS = 2
	}
}
