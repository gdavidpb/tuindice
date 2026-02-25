package com.gdavidpb.tuindice.enrollmentproof.presentation.resource

interface EnrollmentProofTextProvider {
	fun serviceUnavailable(): String
	fun networkUnavailable(): String
	fun enrollmentNotFound(): String
	fun enrollmentUnsupported(): String
	fun timeout(): String
	fun defaultError(): String
}
