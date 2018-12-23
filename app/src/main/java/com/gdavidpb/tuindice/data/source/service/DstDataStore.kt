package com.gdavidpb.tuindice.data.source.service

import com.gdavidpb.tuindice.data.mapper.*
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.Enrollment
import com.gdavidpb.tuindice.domain.model.Record
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.google.common.net.MediaType
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.FileNotFoundException

open class DstDataStore(
        private val authService: DstAuthService,
        private val recordService: DstRecordService,
        private val enrollmentService: DstEnrollmentService,
        private val authResponseMapper: AuthResponseMapper,
        private val accountMapper: AccountMapper,
        private val recordMapper: RecordMapper,
        private val enrollmentMapper: EnrollmentMapper,
        private val mediaTypeMapper: MediaTypeMapper
) : DstRepository {
    override fun getAccount(): Single<Account> {
        return recordService.getPersonalData().map(accountMapper::map)
    }

    override fun getRecord(): Single<Record> {
        return recordService.getRecordData().map(recordMapper::map)
    }

    override fun getEnrollment(): Maybe<Enrollment> {
        return enrollmentService.getEnrollment().map(enrollmentMapper::map)
    }

    override fun getEnrollmentProof(): Single<ResponseBody> {
        return enrollmentService.getEnrollmentProof().map {
            val mediaType = it.contentType()?.let(mediaTypeMapper::map)

            if (mediaType?.`is`(MediaType.PDF) == true)
                it
            else
                throw FileNotFoundException()
        }
    }

    override fun auth(serviceUrl: String, usbId: String, password: String): Single<AuthResponse> {
        return authService.auth(serviceUrl, usbId, password).map(authResponseMapper::map)
    }
}