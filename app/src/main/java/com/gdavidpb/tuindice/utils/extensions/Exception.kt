package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.NoEnrolledException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.storage.StorageException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import javax.net.ssl.SSLHandshakeException

fun List<Throwable>.isInvalidLink() = contains<FirebaseAuthActionCodeException>()

fun List<Throwable>.isAccountDisabled() = contains<FirebaseAuthInvalidUserException>()

fun Throwable.isObjectNotFound() = this is StorageException &&
        (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND ||
                errorCode == StorageException.ERROR_BUCKET_NOT_FOUND ||
                errorCode == StorageException.ERROR_PROJECT_NOT_FOUND)

fun Throwable?.isInvalidCredentials() =
        this is FirebaseAuthInvalidCredentialsException ||
                ((this as? AuthenticationException)?.code) == AuthResponseCode.INVALID_CREDENTIALS

fun Throwable?.isUserNotFound() =
        (this as? FirebaseAuthInvalidUserException)?.errorCode == "ERROR_USER_NOT_FOUND"

fun Throwable.isNotEnrolled() =
        this is NoEnrolledException ||
                (this as? AuthenticationException)?.code == AuthResponseCode.NOT_ENROLLED

fun Throwable.isConnectionIssue() = when (this) {
    is SocketException -> true
    is InterruptedIOException -> true
    is UnknownHostException -> true
    is ConnectException -> true
    is SSLHandshakeException -> true
    is HttpException -> true
    is ExecutionException -> true
    else -> false
}

fun Throwable.causes(): List<Throwable> {
    val hashSet = hashSetOf(this)

    var throwableCause = cause

    while (throwableCause?.let(hashSet::add) == true) throwableCause = throwableCause.cause

    return hashSet.toList()
}
