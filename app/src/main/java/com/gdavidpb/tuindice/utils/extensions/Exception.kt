package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthErrorCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

fun List<Throwable>.isInvalidLink() = any<FirebaseAuthActionCodeException>()

fun List<Throwable>.haveCredentialsChanged() = any<FirebaseAuthInvalidCredentialsException>()

fun List<Throwable>.isAccountDisabled() = find<FirebaseAuthInvalidUserException>()?.errorCode == "ERROR_USER_DISABLED"

fun List<Throwable>.isUserNotFound() = find<FirebaseAuthInvalidUserException>()?.errorCode == "ERROR_USER_NOT_FOUND"

fun Throwable.isObjectNotFound() = this is StorageException && when (errorCode) {
    StorageException.ERROR_OBJECT_NOT_FOUND -> true
    StorageException.ERROR_BUCKET_NOT_FOUND -> true
    StorageException.ERROR_PROJECT_NOT_FOUND -> true
    else -> false
}

fun Throwable.isInvalidCredentials() = when (this) {
    is AuthenticationException -> (errorCode == AuthErrorCode.INVALID_CREDENTIALS)
    else -> false
}

fun Throwable.isNotEnrolled() = when (this) {
    is AuthenticationException -> (errorCode == AuthErrorCode.NOT_ENROLLED)
    else -> false
}

fun Throwable.isTimeout() = when (this) {
    is TimeoutException -> true
    is TimeoutCancellationException -> true
    else -> false
}

fun Throwable.isConnection() = when (this) {
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

tailrec fun Throwable.causes(causes: HashSet<Throwable> = hashSetOf(this)): List<Throwable> {
    val throwableCause = cause

    return if (throwableCause != null && causes.add(throwableCause))
        throwableCause.causes(causes)
    else
        causes.toList()
}