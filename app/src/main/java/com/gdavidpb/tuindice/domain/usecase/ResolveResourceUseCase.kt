package com.gdavidpb.tuindice.domain.usecase

import android.net.Uri
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class ResolveResourceUseCase(
        private val storageRepository: RemoteStorageRepository,
        private val settingsRepository: SettingsRepository
) : SingleUseCase<Uri, String>(
        Schedulers.io(),
        AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: String): Single<Uri> {
        val url = settingsRepository.getPrivacyPolicyUrl()

        return if (url.isNotEmpty())
            Single.just(Uri.parse(url))
        else
            storageRepository.resolveResource(resource = params)
                    .doOnSuccess {
                        settingsRepository.setPrivacyPolicyUrl("$it")
                    }.doOnError {
                        settingsRepository.clearPrivacyPolicyUrl()
                    }
    }
}