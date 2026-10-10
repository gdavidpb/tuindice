package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao

/** The local caches [SyntheticTermCreationDataSource] reads: the selected pensum and the subject search. */
class SyntheticTermCreationCaches(
	val pensumCacheDao: PensumCacheDao,
	val pensumSelectionDao: PensumSelectionDao,
	val subjectCatalogCacheDao: SubjectCatalogCacheDao
)
