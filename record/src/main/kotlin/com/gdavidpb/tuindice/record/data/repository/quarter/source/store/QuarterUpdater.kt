package com.gdavidpb.tuindice.record.data.repository.quarter.source.store

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toRemoteQuarter
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

class QuarterUpdater(
	private val remoteDataSource: RemoteDataSource
) : Updater<QuarterKey, List<Quarter>, QuarterWriteResponse> by Updater.by(
	post = { key, input ->
		require(key is QuarterKey.Write || key is QuarterKey.Remove)

		runCatching {
			when (key) {
				is QuarterKey.Write.SaveAll ->
					if (key.dispatchToRemote)
						remoteDataSource
							.addQuarters(quarters = input.map { quarter -> quarter.toRemoteQuarter() })

				is QuarterKey.Remove.ById ->
					remoteDataSource
						.removeQuarter(qid = key.qid)

				else ->
					throw IllegalStateException()
			}
		}.fold(
			onSuccess = {
				UpdaterResult.Success.Typed(
					value = QuarterWriteResponse(key)
				)
			},
			onFailure = { throwable ->
				UpdaterResult.Error.Exception(throwable)
			}
		)
	}
)