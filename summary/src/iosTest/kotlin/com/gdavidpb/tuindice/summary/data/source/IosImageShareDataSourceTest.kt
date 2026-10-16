package com.gdavidpb.tuindice.summary.data.source

import androidx.compose.ui.graphics.ImageBitmap
import com.gdavidpb.tuindice.summary.data.repository.ImageSharePresenterDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.ui.image.toPngBytes
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class IosImageShareDataSourceTest {
	@Test
	fun shareImageWritesThePngUnderTheGivenDirectoryAndPresentsIt() = runTest {
		val directory = newTemporaryDirectory()
		val presenter = RecordingImageSharePresenter()
		val content = ImageBitmap(width = IMAGE_WIDTH, height = IMAGE_HEIGHT).toPngBytes()

		try {
			dataSource(directory, presenter).shareImage(ShareableImage(content = content, fileName = "statistics.png"))

			val presented = checkNotNull(presenter.presentedFiles.singleOrNull())

			assertEquals(directory.resolve("statistics.png"), presented)
			assertContentEquals(content, FileSystem.SYSTEM.read(presented) { readByteArray() })
			assertContentEquals(PNG_SIGNATURE, content.copyOf(PNG_SIGNATURE.size))
		} finally {
			FileSystem.SYSTEM.deleteRecursively(directory, mustExist = false)
		}
	}

	@Test
	fun shareImageReplacesTheImageWrittenBefore() = runTest {
		val directory = newTemporaryDirectory()
		val dataSource = dataSource(directory, RecordingImageSharePresenter())
		val content = ImageBitmap(width = IMAGE_WIDTH, height = IMAGE_HEIGHT).toPngBytes()

		try {
			dataSource.shareImage(ShareableImage(content = content, fileName = "first.png"))
			dataSource.shareImage(ShareableImage(content = content, fileName = "second.png"))

			assertEquals(listOf("second.png"), FileSystem.SYSTEM.list(directory).map { file -> file.name })
		} finally {
			FileSystem.SYSTEM.deleteRecursively(directory, mustExist = false)
		}
	}

	private fun dataSource(directory: Path, presenter: ImageSharePresenterDataRepository): IosImageShareDataSource {
		return IosImageShareDataSource(
			storageDirectory = directory,
			dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined),
			presenter = presenter
		)
	}

	private fun newTemporaryDirectory(): Path {
		val name = "tuindice-image-share-${Random.nextInt(TEMPORARY_NAME_BOUND)}"

		return FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve(name)
	}

	private class RecordingImageSharePresenter : ImageSharePresenterDataRepository {
		val presentedFiles = mutableListOf<Path>()

		override fun present(file: Path) {
			presentedFiles += file
		}
	}

	private companion object {
		const val IMAGE_WIDTH = 4
		const val IMAGE_HEIGHT = 3
		const val TEMPORARY_NAME_BOUND = 1_000_000
		val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
