package com.gdavidpb.tuindice.base.data.source

import android.media.ExifInterface
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Each test turns the marked picture by one EXIF orientation and samples the centre of the marker, which the
// orientation moves. The expected size and sample come from the orientation's definition, so each orientation puts
// the marker in a corner of its own among the pictures of its shape. A wrong turn or size fails its test.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidBitmapImageEncoderOrientationTest {
	private val dataSource = AndroidBitmapImageEncoderDataSource()

	@Test
	fun encodeTurnsAPictureStoredOnItsSideByItsExifOrientation() = runTest {
		// A quarter turn clockwise: the marker moves to the top right corner, and the sides swap.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_ROTATE_90,
			expectedWidth = MARKED_SHORT_PX,
			expectedHeight = MARKED_LONG_PX,
			sampleX = MARKED_SHORT_PX - 1 - SAMPLE_Y,
			sampleY = SAMPLE_X
		)
	}

	@Test
	fun encodeTurnsAPictureUpsideDownByItsExifOrientation() = runTest {
		// Turned half a turn: the marker moves to the bottom right corner.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_ROTATE_180,
			expectedWidth = MARKED_LONG_PX,
			expectedHeight = MARKED_SHORT_PX,
			sampleX = MARKED_LONG_PX - 1 - SAMPLE_X,
			sampleY = MARKED_SHORT_PX - 1 - SAMPLE_Y
		)
	}

	@Test
	fun encodeMirrorsAPictureLeftToRightByItsExifOrientation() = runTest {
		// Mirrored left to right: the marker moves to the top right corner, and the sides keep their places.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
			expectedWidth = MARKED_LONG_PX,
			expectedHeight = MARKED_SHORT_PX,
			sampleX = MARKED_LONG_PX - 1 - SAMPLE_X,
			sampleY = SAMPLE_Y
		)
	}

	@Test
	fun encodeMirrorsAPictureTopToBottomByItsExifOrientation() = runTest {
		// Mirrored top to bottom: the marker moves to the bottom left corner, and the sides keep their places.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_FLIP_VERTICAL,
			expectedWidth = MARKED_LONG_PX,
			expectedHeight = MARKED_SHORT_PX,
			sampleX = SAMPLE_X,
			sampleY = MARKED_SHORT_PX - 1 - SAMPLE_Y
		)
	}

	@Test
	fun encodeTransposesAPictureByItsExifOrientation() = runTest {
		// Transposed: the sides swap, and the marker stays in the top left corner of the new picture.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_TRANSPOSE,
			expectedWidth = MARKED_SHORT_PX,
			expectedHeight = MARKED_LONG_PX,
			sampleX = SAMPLE_Y,
			sampleY = SAMPLE_X
		)
	}

	@Test
	fun encodeFlipsAPictureAlongItsAntiDiagonalByItsExifOrientation() = runTest {
		// Flipped along its anti-diagonal: the sides swap, and the marker moves to the bottom right corner.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_TRANSVERSE,
			expectedWidth = MARKED_SHORT_PX,
			expectedHeight = MARKED_LONG_PX,
			sampleX = MARKED_SHORT_PX - 1 - SAMPLE_Y,
			sampleY = MARKED_LONG_PX - 1 - SAMPLE_X
		)
	}

	@Test
	fun encodeTurnsAPictureACounterClockwiseQuarterTurnByItsExifOrientation() = runTest {
		// A quarter turn counter-clockwise: the sides swap, and the marker moves to the bottom left corner.
		assertMarkedPictureAfter(
			orientation = ExifInterface.ORIENTATION_ROTATE_270,
			expectedWidth = MARKED_SHORT_PX,
			expectedHeight = MARKED_LONG_PX,
			sampleX = SAMPLE_Y,
			sampleY = MARKED_LONG_PX - 1 - SAMPLE_X
		)
	}

	@Test
	fun encodeKeepsThePictureAsStoredWhenItsExifOrientationIsOutsideTheStandard() = runTest {
		// An orientation the standard does not define leaves the picture as it is stored.
		assertMarkedPictureAfter(
			orientation = UNKNOWN_ORIENTATION,
			expectedWidth = MARKED_LONG_PX,
			expectedHeight = MARKED_SHORT_PX,
			sampleX = SAMPLE_X,
			sampleY = SAMPLE_Y
		)
	}

	// Encodes the marked picture as the camera stored it, turned by the orientation, and checks its size and the colour
	// of the sampled point.
	private suspend fun assertMarkedPictureAfter(
		orientation: Int,
		expectedWidth: Int,
		expectedHeight: Int,
		sampleX: Int,
		sampleY: Int
	) {
		val inputFile = createInputFile(
			prefix = "marked",
			extension = "jpg",
			content = withExifOrientation(markedJpegBytes(), orientation)
		)

		try {
			val encodedImage = dataSource.encode(file = PlatformFile(inputFile), spec = defaultSpec)
			val decodedImage = decodeImage(encodedImage.content)

			try {
				assertEquals(expectedWidth, decodedImage.width)
				assertEquals(expectedHeight, decodedImage.height)
				assertTrue(
					isRed(decodedImage.getPixel(sampleX, sampleY)),
					"Orientation $orientation: the marker is not at ($sampleX, $sampleY)."
				)
			} finally {
				decodedImage.recycle()
			}
		} finally {
			inputFile.delete()
		}
	}

	private companion object {
		// The centre of the marker in the stored picture: the point each test samples before the orientation moves it.
		const val SAMPLE_X = MARKER_WIDTH_PX / 2
		const val SAMPLE_Y = MARKER_HEIGHT_PX / 2
		const val UNKNOWN_ORIENTATION = 9
		const val MAX_BYTES = 1_048_576

		val defaultSpec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = MAX_BYTES, jpegQuality = 85)
	}
}
