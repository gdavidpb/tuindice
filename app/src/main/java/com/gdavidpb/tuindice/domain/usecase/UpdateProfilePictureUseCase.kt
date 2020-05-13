package com.gdavidpb.tuindice.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.ContentRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.QUALITY_PROFILE_PICTURE
import com.gdavidpb.tuindice.utils.SAMPLE_PROFILE_PICTURE
import com.gdavidpb.tuindice.utils.extensions.decodeScaleFactor
import com.gdavidpb.tuindice.utils.extensions.decodeScaledBitmap
import com.gdavidpb.tuindice.utils.extensions.rotate
import java.io.File

open class UpdateProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val contentRepository: ContentRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val remoteStorageRepository: RemoteStorageRepository
) : EventUseCase<Uri, String>() {
    override suspend fun executeOnBackground(params: Uri): String {
        val activeUId = authRepository.getActiveAuth().uid
        val resource = File(PATH_PROFILE_PICTURES, "$activeUId.jpg").path

        val rotationDegrees = contentRepository.openInputStream(params).use { inputStream ->
            ExifInterface(inputStream).rotationDegrees.toFloat()
        }

        val scaleFactor = contentRepository.openInputStream(params).use { inputStream ->
            inputStream.decodeScaleFactor(SAMPLE_PROFILE_PICTURE)
        }

        val bitmap = contentRepository.openInputStream(params).use { inputStream ->
            inputStream
                    .decodeScaledBitmap(scaleFactor)
                    .rotate(rotationDegrees)
        }

        val fileOutputStream = localStorageRepository.outputStream(resource)

        fileOutputStream.use { outputStream ->
            val compress = bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_PROFILE_PICTURE, outputStream)

            if (compress)
                outputStream.flush()
            else
                throw RuntimeException("compress")
        }

        val fileInputStream = localStorageRepository.inputStream(resource)

        val downloadUrl = fileInputStream.use { inputStream ->
            remoteStorageRepository.uploadResource(resource, inputStream)
        }

        localStorageRepository.delete(resource)

        return downloadUrl.toString()
    }
}