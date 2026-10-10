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
	/** A secure field of the same screen, typed with [secureSample]; it is judged by length, like the interpreter does. */
	val secureFieldTag: String? = null,
	/** At least 25 characters, so a driver that drops or doubles a key of a long run is caught. */
	val secureSample: String = "",
	/** Typed into [textFieldTag] by `typeKeys`; it must not be the placeholder the field shows when empty. */
	val textSample: String,
	/** What the field must show after typing [textSample]; differs from it when the field masks its input. */
	val expectedText: String
)
