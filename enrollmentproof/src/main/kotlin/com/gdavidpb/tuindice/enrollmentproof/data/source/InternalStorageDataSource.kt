package com.gdavidpb.tuindice.enrollmentproof.data.source

import android.content.Context
import com.gdavidpb.tuindice.base.utils.extension.File
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64

class InternalStorageDataSource(
	private val context: Context
) : StorageDataSource {
	private val enrollmentProofDir = "enrollmentProofs"

	override suspend fun getEnrollmentProof(name: String): EnrollmentProof {
		val enrollmentProofFile = getEnrollmentProofFile(name)
		val base64EncodedString = enrollmentProofFile.readBytes().encodeBase64()

		return EnrollmentProof(
			source = enrollmentProofFile.path,
			content = base64EncodedString
		)
	}

	override suspend fun enrollmentProofExists(name: String): Boolean {
		val enrollmentProofFile = getEnrollmentProofFile(name)

		return enrollmentProofFile.exists()
	}

	override suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof) {
		val enrollmentProofFile = getEnrollmentProofFile(name)

		enrollmentProofFile.apply {
			parentFile?.mkdirs()

			val base64ByteArray = enrollmentProof.content.decodeBase64Bytes()

			writeBytes(base64ByteArray)
		}
	}

	private fun getEnrollmentProofFile(name: String) =
		File(context.filesDir, enrollmentProofDir, "$name.pdf")
}