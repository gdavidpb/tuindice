package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef

interface ExternalActionsRepository {
	fun openFile(fileRef: PlatformFileRef): Boolean
	fun sendEmail(email: String, subject: String = "", text: String = "")
	fun shareText(subject: String = "", text: String)
	fun openStorePage()
}
