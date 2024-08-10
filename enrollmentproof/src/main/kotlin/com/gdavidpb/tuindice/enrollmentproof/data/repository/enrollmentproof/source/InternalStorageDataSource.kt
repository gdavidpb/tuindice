package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source

import android.content.Context
import com.gdavidpb.tuindice.base.utils.extension.File
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64

class InternalStorageDataSource(
	private val context: Context
) : StorageDataSource {
	private val enrollmentProofDir = "enrollmentProofs"

	override suspend fun getEnrollmentProof(uid: String, name: String): EnrollmentProof {
		val enrollmentProofFile = getEnrollmentProofFile(uid, name)
		val base64EncodedString = enrollmentProofFile.readBytes().encodeBase64()

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

			val base64ByteArray = enrollmentProof.content.decodeBase64Bytes()

			writeBytes(base64ByteArray)
		}
	}

	private fun getEnrollmentProofFile(uid: String, name: String) =
		File(context.filesDir, uid, enrollmentProofDir, "$name.pdf")
}