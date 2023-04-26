package com.gdavidpb.tuindice.base.domain.repository

interface ApplicationRepository {
	suspend fun createFile(path: String): String
	suspend fun canOpenFile(path: String): Boolean
	suspend fun clearData()
}