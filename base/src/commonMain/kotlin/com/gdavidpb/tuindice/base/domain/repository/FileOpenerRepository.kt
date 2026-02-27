package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef

interface FileOpenerRepository {
	fun openFile(fileRef: PlatformFileRef): Boolean
}
