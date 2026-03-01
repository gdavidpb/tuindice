package com.gdavidpb.tuindice.summary.data.repository.user

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformUri

interface PictureEncoderDataSource {
	suspend fun encodePicture(uri: PlatformUri): EncodedImage
}
