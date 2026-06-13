package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.pensum_local_data_warning_network
import tuindice.pensum.generated.resources.pensum_local_data_warning_service
import tuindice.pensum.generated.resources.pensum_local_data_warning_timeout
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumErrorMessagesTest {
	@Test
	fun toLocalDataWarningMessage_whenNotFound_thenReturnsServiceWarning() {
		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_service),
			UpdatePensumUseCaseError.NotFound.toLocalDataWarningMessage()
		)
	}

	@Test
	fun toLocalDataWarningMessage_whenUnavailable_thenReturnsServiceWarning() {
		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_service),
			UpdatePensumUseCaseError.Unavailable.toLocalDataWarningMessage()
		)
	}

	@Test
	fun toLocalDataWarningMessage_whenTimeout_thenReturnsTimeoutWarning() {
		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_timeout),
			UpdatePensumUseCaseError.Timeout.toLocalDataWarningMessage()
		)
	}

	@Test
	fun toLocalDataWarningMessage_whenNoConnectionWithoutNetwork_thenReturnsNetworkWarning() {
		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_network),
			UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = false).toLocalDataWarningMessage()
		)
	}

	@Test
	fun toLocalDataWarningMessage_whenNoConnectionWithNetwork_thenReturnsServiceWarning() {
		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_service),
			UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = true).toLocalDataWarningMessage()
		)
	}

	@Test
	fun toLocalDataWarningMessage_whenErrorIsUnhandled_thenFallsBackToServiceWarning() {
		val error: UpdatePensumUseCaseError? = null

		assertEquals(
			UiText.Resource(Res.string.pensum_local_data_warning_service),
			error.toLocalDataWarningMessage()
		)
	}

	@Test
	fun toFailedMessage_whenNotFound_thenReturnsGenericFailedMessage() {
		assertEquals(
			UiText.Resource(Res.string.pensum_failed_message),
			UpdatePensumUseCaseError.NotFound.toFailedMessage()
		)
	}

	@Test
	fun toFailedMessage_whenUnavailable_thenReturnsServiceUnavailableMessage() {
		assertEquals(
			UiText.Resource(Res.string.pensum_failed_service_unavailable),
			UpdatePensumUseCaseError.Unavailable.toFailedMessage()
		)
	}

	@Test
	fun toFailedMessage_whenTimeout_thenReturnsTimeoutMessage() {
		assertEquals(
			UiText.Resource(Res.string.pensum_failed_timeout),
			UpdatePensumUseCaseError.Timeout.toFailedMessage()
		)
	}

	@Test
	fun toFailedMessage_whenNoConnectionWithoutNetwork_thenReturnsNetworkUnavailableMessage() {
		assertEquals(
			UiText.Resource(Res.string.pensum_failed_network_unavailable),
			UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = false).toFailedMessage()
		)
	}

	@Test
	fun toFailedMessage_whenNoConnectionWithNetwork_thenReturnsServiceUnavailableMessage() {
		assertEquals(
			UiText.Resource(Res.string.pensum_failed_service_unavailable),
			UpdatePensumUseCaseError.NoConnection(isNetworkAvailable = true).toFailedMessage()
		)
	}

	@Test
	fun toFailedMessage_whenErrorIsUnhandled_thenFallsBackToGenericFailedMessage() {
		val error: UpdatePensumUseCaseError? = null

		assertEquals(
			UiText.Resource(Res.string.pensum_failed_message),
			error.toFailedMessage()
		)
	}
}
