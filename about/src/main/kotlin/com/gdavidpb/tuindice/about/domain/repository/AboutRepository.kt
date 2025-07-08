package com.gdavidpb.tuindice.about.domain.repository

interface AboutRepository {
	suspend fun getVersionDescription(): String
}