package com.gdavidpb.tuindice.record.data.storage

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.utils.extensions.File
import com.gdavidpb.tuindice.base.utils.extensions.decodeFromBase64String
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof

class InternalStorageDataSource(
	private val context: Context
) : LocalDataSource {
	private val enrollmentProofDir = "enrollments"

	override suspend fun getEnrollmentProof(uid: String, quarter: Quarter): EnrollmentProof {
		val enrollmentProofFile = getEnrollmentFile(uid, quarter)
		val base64EncodedString = enrollmentProofFile.readBytes().encodeToBase64String()

		return EnrollmentProof(
			source = enrollmentProofFile.path,
			content = base64EncodedString
		)
	}

	override suspend fun enrollmentProofExists(uid: String, quarter: Quarter): Boolean {
		val enrollmentProofFile = getEnrollmentFile(uid, quarter)

		return enrollmentProofFile.exists()
	}

	override suspend fun saveEnrollmentProof(
		uid: String,
		quarter: Quarter,
		enrollmentProof: EnrollmentProof
	) {
		val enrollmentProofFile = getEnrollmentFile(uid, quarter)

		enrollmentProofFile.apply {
			mkdirs()

			val base64ByteArray = enrollmentProof.content.decodeFromBase64String()

			writeBytes(base64ByteArray)
		}
	}

	private fun getEnrollmentFile(uid: String, quarter: Quarter) =
		File(context.filesDir, uid, enrollmentProofDir, "${quarter.name}.pdf")
}