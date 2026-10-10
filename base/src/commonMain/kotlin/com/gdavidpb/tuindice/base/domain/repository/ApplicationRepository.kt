package com.gdavidpb.tuindice.base.domain.repository

interface ApplicationRepository : FileRepository {
	suspend fun clearData()

	/**
	 * Removes only what a previous version left on the device for an account that already signed
	 * out. Asked by the start-up when there is no active session, never otherwise. Every holder is
	 * asked even if one fails; the first failure is thrown afterwards.
	 */
	suspend fun clearSessionResidue()
}
