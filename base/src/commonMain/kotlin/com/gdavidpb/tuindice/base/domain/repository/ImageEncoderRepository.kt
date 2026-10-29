package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import io.github.vinceglb.filekit.PlatformFile

interface ImageEncoderRepository {
	suspend fun encode(file: PlatformFile, spec: ImageEncodingSpec): EncodedImage
}
