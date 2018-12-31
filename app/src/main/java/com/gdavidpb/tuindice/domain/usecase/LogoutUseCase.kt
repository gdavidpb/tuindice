package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class LogoutUseCase(
        private val localStorageRepository: LocalStorageRepository,
        private val localDatabaseRepository: LocalDatabaseRepository
) : CompletableUseCase<Void?>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: Void?): Completable {
        val removeActive = localDatabaseRepository.removeActive()
        val deleteCookies = localStorageRepository.delete("cookies", false)

        return removeActive.andThen(deleteCookies)
    }

}