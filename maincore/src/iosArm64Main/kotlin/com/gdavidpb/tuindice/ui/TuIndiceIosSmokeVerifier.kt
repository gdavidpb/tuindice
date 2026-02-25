package com.gdavidpb.tuindice.ui

/**
 * Device (iosArm64) binaries intentionally keep smoke runtime disabled.
 * Operational smoke/e2e functional checks run on simulator targets.
 */
class TuIndiceIosSmokeVerifier {
	fun runChecks(): String {
		return "PASS:smoke-runtime:disabled-on-device"
	}
}

