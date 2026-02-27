package com.gdavidpb.tuindice.about.domain.repository

interface ExternalActionsRepository {
	fun sendEmail(email: String, subject: String, text: String)
	fun shareText(subject: String, text: String)
	fun openStorePage()
}
