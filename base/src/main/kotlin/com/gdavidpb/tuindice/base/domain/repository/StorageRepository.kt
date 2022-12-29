package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface StorageRepository {
	suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof
	suspend fun existsEnrollmentProof(quarter: Quarter): Boolean
	suspend fun saveEnrollmentProof(quarter: Quarter, enrollmentProof: EnrollmentProof)

	@Deprecated("This will replace by an entity operation.")
	fun get(path: String): File

	@Deprecated("This will be replace by an entity operation.")
	fun create(path: String): File

	@Deprecated("This will be replace by an entity operation.")
	fun inputStream(path: String): InputStream

	@Deprecated("This will be replace by an entity operation.")
	fun outputStream(path: String): OutputStream

	@Deprecated("This will be replace by an entity operation.")
	fun delete(path: String)

	@Deprecated("This will be replace by an entity operation.")
	fun exists(path: String): Boolean

	suspend fun clear()
}