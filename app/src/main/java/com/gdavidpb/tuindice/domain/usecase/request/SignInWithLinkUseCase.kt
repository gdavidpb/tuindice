package com.gdavidpb.tuindice.domain.usecase.request

import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class SignInWithLinkUseCase(
        private val baaSRepository: BaaSRepository,
        private val settingsRepository: SettingsRepository
) : CompletableUseCase<String>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: String): Completable {
        return settingsRepository.getEmailSentTo().flatMapCompletable { email ->
            baaSRepository.signInWithLink(email = email, link = params)
        }
    }
}