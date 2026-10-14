package com.gdavidpb.tuindice.summary.data.repository.share

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse

interface ShareBackgroundRemoteDataRepository {
	suspend fun getShareBackgrounds(): List<ShareBackgroundResponse>
}
