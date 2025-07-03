package com.gdavidpb.tuindice.login.data.repository

interface ReportingDataSource {
	suspend fun setIdentifier(id: String)
}