package com.gdavidpb.tuindice.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.ProfilePictureError
import com.gdavidpb.tuindice.utils.PATH_PROFILE_PICTURES
import com.gdavidpb.tuindice.utils.QUALITY_PROFILE_PICTURE
import com.gdavidpb.tuindice.utils.SAMPLE_PROFILE_PICTURE
import com.gdavidpb.tuindice.utils.extensions.decodeScaleFactor
import com.gdavidpb.tuindice.utils.extensions.decodeScaledBitmap
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import com.gdavidpb.tuindice.utils.extensions.rotate
import java.io.File
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
open class UpdateProfilePictureUseCase(
        private val authRepository: AuthRepository,
        private val contentRepository: ContentRepository,
        private val storageRepository: StorageRepository<File>,
        private val remoteStorageRepository: RemoteStorageRepository,
        private val networkRepository: NetworkRepository
) : EventUseCase<Uri, String, ProfilePictureError>() {
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

        val fileOutputStream = storageRepository.outputStream(resource)

        fileOutputStream.use { outputStream ->
            val compress = bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_PROFILE_PICTURE, outputStream)

            if (compress)
                outputStream.flush()
            else
                throw RuntimeException("compress")
        }

        val fileInputStream = storageRepository.inputStream(resource)

        val downloadUrl = fileInputStream.use { inputStream ->
            remoteStorageRepository.uploadResource(resource, inputStream)
        }

        storageRepository.delete(resource)

        return downloadUrl.toString()
    }

    override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
        return when {
            throwable is IOException -> ProfilePictureError.IO
            throwable.isConnectionIssue() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}