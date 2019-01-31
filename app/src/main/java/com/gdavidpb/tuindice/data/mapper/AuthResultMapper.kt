package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.Account
import com.google.firebase.auth.AuthResult

open class AuthResultMapper : Mapper<AuthResult, Account> {
    override fun map(value: AuthResult): Account {
        return value.user.run {
            Account(
                    uid = uid,
                    email = email ?: "",
                    fullName = displayName ?: "",
                    verified = isEmailVerified
            )
        }
    }
}