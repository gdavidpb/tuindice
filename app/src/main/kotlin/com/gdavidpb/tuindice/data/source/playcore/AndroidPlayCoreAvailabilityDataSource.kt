package com.gdavidpb.tuindice.data.source.playcore

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreEnvironmentDataRepository
import com.google.android.gms.common.ConnectionResult

class AndroidPlayCoreAvailabilityDataSource(
	private val environmentRepository: PlayCoreEnvironmentDataRepository,
	private val reportingRepository: ReportingRepository
) : PlayCoreAvailabilityDataRepository {
	override fun isAvailable(surface: PlayCoreSurface): Boolean {
		val googlePlayServicesStatus = getGooglePlayServicesStatus(surface) ?: return false
		val playStoreAvailable = isPlayStoreAvailable(surface)
		val playCoreServiceAvailable = isPlayCoreServiceAvailable(surface)

		reportAvailability(
			surface = surface,
			googlePlayServicesStatus = googlePlayServicesStatus,
			playStoreAvailable = playStoreAvailable,
			playCoreServiceAvailable = playCoreServiceAvailable
		)

		val isAvailable = googlePlayServicesStatus == ConnectionResult.SUCCESS &&
			playStoreAvailable &&
			playCoreServiceAvailable
		if (!isAvailable) {
			reportMessage(
				"play_core_unavailable " +
					"surface=${surface.logName} " +
					"google_play_services_status=$googlePlayServicesStatus " +
					"play_store_available=$playStoreAvailable " +
					"play_core_service_available=$playCoreServiceAvailable"
			)
		}

		return isAvailable
	}

	private fun getGooglePlayServicesStatus(surface: PlayCoreSurface): Int? =
		runCatching {
			environmentRepository.getGooglePlayServicesStatus()
		}.onFailure { throwable ->
			reportFailure(
				surface = surface,
				checkName = "google_play_services",
				throwable = throwable
			)
		}.getOrNull()

	private fun isPlayStoreAvailable(surface: PlayCoreSurface): Boolean =
		runCatching {
			environmentRepository.isPlayStoreAvailable()
		}.onFailure { throwable ->
			reportFailure(
				surface = surface,
				checkName = "play_store",
				throwable = throwable
			)
		}.getOrDefault(false)

	private fun isPlayCoreServiceAvailable(surface: PlayCoreSurface): Boolean =
		runCatching {
			environmentRepository.isPlayCoreServiceAvailable(surface)
		}.onFailure { throwable ->
			reportFailure(
				surface = surface,
				checkName = "play_core_service",
				throwable = throwable
			)
		}.getOrDefault(false)

	private fun reportAvailability(
		surface: PlayCoreSurface,
		googlePlayServicesStatus: Int,
		playStoreAvailable: Boolean,
		playCoreServiceAvailable: Boolean
	) {
		report {
			reportingRepository.setCustomKey(KEY_LAST_SURFACE, surface.logName)
			reportingRepository.setCustomKey(KEY_GOOGLE_PLAY_SERVICES_STATUS, googlePlayServicesStatus)
			reportingRepository.setCustomKey(KEY_PLAY_STORE_AVAILABLE, playStoreAvailable)
			reportingRepository.setCustomKey(KEY_PLAY_CORE_SERVICE_AVAILABLE, playCoreServiceAvailable)
		}
	}

	private fun reportFailure(
		surface: PlayCoreSurface,
		checkName: String,
		throwable: Throwable
	) {
		reportMessage("play_core_availability_check_failed surface=${surface.logName} check=$checkName")
		report {
			reportingRepository.setCustomKey(KEY_LAST_SURFACE, surface.logName)
			reportingRepository.logException(throwable)
		}
	}

	private fun reportMessage(message: String) {
		report {
			reportingRepository.logMessage(message)
		}
	}

	private fun report(block: () -> Unit) {
		runCatching(block)
	}

	companion object {
		private const val KEY_LAST_SURFACE = "play_core.last_surface"
		private const val KEY_GOOGLE_PLAY_SERVICES_STATUS = "play_core.google_play_services_status"
		private const val KEY_PLAY_STORE_AVAILABLE = "play_core.play_store_available"
		private const val KEY_PLAY_CORE_SERVICE_AVAILABLE = "play_core.service_available"
	}
}
