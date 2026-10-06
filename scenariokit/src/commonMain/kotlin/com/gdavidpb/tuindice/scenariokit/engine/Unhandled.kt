package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step

internal fun unhandled(step: Step): StepResult =
	StepResult.Failed(FailureKind.DRIVER_ERROR, "No executor for ${step::class.simpleName}")
