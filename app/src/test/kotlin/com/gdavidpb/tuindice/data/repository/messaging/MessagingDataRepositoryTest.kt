package com.gdavidpb.tuindice.data.repository.messaging

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessagingDataRepositoryTest {
	@Test
	fun subscribe_withFreshToken_postsAndPersistsToken() = runBlocking {
		val local = FakeLocalDataSource()
		val remote = FakeRemoteDataSource()
		val provider = FakeProviderDataSource(token = "token-1")
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = provider
		)

		repository.subscribe()

		assertEquals(listOf("token-1"), remote.subscribeCalls)
		assertEquals(true, local.isSubscribed())
		assertEquals("token-1", local.getSubscribedToken())
	}

	@Test
	fun subscribe_withSameTokenAndSubscribed_skipsRemoteCall() = runBlocking {
		val local = FakeLocalDataSource().apply {
			subscribed = true
			subscribedToken = "token-1"
		}
		val remote = FakeRemoteDataSource()
		val provider = FakeProviderDataSource(token = "token-1")
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = provider
		)

		repository.subscribe()

		assertEquals(emptyList<String>(), remote.subscribeCalls)
		assertEquals("token-1", local.subscribedToken)
	}

	@Test
	fun subscribe_withRotatedToken_reSubscribesAndUpdatesStoredToken() = runBlocking {
		val local = FakeLocalDataSource().apply {
			subscribed = true
			subscribedToken = "token-1"
		}
		val remote = FakeRemoteDataSource()
		val provider = FakeProviderDataSource(token = "token-2")
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = provider
		)

		repository.subscribe()

		assertEquals(listOf("token-2"), remote.subscribeCalls)
		assertEquals("token-2", local.subscribedToken)
	}

	@Test
	fun subscribe_withBlankToken_skipsRemoteCallAndDoesNotPersistSubscription() = runBlocking {
		val local = FakeLocalDataSource()
		val remote = FakeRemoteDataSource()
		val provider = FakeProviderDataSource(token = "   ")
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = provider
		)

		repository.subscribe()

		assertEquals(emptyList<String>(), remote.subscribeCalls)
		assertEquals(false, local.isSubscribed())
		assertNull(local.getSubscribedToken())
	}

	@Test
	fun unsubscribe_callsRemoteAndClearsLocalSubscription() = runBlocking {
		val local = FakeLocalDataSource().apply {
			subscribed = true
			subscribedToken = "token-1"
		}
		val remote = FakeRemoteDataSource()
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = FakeProviderDataSource(token = "token-1")
		)

		repository.unsubscribe()

		assertEquals(1, remote.unsubscribeCalls)
		assertEquals(false, local.subscribed)
		assertNull(local.subscribedToken)
	}

	@Test
	fun unsubscribe_whenRemoteFails_stillClearsLocalSubscription() = runBlocking {
		val local = FakeLocalDataSource().apply {
			subscribed = true
			subscribedToken = "token-1"
		}
		val remote = FakeRemoteDataSource().apply {
			unsubscribeError = IllegalStateException("network-error")
		}
		val repository = MessagingDataRepository(
			localDataSource = local,
			remoteDataSource = remote,
			providerDataSource = FakeProviderDataSource(token = "token-1")
		)

		repository.unsubscribe()

		assertEquals(1, remote.unsubscribeCalls)
		assertEquals(false, local.subscribed)
		assertNull(local.subscribedToken)
	}
}

private class FakeLocalDataSource : LocalDataSource {
	var subscribed: Boolean = false
	var subscribedToken: String? = null

	override suspend fun isSubscribed(): Boolean = subscribed

	override suspend fun getSubscribedToken(): String? = subscribedToken

	override suspend fun markAsSubscribed(token: String) {
		subscribed = true
		subscribedToken = token
	}

	override suspend fun clearSubscription() {
		subscribed = false
		subscribedToken = null
	}
}

private class FakeRemoteDataSource : RemoteDataSource {
	val subscribeCalls = mutableListOf<String>()
	var unsubscribeCalls: Int = 0
	var unsubscribeError: Throwable? = null

	override suspend fun subscribe(messagingToken: String) {
		subscribeCalls += messagingToken
	}

	override suspend fun unsubscribe() {
		unsubscribeCalls++
		unsubscribeError?.let { throwable ->
			throw throwable
		}
	}
}

private class FakeProviderDataSource(
	private val token: String?
) : ProviderDataSource {
	override suspend fun getToken(): String? = token
}
