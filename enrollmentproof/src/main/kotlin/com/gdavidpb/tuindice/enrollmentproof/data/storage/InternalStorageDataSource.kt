package com.gdavidpb.tuindice.enrollmentproof.data.storage

import android.content.Context
import com.gdavidpb.tuindice.base.utils.extensions.File
import com.gdavidpb.tuindice.base.utils.extensions.decodeFromBase64String
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof

class InternalStorageDataSource(
	private val context: Context
) : StorageDataSource {
	private val enrollmentProofDir = "enrollmentProofs"

	override suspend fun getEnrollmentProof(uid: String, name: String): EnrollmentProof {
		val enrollmentProofFile = getEnrollmentProofFile(uid, name)
		val base64EncodedString = enrollmentProofFile.readBytes().encodeToBase64String()

		return EnrollmentProof(
			source = enrollmentProofFile.path,
			content = base64EncodedString
		)
	}

	override suspend fun enrollmentProofExists(uid: String, name: String): Boolean {
		val enrollmentProofFile = getEnrollmentProofFile(uid, name)

		return enrollmentProofFile.exists()
	}

	override suspend fun saveEnrollmentProof(
		uid: String,
		name: String,
		enrollmentProof: EnrollmentProof
	) {
		val enrollmentProofFile = getEnrollmentProofFile(uid, name)

		enrollmentProofFile.apply {
			parentFile?.mkdirs()

			val base64ByteArray = enrollmentProof.content.decodeFromBase64String()

			writeBytes(base64ByteArray)
		}
	}

	private fun getEnrollmentProofFile(uid: String, name: String) =
		File(context.filesDir, uid, enrollmentProofDir, "$name.pdf")
}