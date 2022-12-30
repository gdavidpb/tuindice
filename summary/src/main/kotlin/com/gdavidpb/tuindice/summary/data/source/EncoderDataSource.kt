package com.gdavidpb.tuindice.summary.data.source

interface EncoderDataSource {
	suspend fun encodePicture(path: String): String
}