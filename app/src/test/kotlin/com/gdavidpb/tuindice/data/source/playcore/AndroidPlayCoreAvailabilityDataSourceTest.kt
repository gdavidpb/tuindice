package com.gdavidpb.tuindice.data.source.playcore

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreEnvironmentDataRepository
import com.google.android.gms.common.ConnectionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPlayCoreAvailabilityDataSourceTest {
	@Test
	fun isAvailable_whenGooglePlayServicesAndPlayStoreAreAvailable_returnsTrue() {
		val reportingRepository = RecordingReportingRepository()
		val dataSource = AndroidPlayCoreAvailabilityDataSource(
			environmentRepository = FakePlayCoreEnvironmentDataRepository(
				googlePlayServicesStatus = ConnectionResult.SUCCESS,
				playStoreAvailable = true,
				playCoreServiceAvailable = true
			),
			reportingRepository = reportingRepository,
		)

		assertTrue(dataSource.isAvailable(PlayCoreSurface.Review))

		assertEquals("review", reportingRepository.customKeys["play_core.last_surface"])
		assertEquals(ConnectionResult.SUCCESS, reportingRepository.customKeys["play_core.google_play_services_status"])
		assertEquals(true, reportingRepository.customKeys["play_core.play_store_available"])
		assertEquals(true, reportingRepository.customKeys["play_core.service_available"])
		assertTrue(reportingRepository.loggedMessages.isEmpty())
		assertTrue(reportingRepository.loggedExceptions.isEmpty())
	}

	@Test
	fun isAvailable_whenGooglePlayServicesIsUnavailable_returnsFalseAndReportsState() {
		val reportingRepository = RecordingReportingRepository()
		val dataSource = AndroidPlayCoreAvailabilityDataSource(
			environmentRepository = FakePlayCoreEnvironmentDataRepository(
				googlePlayServicesStatus = ConnectionResult.SERVICE_MISSING,
				playStoreAvailable = true,
				playCoreServiceAvailable = true
			),
			reportingRepository = reportingRepository,
		)

		assertFalse(dataSource.isAvailable(PlayCoreSurface.Update))

		assertEquals("update", reportingRepository.customKeys["play_core.last_surface"])
		assertEquals(ConnectionResult.SERVICE_MISSING, reportingRepository.customKeys["play_core.google_play_services_status"])
		assertEquals(true, reportingRepository.customKeys["play_core.play_store_available"])
		assertEquals(true, reportingRepository.customKeys["play_core.service_available"])
		assertEquals(
			listOf(
				"play_core_unavailable " +
					"surface=update " +
					"google_play_services_status=${ConnectionResult.SERVICE_MISSING} " +
					"play_store_available=true " +
					"play_core_service_available=true"
			),
			reportingRepository.loggedMessages
		)
		assertTrue(reportingRepository.loggedExceptions.isEmpty())
	}

	@Test
	fun isAvailable_whenPlayStoreIsMissing_returnsFalseWithoutReportingException() {
		val reportingRepository = RecordingReportingRepository()
		val dataSource = AndroidPlayCoreAvailabilityDataSource(
			environmentRepository = FakePlayCoreEnvironmentDataRepository(
				googlePlayServicesStatus = ConnectionResult.SUCCESS,
				playStoreAvailable = false,
				playCoreServiceAvailable = false
			),
			reportingRepository = reportingRepository,
		)

		assertFalse(dataSource.isAvailable(PlayCoreSurface.Review))

		assertEquals("review", reportingRepository.customKeys["play_core.last_surface"])
		assertEquals(ConnectionResult.SUCCESS, reportingRepository.customKeys["play_core.google_play_services_status"])
		assertEquals(false, reportingRepository.customKeys["play_core.play_store_available"])
		assertEquals(false, reportingRepository.customKeys["play_core.service_available"])
		assertEquals(
			listOf(
				"play_core_unavailable " +
					"surface=review " +
					"google_play_services_status=${ConnectionResult.SUCCESS} " +
					"play_store_available=false " +
					"play_core_service_available=false"
			),
			reportingRepository.loggedMessages
		)
		assertTrue(reportingRepository.loggedExceptions.isEmpty())
	}

	@Test
	fun isAvailable_whenPlayStoreCheckFails_returnsFalseAndReportsException() {
		val failure = IllegalStateException("package manager failed")
		val reportingRepository = RecordingReportingRepository()
		val dataSource = AndroidPlayCoreAvailabilityDataSource(
			environmentRepository = FakePlayCoreEnvironmentDataRepository(
				googlePlayServicesStatus = ConnectionResult.SUCCESS,
				playStoreAvailableFailure = failure,
				playCoreServiceAvailable = true
			),
			reportingRepository = reportingRepository,
		)

		assertFalse(dataSource.isAvailable(PlayCoreSurface.Review))

		assertEquals("review", reportingRepository.customKeys["play_core.last_surface"])
		assertEquals(ConnectionResult.SUCCESS, reportingRepository.customKeys["play_core.google_play_services_status"])
		assertEquals(false, reportingRepository.customKeys["play_core.play_store_available"])
		assertEquals(true, reportingRepository.customKeys["play_core.service_available"])
		assertEquals(
			listOf(
				"play_core_availability_check_failed surface=review check=play_store",
				"play_core_unavailable " +
					"surface=review " +
					"google_play_services_status=${ConnectionResult.SUCCESS} " +
					"play_store_available=false " +
					"play_core_service_available=true"
			),
			reportingRepository.loggedMessages
		)
		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertSame(failure, reportingRepository.loggedExceptions.single())
	}

	@Test
	fun isAvailable_whenPlayCoreServiceIsMissing_returnsFalseAndReportsState() {
		val reportingRepository = RecordingReportingRepository()
		val environmentRepository = FakePlayCoreEnvironmentDataRepository(
			googlePlayServicesStatus = ConnectionResult.SUCCESS,
			playStoreAvailable = true,
			playCoreServiceAvailable = false
		)
		val dataSource = AndroidPlayCoreAvailabilityDataSource(
			environmentRepository = environmentRepository,
			reportingRepository = reportingRepository,
		)

		assertFalse(dataSource.isAvailable(PlayCoreSurface.Review))

		assertEquals("review", reportingRepository.customKeys["play_core.last_surface"])
		assertEquals(listOf(PlayCoreSurface.Review), environmentRepository.playCoreServiceCalls)
		assertEquals(ConnectionResult.SUCCESS, reportingRepository.customKeys["play_core.google_play_services_status"])
		assertEquals(true, reportingRepository.customKeys["play_core.play_store_available"])
		assertEquals(false, reportingRepository.customKeys["play_core.service_available"])
		assertEquals(
			listOf(
				"play_core_unavailable " +
					"surface=review " +
					"google_play_services_status=${ConnectionResult.SUCCESS} " +
					"play_store_available=true " +
					"play_core_service_available=false"
			),
			reportingRepository.loggedMessages
		)
		assertTrue(reportingRepository.loggedExceptions.isEmpty())
	}
}

