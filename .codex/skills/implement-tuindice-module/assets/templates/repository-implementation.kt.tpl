package $PACKAGE.data.repository

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class $DATA_REPOSITORY_NAME : $REPOSITORY_INTERFACE_NAME {
	override suspend fun getMessageFlow(): Flow<String> {
		return flowOf($READY_MESSAGE_LITERAL)
	}
}
