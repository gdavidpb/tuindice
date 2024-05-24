package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import org.mobilenativefoundation.store.store5.Fetcher

class QuarterFetcher(
	private val remoteDataSource: RemoteDataSource
) : Fetcher<QuarterKey, List<RemoteQuarter>> by Fetcher.of(
	fetch = { key: QuarterKey ->
		require(key is QuarterKey.Read)

		when (key) {
			is QuarterKey.Read.All ->
				remoteDataSource.getQuarters()

			is QuarterKey.Read.ById ->
				listOfNotNull(remoteDataSource.getQuarter(qid = key.qid))
		}
	}
)