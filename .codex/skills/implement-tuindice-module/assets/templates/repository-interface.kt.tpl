package $PACKAGE.domain.repository

import kotlinx.coroutines.flow.Flow

interface $REPOSITORY_INTERFACE_NAME {
	suspend fun getMessageFlow(): Flow<String>
}
