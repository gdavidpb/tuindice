package com.gdavidpb.tuindice.testkit.coroutines

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

fun testSessionCoroutineScope(
	dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
): SessionCoroutineScope = SessionCoroutineScope(TestTuIndiceDispatchers(dispatcher))
