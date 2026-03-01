package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType

data class IosPlatformAttestation(
	val token: String,
	val keyId: String? = null,
	val provider: AttestationProvider = AttestationProvider.APP_ATTEST
)

interface IosPlatformBridge {
	suspend fun fetchRemoteConfig() {}
	fun remoteConfigString(key: String): String?
	fun remoteConfigStringList(key: String): List<String>?
	suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation?
	suspend fun pushToken(): String?
	suspend fun launchReview()
	suspend fun checkForUpdate(stalenessDays: Int): UpdateAction?
	suspend fun launchUpdate(action: UpdateAction)
	fun openUrl(url: String)
	fun openStorePage()
	fun openFile(fileRef: PlatformFileRef): Boolean
	fun canOpen(fileRef: PlatformFileRef): Boolean
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
	fun isNetworkAvailable(): Boolean
	fun setUserIdentifier(identifier: String)
	fun logMessage(message: String)
	fun logException(throwable: Throwable)
	fun setCustomKey(key: String, value: String)
	fun restartDependencies()
	fun secureStoreContains(key: String): Boolean
	fun secureStoreGetString(key: String): String?
	fun secureStorePutString(key: String, value: String)
	fun secureStoreClear()
}

object DefaultIosPlatformBridge : IosPlatformBridge {
	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? {
		failMissingBridge(api = "requestAttestation")
	}

	override suspend fun pushToken(): String? {
		failMissingBridge(api = "pushToken")
	}

	override suspend fun launchReview() = Unit

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = null

	override suspend fun launchUpdate(action: UpdateAction) = Unit

	override fun openUrl(url: String) {
		val platformUrl = NSURL.URLWithString(url) ?: return
		UIApplication.sharedApplication.openURL(platformUrl)
	}

	override fun openStorePage() {
		openUrl(STORE_URL)
	}

	override fun openFile(fileRef: PlatformFileRef): Boolean {
		val platformUrl = fileRef.toPlatformUrl() ?: return false
		if (!UIApplication.sharedApplication.canOpenURL(platformUrl)) return false
		UIApplication.sharedApplication.openURL(platformUrl)
		return true
	}

	override fun canOpen(fileRef: PlatformFileRef): Boolean {
		val platformUrl = fileRef.toPlatformUrl() ?: return false
		return UIApplication.sharedApplication.canOpenURL(platformUrl)
	}

	override fun appVersionName(): String {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
			?.toString()
			?: "0.0.0"
	}

	override fun appVersionCode(): Long {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion")
			?.toString()
			?.toLongOrNull()
			?: 0L
	}

	override fun hasCamera(): Boolean {
		return UIImagePickerController.isSourceTypeAvailable(
			UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
		)
	}

	override fun isNetworkAvailable(): Boolean = true

	override fun setUserIdentifier(identifier: String) = Unit

	override fun logMessage(message: String) {
		iosLog(message)
	}

	override fun logException(throwable: Throwable) {
		iosLog(throwable.message ?: "Unknown iOS bridge exception")
	}

	override fun setCustomKey(key: String, value: String) = Unit

	override fun restartDependencies() {
		failMissingBridge(api = "restartDependencies")
	}

	override fun secureStoreContains(key: String): Boolean {
		failMissingBridge(api = "secureStoreContains")
	}

	override fun secureStoreGetString(key: String): String? {
		failMissingBridge(api = "secureStoreGetString")
	}

	override fun secureStorePutString(key: String, value: String) {
		failMissingBridge(api = "secureStorePutString")
	}

	override fun secureStoreClear() {
		failMissingBridge(api = "secureStoreClear")
	}

	private fun PlatformFileRef.toPlatformUrl(): NSURL? {
		val rawValue = value

		return when {
			rawValue.startsWith("file://") -> NSURL.URLWithString(rawValue)
			rawValue.contains("://") -> NSURL.URLWithString(rawValue)
			else -> NSURL.fileURLWithPath(rawValue)
		}
	}

	private fun failMissingBridge(api: String): Nothing {
		error(
			"DefaultIosPlatformBridge cannot execute '$api'. " +
				"Inject a real iOS bridge from host app bootstrap."
		)
	}

	private const val STORE_URL = "itms-apps://apps.apple.com"
}
