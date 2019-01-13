package com.gdavidpb.tuindice.domain.usecase

import android.content.Intent
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val baaSRepository: BaaSRepository
) : SingleUseCase<StartUpAction, Intent>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: Intent): Single<StartUpAction> {
        val emailLink = "${params.data}"

        val activeAccount = localDatabaseRepository.getActiveAccount().isEmpty.map(Boolean::not)
        val linkReceived = baaSRepository.isSignInLink(emailLink)
        val emailSent = settingsRepository.getEmailSentTo().map(String::isNotEmpty)

        val chain = mapOf(
                activeAccount to StartUpAction.MAIN,
                linkReceived to StartUpAction.EMAIL_LINK,
                emailSent to StartUpAction.EMAIL_SENT
        )

        val selected = Single.concat(chain.keys).any { it }

        return Single.just(chain[selected] ?: StartUpAction.LOGIN)
    }
}