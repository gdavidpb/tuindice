package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Step

/** A step with where it sits: the platform branch it runs under and the groups that enclose it. */
internal data class LocatedStep(val step: Step, val platform: Platform?, val groups: List<String>)

/** Every step, containers included, depth first, each with its platform branch and enclosing groups. */
internal fun List<Step>.located(platform: Platform? = null, groups: List<String> = emptyList()): List<LocatedStep> =
	flatMap { step ->
		val inner = when (step) {
			is Step.OnPlatform -> step.steps.located(step.platform, groups)
			is Step.Group -> step.steps.located(platform, groups + step.name)
			is Step.Container -> step.steps.located(platform, groups)
			else -> emptyList()
		}

		listOf(LocatedStep(step, platform, groups)) + inner
	}
