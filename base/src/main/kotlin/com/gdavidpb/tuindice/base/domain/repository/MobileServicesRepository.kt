package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus

interface MobileServicesRepository {
	suspend fun getServicesStatus(): ServicesStatus
}