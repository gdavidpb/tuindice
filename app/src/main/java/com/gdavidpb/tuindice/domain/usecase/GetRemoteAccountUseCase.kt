package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_ENROLLMENT_AUTH
import com.gdavidpb.tuindice.data.utils.ENDPOINT_DST_RECORD_AUTH
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class GetRemoteAccountUseCase(
        private val dstRepository: DstRepository
) : SingleUseCase<Account, AuthRequest>(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: AuthRequest): Single<Account> {
        val enrollmentAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_ENROLLMENT_AUTH))
        val recordAuth = dstRepository.auth(params.copy(serviceUrl = ENDPOINT_DST_RECORD_AUTH))

        return Single.create { }
    }
}