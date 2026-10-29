package com.gdavidpb.tuindice.base.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertNotNull

// The marked picture the encoder tests start from: blue, MARKED_LONG_PX wide and MARKED_SHORT_PX high, with a red
// marker of MARKER_WIDTH_PX by MARKER_HEIGHT_PX in its top-left corner.
// Its sides differ, so a turn or a transposition shows.
internal const val MARKED_LONG_PX = 200
internal const val MARKED_SHORT_PX = 100
internal const val MARKER_WIDTH_PX = 40
internal const val MARKER_HEIGHT_PX = 20

private const val PNG_QUALITY = 100
private const val JPEG_QUALITY_MAX = 100
private const val TEST_COLOR_RED = 68
private const val TEST_COLOR_GREEN = 119
private const val TEST_COLOR_BLUE = 153
private const val STRONG_CHANNEL = 200
private const val WEAK_CHANNEL = 60

// The start of image marker of a JPEG, and the Exif segment's marker and its length field, as the file lays them out.
private const val JPEG_START_LENGTH = 2
private const val APP1_MARKER_PREFIX: Byte = -1
private const val APP1_MARKER: Byte = -31
private const val LENGTH_FIELD_LENGTH = 2

// The TIFF block of the Exif segment: a big-endian header, and one IFD that holds the orientation tag alone.
private const val TIFF_LENGTH = 26
private const val TIFF_MAGIC: Short = 42
private const val FIRST_IFD_OFFSET = 8
private const val ONE_ENTRY: Short = 1
private const val ORIENTATION_TAG: Short = 0x0112
private const val SHORT_TYPE: Short = 3
private const val ONE_VALUE = 1
private const val PADDING: Short = 0
private const val NO_NEXT_IFD = 0

private val BIG_ENDIAN_MARK = "MM".encodeToByteArray()
private val EXIF_HEADER = "Exif\u0000\u0000".encodeToByteArray()
private val APP1_PREFIX = byteArrayOf(APP1_MARKER_PREFIX, APP1_MARKER)

internal fun createInputFile(
	prefix: String,
	extension: String,
	content: ByteArray
): File {
	return File.createTempFile("${prefix}_", ".$extension").apply {
		writeBytes(content)
		deleteOnExit()
	}
}

internal fun createPngBytes(width: Int, height: Int): ByteArray {
	val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

	try {
		bitmap.eraseColor(Color.rgb(TEST_COLOR_RED, TEST_COLOR_GREEN, TEST_COLOR_BLUE))

		val output = ByteArrayOutputStream()
		check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)) {
			"Expected a PNG-encoded image for tests."
		}

		return output.toByteArray()
	} finally {
		bitmap.recycle()
	}
}

// The marked picture as a JPEG the camera could have written, without any Exif data.
internal fun markedJpegBytes(): ByteArray {
	val bitmap = Bitmap.createBitmap(MARKED_LONG_PX, MARKED_SHORT_PX, Bitmap.Config.ARGB_8888)

	try {
		bitmap.eraseColor(Color.BLUE)
		Canvas(bitmap).drawRect(
			0f,
			0f,
			MARKER_WIDTH_PX.toFloat(),
			MARKER_HEIGHT_PX.toFloat(),
			Paint().apply { color = Color.RED }
		)

		return jpegBytes(bitmap)
	} finally {
		bitmap.recycle()
	}
}

private fun jpegBytes(bitmap: Bitmap): ByteArray {
	val output = ByteArrayOutputStream()
	check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY_MAX, output)) {
		"Expected a JPEG-encoded image for tests."
	}

	return output.toByteArray()
}

// The JPEG with the Exif segment a camera writes right after the start of image, turned by its orientation tag.
internal fun withExifOrientation(jpeg: ByteArray, orientation: Int): ByteArray =
	jpeg.copyOfRange(0, JPEG_START_LENGTH) +
		exifSegment(orientation) +
		jpeg.copyOfRange(JPEG_START_LENGTH, jpeg.size)

private fun exifSegment(orientation: Int): ByteArray {
	val tiff = ByteBuffer.allocate(TIFF_LENGTH)
		.order(ByteOrder.BIG_ENDIAN)
		.put(BIG_ENDIAN_MARK)
		.putShort(TIFF_MAGIC)
		.putInt(FIRST_IFD_OFFSET)
		.putShort(ONE_ENTRY)
		.putShort(ORIENTATION_TAG)
		.putShort(SHORT_TYPE)
		.putInt(ONE_VALUE)
		.putShort(orientation.toShort())
		.putShort(PADDING)
		.putInt(NO_NEXT_IFD)
		.array()
	val payload = EXIF_HEADER + tiff

	return ByteBuffer.allocate(APP1_PREFIX.size + LENGTH_FIELD_LENGTH + payload.size)
		.order(ByteOrder.BIG_ENDIAN)
		.put(APP1_PREFIX)
		.putShort((LENGTH_FIELD_LENGTH + payload.size).toShort())
		.put(payload)
		.array()
}

internal fun decodeImage(content: ByteArray): Bitmap =
	assertNotNull(BitmapFactory.decodeByteArray(content, 0, content.size))

internal fun isRed(pixel: Int): Boolean =
	Color.red(pixel) > STRONG_CHANNEL && Color.green(pixel) < WEAK_CHANNEL && Color.blue(pixel) < WEAK_CHANNEL
