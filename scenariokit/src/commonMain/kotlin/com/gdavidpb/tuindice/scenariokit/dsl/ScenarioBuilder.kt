package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Quarantine
import com.gdavidpb.tuindice.scenariokit.model.Scenario

/** Steps plus the scenario's metadata; only the top-level block has the metadata functions. */
class ScenarioBuilder internal constructor(
	private val id: String,
	private val module: String,
	private val start: LaunchSpec
) : StepBuilder() {
	private var covers = emptyList<String>()
	private var tags = emptyList<String>()
	private var platforms = Platform.entries.toList()
	private var account: String? = null
	private var signsIn = false
	private var timeoutSeconds = Scenario.DefaultTimeoutSeconds
	private var quarantine: Quarantine? = null

	fun covers(vararg actions: String) {
		covers = actions.toList()
	}

	fun tags(vararg values: String) {
		tags = values.toList()
	}

	fun platforms(vararg values: Platform) {
		platforms = values.toList()
	}

	/** The fixture account the scenario starts with or signs in as. */
	fun account(accountId: String) {
		account = accountId
	}

	/** The scenario signs in through the UI (as opposed to starting with a seeded session). */
	fun signsIn() {
		signsIn = true
	}

	fun timeout(seconds: Int) {
		timeoutSeconds = seconds
	}

	fun quarantine(reason: String, until: String) {
		quarantine = Quarantine(reason, until)
	}

	internal fun toScenario(): Scenario = Scenario(
		id = id,
		module = module,
		start = start,
		steps = build(),
		covers = covers,
		tags = tags,
		platforms = platforms,
		account = account,
		signsIn = signsIn,
		timeoutSeconds = timeoutSeconds,
		quarantine = quarantine
	)
}
