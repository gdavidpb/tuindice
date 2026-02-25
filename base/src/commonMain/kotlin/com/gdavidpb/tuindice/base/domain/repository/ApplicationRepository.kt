package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef

interface ApplicationRepository : FileGateway {
	suspend fun clearData()
}
