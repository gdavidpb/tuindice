package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.StorageRepository
import com.gdavidpb.tuindice.base.utils.extensions.decodeFromBase64String
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import java.io.File
import java.io.FileNotFoundException

class LocalStorageDataSource(
	private val context: Context
) : StorageRepository {

	private val root by lazy { context.filesDir }

	private val enrollments by lazy { File(root, "enrollments") }

	override suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof {
		val enrollmentProofFile = File(enrollments, "${quarter.name}.pdf")

		val base64EncodedString = enrollmentProofFile.readBytes().encodeToBase64String()

		return EnrollmentProof(
			source = enrollmentProofFile.path,
			content = base64EncodedString
		)
	}

	override suspend fun existsEnrollmentProof(quarter: Quarter): Boolean {
		val enrollmentProofFile = File(enrollments, "${quarter.name}.pdf")

		return enrollmentProofFile.exists()
	}

	override suspend fun saveEnrollmentProof(quarter: Quarter, enrollmentProof: EnrollmentProof) {
		val enrollmentProofFile = File(enrollments, "${quarter.name}.pdf")

		enrollmentProofFile.apply {
			mkdirs()

			val base64ByteArray = enrollmentProof.content.decodeFromBase64String()

			writeBytes(base64ByteArray)
		}
	}

	override suspend fun clear() {
		runCatching {
			root.deleteRecursively()
			root.mkdir()
		}.onFailure { throwable ->
			if (throwable !is FileNotFoundException) throw throwable
		}
	}
}