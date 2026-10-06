package com.gdavidpb.tuindice.scenariokit.contract

/** One probe of a driver; [run] answers the problem it found, or null when the driver behaves. */
internal class DriverContractCheck(val name: String, val run: () -> String?)
