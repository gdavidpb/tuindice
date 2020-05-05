package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.*
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

fun Throwable.isPermissionDenied() = when (val internal = cause) {
    is FirebaseFirestoreException -> {
        internal.code == FirebaseFirestoreException.Code.UNAUTHENTICATED ||
                internal.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    is StorageException -> {
        internal.errorCode == ERROR_NOT_AUTHENTICATED ||
                internal.errorCode == ERROR_NOT_AUTHORIZED
    }
    else -> false
}

fun Throwable.isObjectNotFound() = when (val internal = cause) {
    is StorageException -> {
        internal.errorCode == ERROR_OBJECT_NOT_FOUND ||
                internal.errorCode == ERROR_BUCKET_NOT_FOUND ||
                internal.errorCode == ERROR_PROJECT_NOT_FOUND
    }
    else -> false
}

fun Throwable.isInvalidCredentials() =
        ((this as? AuthenticationException)?.code) == AuthResponseCode.INVALID_CREDENTIALS

fun Throwable.isNoNetworkAvailableIssue(isNetworkAvailable: Boolean) =
        isConnectionIssue() && !isNetworkAvailable

fun Throwable.isConnectionIssue() = when (this) {
    is SocketException -> true
    is InterruptedIOException -> true
    is UnknownHostException -> true
    is ConnectException -> true
    is SSLHandshakeException -> true
    is HttpException -> true
    else -> false
}

fun Throwable.isSynchronizationIssue() = this is SynchronizationException