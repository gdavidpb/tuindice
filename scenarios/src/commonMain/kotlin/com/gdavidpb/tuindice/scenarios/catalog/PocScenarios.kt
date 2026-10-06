package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenarios.fixture.Start
import kotlin.time.Duration.Companion.seconds

/** Fails on purpose to measure how a failure is reported; removed at the cut-over. */
val pocExpectedFailure = scenario("poc-expected-failure", "poc", Start.Clean().toLaunchSpec()) {
	tags("poc")

	waitVisible("poc_never_present", 2.seconds)
}
