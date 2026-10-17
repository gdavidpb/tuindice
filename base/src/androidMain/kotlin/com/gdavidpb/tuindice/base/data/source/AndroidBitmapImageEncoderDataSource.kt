package com.gdavidpb.tuindice.base.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import java.io.ByteArrayOutputStream

class AndroidBitmapImageEncoderDataSource : ImageEncoderRepository {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
	}

	override suspend fun encode(file: PlatformFile, spec: ImageEncodingSpec): EncodedImage {
		val content = file.readBytes()
		val bounds = decodeBounds(content)
		val targetSize = bounds.targetSize(spec.maxDimensionPx)
		val decodedBitmap = decodeBitmap(content, bounds.sampleSize(spec.maxDimensionPx))
		val normalizedBitmap = decodedBitmap.toOpaqueScaledBitmap(
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

	private fun Bitmap.toOpaqueScaledBitmap(width: Int, height: Int): Bitmap {
		val target = createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

		Canvas(target).apply {
			drawColor(Color.WHITE)
			drawBitmap(
				this@toOpaqueScaledBitmap,
				null,
				Rect(0, 0, width, height),
				paint
			)
		}

		return target
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
