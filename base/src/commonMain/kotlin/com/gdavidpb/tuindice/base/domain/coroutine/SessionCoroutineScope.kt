package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll

class SessionCoroutineScope(
	dispatchers: TuIndiceDispatchers
) : CoroutineScope {
	private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

	override val coroutineContext: CoroutineContext
		get() = scope.coroutineContext

	suspend fun cancelActiveWork() {
		val activeChildren = coroutineContext[Job]?.children?.toList().orEmpty()
		activeChildren.forEach { child -> child.cancel() }
		activeChildren.joinAll()
	}
}
