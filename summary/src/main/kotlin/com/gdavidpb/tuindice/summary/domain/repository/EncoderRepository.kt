package com.gdavidpb.tuindice.summary.domain.repository

interface EncoderRepository {
	suspend fun encodePicture(path: String): String
}