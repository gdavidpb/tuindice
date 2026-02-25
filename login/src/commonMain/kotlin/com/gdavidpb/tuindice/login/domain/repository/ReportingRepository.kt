package com.gdavidpb.tuindice.login.domain.repository

interface ReportingRepository {
	suspend fun setIdentifier(id: String)
}