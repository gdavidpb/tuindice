package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview

interface SyntheticTermLoadPreviewRepository {
	suspend fun loadSyntheticTermPreview(subjectCodes: List<String>): SyntheticTermLoadPreview
}
