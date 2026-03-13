package $PACKAGE.testing

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class $RECORDING_REPOSITORY_NAME(
	private val message: String = $READY_MESSAGE_LITERAL
) : $REPOSITORY_INTERFACE_NAME {
	override suspend fun getMessageFlow(): Flow<String> = flowOf(message)
}
