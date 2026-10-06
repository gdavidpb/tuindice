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
	val textFieldTag: String? = null,
	/** Typed into [textFieldTag] by both `setText` and `typeKeys`. */
	val textSample: String = "ab1",
	/** What the field must show after typing [textSample]; differs from it when the field masks its input. */
	val expectedText: String = "ab1"
)
