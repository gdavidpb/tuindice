package $PACKAGE.testing

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class $RECORDING_REPOSITORY_NAME(
	private val message: String = $READY_MESSAGE_LITERAL
) : $REPOSITORY_INTERFACE_NAME {
	val updateCalls = MutableStateFlow(0)

	override suspend fun observeMessageFlow(): Flow<String> = flowOf(message)

	override suspend fun updateMessage() {
		updateCalls.value += 1
	}
}
