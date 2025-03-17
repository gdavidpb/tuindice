package com.gdavidpb.tuindice.summary.domain.repository

import java.io.InputStream

interface EncoderRepository {
	suspend fun encodePicture(path: String): InputStream
}