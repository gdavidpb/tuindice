package com.gdavidpb.tuindice.summary.utils

import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec

object ProfilePictureEncoding {
	// The profile picture as the server takes it: at most 1024 px on its longest side and 1 MB, as JPEG of quality 85.
	val SPEC = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 1_048_576, jpegQuality = 85)
}
