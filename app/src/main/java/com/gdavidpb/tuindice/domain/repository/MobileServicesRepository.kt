package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.ServicesStatus

interface MobileServicesRepository {
	suspend fun getServicesStatus(): ServicesStatus
}