package com.gdavidpb.tuindice.enrollmentproof.presentation.resource

interface EnrollmentProofTextProvider {
	suspend fun serviceUnavailable(): String
	suspend fun networkUnavailable(): String
	suspend fun enrollmentNotFound(): String
	suspend fun enrollmentUnsupported(): String
	suspend fun timeout(): String
	suspend fun defaultError(): String
}
