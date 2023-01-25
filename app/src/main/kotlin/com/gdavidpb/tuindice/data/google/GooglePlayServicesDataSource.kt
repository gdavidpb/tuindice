package com.gdavidpb.tuindice.data.google

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class GooglePlayServicesDataSource(
	private val context: Context,
	private val googleApiAvailability: GoogleApiAvailability
) : MobileServicesRepository {
	override suspend fun getServicesStatus(): ServicesStatus {
		val status = googleApiAvailability.isGooglePlayServicesAvailable(context)

		return ServicesStatus(
			isAvailable = (status == ConnectionResult.SUCCESS),
			status = status
		)
	}
}