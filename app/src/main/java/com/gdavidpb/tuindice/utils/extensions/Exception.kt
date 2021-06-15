package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.exception.IllegalAuthProviderException
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

fun Throwable.isUnavailable() = when (this) {
    is HttpException -> (code() == HttpURLConnection.HTTP_UNAVAILABLE)
    else -> false
}

fun Throwable.isForbidden() = when (this) {
    is HttpException -> (code() == HttpURLConnection.HTTP_FORBIDDEN)
    else -> false
}

fun Throwable.isConflict() = when (this) {
    is HttpException -> (code() == HttpURLConnection.HTTP_CONFLICT)
    else -> false
}

fun Throwable.isUnauthorized() = when (this) {
    is HttpException -> (code() == HttpURLConnection.HTTP_UNAUTHORIZED)
    else -> false
}

fun Throwable.isNotFound() = when (this) {
    is HttpException -> (code() == HttpURLConnection.HTTP_NOT_FOUND)
    else -> false
}

fun Throwable.isIllegalAuthProvider() = when (this) {
    is IllegalAuthProviderException -> true
    else -> false
}

fun Throwable.isTimeout() = when (this) {
    is TimeoutException -> true
    is TimeoutCancellationException -> true
    else -> false
}

fun Throwable.isObjectNotFound() = this is StorageException && when (errorCode) {
    StorageException.ERROR_OBJECT_NOT_FOUND -> true
    StorageException.ERROR_BUCKET_NOT_FOUND -> true
    StorageException.ERROR_PROJECT_NOT_FOUND -> true
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