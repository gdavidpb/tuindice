package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef

interface FileGateway {
	suspend fun createTemporaryFile(nameHint: String): PlatformFileRef
	suspend fun canOpen(fileRef: PlatformFileRef): Boolean
}
