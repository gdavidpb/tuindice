package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class ResolveResourceUseCase(
        private val remoteRepository: RemoteStorageRepository,
        private val settingsRepository: SettingsRepository
) : SingleUseCase<Uri, String>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: String): Single<Uri> {
        //todo cache?
        //val url = settingsRepository.get<String>(key = params)

        //return if (url.isNotEmpty())
        //Single.just(Uri.parse(url))
        //else
        return remoteRepository.resolveResource(resource = params)
                .doAfterSuccess {
                    //settingsRepository.set(key = params, value = "$it")
                }
    }
}