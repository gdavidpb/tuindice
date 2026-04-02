package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.github.vinceglb.filekit.tempDir
import io.github.vinceglb.filekit.write
import kotlin.random.Random
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class IosProfilePictureInputDataSource : ProfilePictureInputDataRepository {
	private object Settings {
		const val JPEG_QUALITY = 0.85
		const val NORMALIZED_DIRECTORY = "summaryProfilePictures"
	}

	override suspend fun normalizeInput(file: PlatformFile): PlatformFile {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			val content = file.readBytes()
			if (!content.isHeifFamily()) return file

			val path = file.path
			if (path.isBlank()) return file

			val image = UIImage(contentsOfFile = path)
			val jpegData = UIImageJPEGRepresentation(image, Settings.JPEG_QUALITY) ?: return file
			val normalizedDirectory = FileKit.filesDir / Settings.NORMALIZED_DIRECTORY

			val normalizedFile =
				normalizedDirectory /
					"profile_picture_${Random.nextInt(1_000_000)}.jpg"

			normalizedDirectory.createDirectories()

			normalizedFile.write(jpegData.toByteArray())

			check(normalizedFile.path.isNotBlank()) {
				"Expected HEIC/HEIF profile picture normalization to persist a JPEG file."
			}

			return normalizedFile
		} finally {
			if (accessGranted)
				file.stopAccessingSecurityScopedResource()
		}
	}

	private fun ByteArray.isHeifFamily(): Boolean {
		if (size < 12) return false
		if (decodeAscii(start = 4, count = 4) != "ftyp") return false

		return decodeAscii(start = 8, count = 4) in setOf(
			"heic",
			"heix",
			"hevc",
			"hevx",
			"heim",
			"heis",
			"hevm",
			"hevs",
			"mif1",
			"msf1"
		)
	}

	private fun ByteArray.decodeAscii(start: Int, count: Int): String {
		return buildString(count) {
			for (index in start until start + count) {
				append(this@decodeAscii[index].toInt().toChar())
			}
		}
	}

	private fun NSData.toByteArray(): ByteArray {
		val length = length.toInt()
		if (length == 0) return ByteArray(0)

		return ByteArray(length).also { result ->
			result.usePinned { pinned ->
				memcpy(pinned.addressOf(0), bytes, length.convert())
			}
		}
	}
}
