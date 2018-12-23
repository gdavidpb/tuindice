package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.Enrollment
import com.gdavidpb.tuindice.domain.model.Record
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody

interface DstRepository {
    fun getAccount(): Single<Account>
    fun getRecord(): Single<Record>
    fun getEnrollment(): Maybe<Enrollment>
    fun getEnrollmentProof(): Single<ResponseBody>

    fun auth(serviceUrl: String, usbId: String, password: String): Single<AuthResponse>
}