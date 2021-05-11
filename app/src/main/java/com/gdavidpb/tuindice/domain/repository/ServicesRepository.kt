package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.ServicesStatus

interface ServicesRepository {
	suspend fun getServicesStatus(): ServicesStatus
}