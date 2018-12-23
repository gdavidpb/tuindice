package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class AuthUseCase(
        private val serviceRepository: DstRepository
) : SingleUseCase<AuthResponse, AuthRequest>(
        Schedulers.io(),
        AndroidSchedulers.mainThread()
) {
    override fun buildUseCaseObservable(params: AuthRequest): Single<AuthResponse> {
        return serviceRepository
                .auth(serviceUrl = params.serviceUrl,
                        usbId = params.usbId,
                        password = params.password)
    }
}