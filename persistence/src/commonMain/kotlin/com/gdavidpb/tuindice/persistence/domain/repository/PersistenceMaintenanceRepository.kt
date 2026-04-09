package com.gdavidpb.tuindice.persistence.domain.repository

interface PersistenceMaintenanceRepository {
	suspend fun clearAll()
}
