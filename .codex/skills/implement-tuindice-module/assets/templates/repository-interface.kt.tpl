package $PACKAGE.domain.repository

import kotlinx.coroutines.flow.Flow

interface $REPOSITORY_INTERFACE_NAME {
	suspend fun observeMessageFlow(): Flow<String>
	suspend fun updateMessage()
}
