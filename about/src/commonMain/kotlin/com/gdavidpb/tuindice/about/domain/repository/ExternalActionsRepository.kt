package com.gdavidpb.tuindice.about.domain.repository

interface ExternalActionsRepository {
	fun shareText(subject: String, text: String)
}
