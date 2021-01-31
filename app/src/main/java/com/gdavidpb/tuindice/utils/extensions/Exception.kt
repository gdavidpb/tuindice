package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.*
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

fun Throwable.isObjectNotFound() = when (val internal = cause) {
    is StorageException -> {
        internal.errorCode == ERROR_OBJECT_NOT_FOUND ||
                internal.errorCode == ERROR_BUCKET_NOT_FOUND ||
                internal.errorCode == ERROR_PROJECT_NOT_FOUND
    }
    else -> false
}

fun List<Throwable>.isInvalidLink() = contains<FirebaseAuthActionCodeException>()

fun List<Throwable>.isAccountDisabled() = contains<FirebaseAuthInvalidUserException>()

fun Throwable?.isInvalidCredentials() =
        ((this as? AuthenticationException)?.code) == AuthResponseCode.INVALID_CREDENTIALS ||
                (this is FirebaseAuthInvalidCredentialsException)

fun Throwable?.isUserNoFound() =
        (this as? FirebaseAuthInvalidUserException)?.errorCode == "ERROR_USER_NOT_FOUND"

fun Throwable.isConnectionIssue() = when (this) {
    is SocketException -> true
    is InterruptedIOException -> true
    is UnknownHostException -> true
    is ConnectException -> true
    is SSLHandshakeException -> true
    is HttpException -> true
    else -> false
}

fun Throwable.causes(): List<Throwable> {
    val hashSet = hashSetOf(this)

    var throwableCause = cause

    while (throwableCause?.let(hashSet::add) == true) throwableCause = throwableCause.cause

    return hashSet.toList()
}
