package com.gdavidpb.tuindice.data.mapper

import android.net.Uri
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest

open class ResetMapper(
        private val resetParamMapper: ResetParamMapper
) : Mapper<String, ResetRequest> {
    override fun map(value: String): ResetRequest {
        val mainUri = Uri.parse(value)

        val code = getCode(mainUri)
        val continueUrl = getContinueUrl(mainUri)

        val continueUri = Uri.parse(continueUrl)

        val resetPassword = continueUri.getQueryParameter("")!!

        val (email, password) = resetPassword.let(resetParamMapper::mapTo)

        return ResetRequest(code, email, password)
    }

    private fun getCode(uri: Uri): String {
        return uri.getQueryParameter("oobCode")!!
    }

    private fun getContinueUrl(uri: Uri): String {
        return uri.getQueryParameter("continueUrl")!!
    }
}