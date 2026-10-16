package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.data.repository.ImageSharePresenterDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path

/**
 * Shares a PNG through the system share sheet. The image is written under [storageDirectory], which the
 * platform module places in the temporary storage that sign-out clears.
 */
class IosImageShareDataSource(
	private val storageDirectory: Path,
	private val dispatchers: TuIndiceDispatchers,
	private val presenter: ImageSharePresenterDataRepository
) : ImageShareRepository {
	override suspend fun shareImage(image: ShareableImage) {
		val file = withContext(dispatchers.io) { writeImage(image) }

		withContext(dispatchers.main) {
			presenter.present(file)
		}
	}

	// Replaces the previous image: the directory holds only the one being shared.
	internal fun writeImage(image: ShareableImage): Path {
		FileSystem.SYSTEM.deleteRecursively(storageDirectory, mustExist = false)
		FileSystem.SYSTEM.createDirectories(storageDirectory)

		val file = storageDirectory.resolve(image.fileName)

		FileSystem.SYSTEM.write(file) {
			write(image.content)
		}

		return file
	}
}
