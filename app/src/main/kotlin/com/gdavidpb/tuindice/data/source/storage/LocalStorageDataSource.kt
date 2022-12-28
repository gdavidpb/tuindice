package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.StorageRepository
import com.gdavidpb.tuindice.base.utils.extensions.decodeFromBase64String
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class LocalStorageDataSource(
	private val context: Context
) : StorageRepository {

	private val root by lazy { context.filesDir }

	override suspend fun existsEnrollmentProof(quarter: Quarter): Boolean {
		val outputPath = File(root, quarter.id)
		val outputPdfFile = File(outputPath, "${quarter.name}.pdf")

		return outputPdfFile.exists()
	}

	override suspend fun getEnrollmentProof(quarter: Quarter): EnrollmentProof {
		val pdfFilePath = File(root, quarter.id)
		val pdfFile = File(pdfFilePath, "${quarter.name}.pdf")

		val base64EncodedString = pdfFile.readBytes().encodeToBase64String()

		return EnrollmentProof(
			name = quarter.name,
			content = base64EncodedString
		)
	}

	override suspend fun saveEnrollmentProof(quarter: Quarter, enrollmentProof: EnrollmentProof) {
		val pdfFilePath = File(root, quarter.id)
		val pdfFile = File(pdfFilePath, "${quarter.name}.pdf")

		pdfFile.apply {
			mkdirs()

			val base64ByteArray = enrollmentProof.content.decodeFromBase64String()

			writeBytes(base64ByteArray)
		}
	}

	override fun get(path: String): File {
		return File(root, path)
	}

	override fun create(path: String): File {
		val outputFile = File(path)

		outputFile.createNewFile()

		return outputFile
	}

	override fun outputStream(path: String): OutputStream {
		val file = File(path)

		return file.outputStream()
	}

	override fun inputStream(path: String): InputStream {
		val file = File(root, path)

		return file.inputStream()
	}

	override fun delete(path: String) {
		runCatching {
			File(root, path).let {
				if (it.isDirectory)
					it.deleteRecursively()
				else
					it.delete()
			}
		}.onFailure { throwable ->
			if (throwable !is FileNotFoundException) throw throwable
		}
	}

	override fun exists(path: String): Boolean {
		val file = File(root, path)

		return file.exists()
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