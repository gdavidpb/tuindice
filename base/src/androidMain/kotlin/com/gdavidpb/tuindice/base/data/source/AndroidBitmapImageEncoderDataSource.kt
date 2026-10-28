package com.gdavidpb.tuindice.base.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import androidx.core.graphics.createBitmap
import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// The orientations that turn the picture a quarter turn, or flip it along a diagonal: they swap its width and height.
private val swappingOrientations = setOf(
	ExifInterface.ORIENTATION_TRANSPOSE,
	ExifInterface.ORIENTATION_ROTATE_90,
	ExifInterface.ORIENTATION_TRANSVERSE,
	ExifInterface.ORIENTATION_ROTATE_270
)

class AndroidBitmapImageEncoderDataSource : ImageEncoderRepository {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
	}

	override suspend fun encode(file: PlatformFile, spec: ImageEncodingSpec): EncodedImage {
		val content = file.readBytes()
		val orientation = storedOrientation(content)
		val bounds = decodeBounds(content)
		val targetSize = bounds.turnedBy(orientation).targetSize(spec.maxDimensionPx)
		val decodedBitmap = decodeBitmap(content, bounds.sampleSize(spec.maxDimensionPx))
		val normalizedBitmap = decodedBitmap.toOpaqueScaledBitmap(
			orientation = orientation,
			width = targetSize.width,
			height = targetSize.height
		)

		try {
			val encodedBytes = normalizedBitmap.encodeJpeg(spec.jpegQuality)

			check(encodedBytes.size <= spec.maxBytes) {
				"Image exceeds the maximum size of ${spec.maxBytes} bytes."
			}

			return EncodedImage(
				content = encodedBytes,
				mimeType = Settings.JPEG_MIME_TYPE
			)
		} finally {
			normalizedBitmap.recycle()
			decodedBitmap.recycle()
		}
	}

	// The orientation the camera stored with the picture (EXIF 1 to 8), upright when it stored none or cannot be read.
	// BitmapFactory decodes the pixels as the sensor stored them and ignores the tag, so the picture is turned here.
	private fun storedOrientation(content: ByteArray): Int = runCatching {
		ExifInterface(ByteArrayInputStream(content)).getAttributeInt(
			ExifInterface.TAG_ORIENTATION,
			ExifInterface.ORIENTATION_NORMAL
		)
	}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

	private fun decodeBounds(content: ByteArray): BitmapBounds {
		val options = BitmapFactory.Options().apply {
			inJustDecodeBounds = true
		}

		BitmapFactory.decodeByteArray(content, 0, content.size, options)

		require(options.outWidth > 0 && options.outHeight > 0) {
			"Image content is not a supported image."
		}

		return BitmapBounds(
			width = options.outWidth,
			height = options.outHeight
		)
	}

	private fun decodeBitmap(content: ByteArray, sampleSize: Int): Bitmap {
		val options = BitmapFactory.Options().apply {
			inPreferredConfig = Bitmap.Config.ARGB_8888
			inSampleSize = sampleSize
		}

		return BitmapFactory.decodeByteArray(content, 0, content.size, options)
			?: throw IllegalArgumentException("Image content is not a supported image.")
	}

	// Draws the decoded pixels, turned by the stored orientation, onto a white opaque bitmap of the target size.
	private fun Bitmap.toOpaqueScaledBitmap(orientation: Int, width: Int, height: Int): Bitmap {
		val target = createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
		val swapped = orientation in swappingOrientations
		val turnedWidth = if (swapped) this.height else this.width
		val turnedHeight = if (swapped) this.width else this.height
		val matrix = orientationMatrix(
			orientation = orientation,
			sourceWidth = this.width.toFloat(),
			sourceHeight = this.height.toFloat()
		)

		matrix.postScale(width.toFloat() / turnedWidth, height.toFloat() / turnedHeight)

		Canvas(target).apply {
			drawColor(Color.WHITE)
			drawBitmap(this@toOpaqueScaledBitmap, matrix, paint)
		}

		return target
	}

	// The matrix that maps the stored pixels, sourceWidth by sourceHeight, onto the picture as it is seen: a stored point
	// (x, y) goes to (a·x + b·y + c, d·x + e·y + f), with the coefficients of its orientation, in Matrix's own order.
	private fun orientationMatrix(orientation: Int, sourceWidth: Float, sourceHeight: Float): Matrix {
		val coefficients = when (orientation) {
			ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> floatArrayOf(-1f, 0f, sourceWidth, 0f, 1f, 0f)
			ExifInterface.ORIENTATION_ROTATE_180 -> floatArrayOf(-1f, 0f, sourceWidth, 0f, -1f, sourceHeight)
			ExifInterface.ORIENTATION_FLIP_VERTICAL -> floatArrayOf(1f, 0f, 0f, 0f, -1f, sourceHeight)
			ExifInterface.ORIENTATION_TRANSPOSE -> floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f)
			ExifInterface.ORIENTATION_ROTATE_90 -> floatArrayOf(0f, -1f, sourceHeight, 1f, 0f, 0f)
			ExifInterface.ORIENTATION_TRANSVERSE -> floatArrayOf(0f, -1f, sourceHeight, -1f, 0f, sourceWidth)
			ExifInterface.ORIENTATION_ROTATE_270 -> floatArrayOf(0f, 1f, 0f, -1f, 0f, sourceWidth)
			else -> floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f)
		}

		return Matrix().apply {
			setValues(coefficients + floatArrayOf(0f, 0f, 1f))
		}
	}

	private fun Bitmap.encodeJpeg(quality: Int): ByteArray {
		val output = ByteArrayOutputStream()
		check(compress(Bitmap.CompressFormat.JPEG, quality, output)) {
			"Image encoding returned no data."
		}

		return output.toByteArray()
	}

	private data class BitmapBounds(
		val width: Int,
		val height: Int
	) {
		fun turnedBy(orientation: Int): BitmapBounds =
			if (orientation in swappingOrientations) BitmapBounds(width = height, height = width) else this

		fun targetSize(maxDimensionPx: Int): BitmapSize {
			val maxDimension = maxOf(width, height)
			val scaleFactor = if (maxDimension <= maxDimensionPx) {
				1.0
			} else {
				maxDimensionPx.toDouble() / maxDimension.toDouble()
			}

			return BitmapSize(
				width = maxOf(1, (width * scaleFactor).toInt()),
				height = maxOf(1, (height * scaleFactor).toInt())
			)
		}

		fun sampleSize(maxDimensionPx: Int): Int {
			var sampleSize = 1
			val halfWidth = width / 2
			val halfHeight = height / 2

			while (maxOf(halfWidth / sampleSize, halfHeight / sampleSize) >= maxDimensionPx) {
				sampleSize *= 2
			}

			return sampleSize
		}
	}

	private data class BitmapSize(
		val width: Int,
		val height: Int
	)
}
