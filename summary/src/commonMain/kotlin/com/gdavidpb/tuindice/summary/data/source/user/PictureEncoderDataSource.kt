package com.gdavidpb.tuindice.summary.data.source.user

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import io.github.vinceglb.filekit.PlatformFile

interface PictureEncoderDataSource {
	suspend fun encodePicture(file: PlatformFile): EncodedImage
}
