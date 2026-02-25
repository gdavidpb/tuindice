package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import kotlin.io.encoding.Base64
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

class IosStorageDataSource : StorageDataSource {
	override suspend fun getEnrollmentProof(name: String): EnrollmentProof {
		val filePath = enrollmentProofPath(name)
		val content = FileSystem.SYSTEM.read(filePath) {
			readByteArray()
		}

		return EnrollmentProof(
			source = filePath.toString(),
			content = Base64.encode(content)
		)
	}

	override suspend fun enrollmentProofExists(name: String): Boolean {
		return FileSystem.SYSTEM.exists(enrollmentProofPath(name))
	}

	override suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof) {
		val filePath = enrollmentProofPath(name)

		FileSystem.SYSTEM.createDirectories(filePath.parent!!)
		FileSystem.SYSTEM.write(filePath) {
			write(Base64.decode(enrollmentProof.content))
		}
	}

	private fun enrollmentProofPath(name: String): Path {
		val tempPath = NSTemporaryDirectory().trimEnd('/')
		return "$tempPath/tuindice/enrollmentProofs/$name.pdf".toPath()
	}
}
