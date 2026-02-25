package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformUri

interface ImageCodecGateway {
	suspend fun encodePicture(uri: PlatformUri): EncodedImage
}