class RecordingReportingRepository : ReportingRepository {
	var lastIdentifier: String? = null
	val loggedExceptions = mutableListOf<Throwable>()
	val loggedMessages = mutableListOf<String>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		lastIdentifier = identifier
	}

	override fun logException(throwable: Throwable) {
		loggedExceptions += throwable
	}

	override fun logMessage(message: String) {
		loggedMessages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class FakePlayCoreAvailabilityDataRepository(
	private val available: Boolean
) : PlayCoreAvailabilityDataRepository {
	val calls = mutableListOf<PlayCoreSurface>()

	override fun isAvailable(surface: PlayCoreSurface): Boolean {
		calls += surface
		return available
	}
}

private class FakePlayCoreEnvironmentDataRepository(
	private val googlePlayServicesStatus: Int = ConnectionResult.SUCCESS,
	private val googlePlayServicesStatusFailure: Throwable? = null,
	private val playStoreAvailable: Boolean = true,
	private val playStoreAvailableFailure: Throwable? = null,
	private val playCoreServiceAvailable: Boolean = true,
	private val playCoreServiceAvailableFailure: Throwable? = null
) : PlayCoreEnvironmentDataRepository {
	val playCoreServiceCalls = mutableListOf<PlayCoreSurface>()

	override fun getGooglePlayServicesStatus(): Int {
		googlePlayServicesStatusFailure?.let { throwable -> throw throwable }
		return googlePlayServicesStatus
	}

	override fun isPlayStoreAvailable(): Boolean {
		playStoreAvailableFailure?.let { throwable -> throw throwable }
		return playStoreAvailable
	}

	override fun isPlayCoreServiceAvailable(surface: PlayCoreSurface): Boolean {
		playCoreServiceAvailableFailure?.let { throwable -> throw throwable }
		playCoreServiceCalls += surface
		return playCoreServiceAvailable
	}
}
