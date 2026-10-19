package com.gdavidpb.tuindice.auth.presentation.resource

// The error texts of a failed sign-in attempt that only sign-in produces. The machine reads them through this seam, so
// its walk runs on any host without platform resources.
interface SignInTextProvider {
	suspend fun invalidUsbIdCredentials(): String
	suspend fun invalidUsbEmailCredentials(): String
	suspend fun accountDisabled(supportEmail: String): String
	suspend fun untrusted(supportEmail: String): String
	suspend fun authenticationFailed(): String
	suspend fun usbUnavailable(): String
	suspend fun tooManyRequests(): String
}
