package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.NoEnrolledException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
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

fun Throwable.isObjectNotFound() = this is StorageException && when (errorCode) {
    StorageException.ERROR_OBJECT_NOT_FOUND -> true
    StorageException.ERROR_BUCKET_NOT_FOUND -> true
    StorageException.ERROR_PROJECT_NOT_FOUND -> true
    else -> false
}

fun Throwable?.isInvalidCredentials() = when (this) {
    is FirebaseAuthInvalidCredentialsException -> true
    is AuthenticationException -> (code == AuthResponseCode.INVALID_CREDENTIALS)
    else -> false
}

fun Throwable?.isUserNotFound() = when (this) {
    is FirebaseAuthInvalidUserException -> errorCode == "ERROR_USER_NOT_FOUND"
    else -> false
}

fun Throwable.isNotEnrolled() = when (this) {
    is NoEnrolledException -> true
    is AuthenticationException -> (code == AuthResponseCode.NOT_ENROLLED)
    else -> false
}

fun Throwable.isConnectionIssue() = when (this) {
    is SocketException -> true
    is InterruptedIOException -> true
    is UnknownHostException -> true
    is ConnectException -> true
    is SSLHandshakeException -> true
    is HttpException -> true
    is ExecutionException -> true
    is FirebaseFirestoreException -> (code == FirebaseFirestoreException.Code.UNAVAILABLE)
    else -> false
}

fun Throwable.causes(): List<Throwable> {
    val hashSet = hashSetOf(this)

    var throwableCause = cause

    while (throwableCause?.let(hashSet::add) == true) throwableCause = throwableCause.cause

    return hashSet.toList()
}
