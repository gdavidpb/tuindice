package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_ENROLLMENT_AUTH
import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_RECORD_AUTH
import com.gdavidpb.tuindice.data.utils.andThen
import com.gdavidpb.tuindice.data.utils.first
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.LocalDatabaseRepository
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class LoginUseCase(
        private val dstRepository: DstRepository,
        private val localStorageRepository: LocalStorageRepository,
        private val localDatabaseRepository: LocalDatabaseRepository
) : SingleUseCase<AuthResponse, AuthRequest>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: AuthRequest): Single<AuthResponse> {
        val account = Account(usbId = params.usbId, password = params.password)

        val clearCookies = localStorageRepository.delete("cookies", false)
        val enrollmentAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_ENROLLMENT_AUTH))
        val recordAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_RECORD_AUTH))
        val activeRemove = localDatabaseRepository.removeActive()
        val accountStore = localDatabaseRepository.storeAccount(account = account, active = true)

        val auth = enrollmentAuth.concatWith(recordAuth).first { it.isSuccessful }
        val store = activeRemove.andThen(accountStore)

        return clearCookies.andThen(auth).andThen(store)
    }
}