package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.base.MaybeUseCase
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class GetAccountUseCase(
        private val dstRepository: DstRepository,
        private val localDatabaseRepository: LocalDatabaseRepository,
        private val settingsRepository: SettingsRepository
) : MaybeUseCase<Account, Boolean>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: Boolean): Maybe<Account> {
        val local = localDatabaseRepository.getActiveAccount()
        val remote = dstRepository.getAccount()
        val setCooldown = settingsRepository.setCooldown(key = "GetAccountUseCase")
        val isCooldown = settingsRepository.isCooldown(key = "GetAccountUseCase")

        /* params -> tryRefresh */
        return isCooldown.flatMapMaybe { cooldown ->
            if (cooldown || !params)
                local
            else
                setCooldown.andThen(remote)
        }
    }
}