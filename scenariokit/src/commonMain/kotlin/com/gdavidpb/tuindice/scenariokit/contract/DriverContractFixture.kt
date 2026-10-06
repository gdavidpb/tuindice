package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import kotlinx.serialization.Serializable

/** The screen and the tags `DriverContract` probes a driver against; shipped inside the catalog. */
@Serializable
data class DriverContractFixture(
	val start: LaunchSpec,
	val presentTag: String,
	val absentTag: String,
	val disabledTag: String? = null,
	val textFieldTag: String? = null
)
