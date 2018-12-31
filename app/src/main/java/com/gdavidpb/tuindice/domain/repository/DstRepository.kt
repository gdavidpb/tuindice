package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.Enrollment
import com.gdavidpb.tuindice.domain.model.Record
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody

interface DstRepository {
    fun getAccount(): Maybe<Account>
    fun getRecord(): Maybe<Record>
    fun getEnrollment(): Maybe<Enrollment>
    fun getEnrollmentProof(): Maybe<ResponseBody>

    fun auth(request: AuthRequest): Single<AuthResponse>
}