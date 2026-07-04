package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.domain.usecase.EnsureMessagingSubscribedUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PushTokenRotationHandler(
	private val ensureMessagingSubscribedUseCase: EnsureMessagingSubscribedUseCase,
	private val sessionCoroutineScope: SessionCoroutineScope
) {
	fun onPushTokenRotated() {
		sessionCoroutineScope.launch {
			ensureMessagingSubscribedUseCase.execute(Unit).collect()
		}
	}
}
