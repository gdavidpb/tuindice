package com.gdavidpb.tuindice.platform.android

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.ui.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.term_report_channel_name

class TuIndiceMessagingService : FirebaseMessagingService(), KoinComponent {
	private val pushTokenRotationHandler: PushTokenRotationHandler by inject()

	override fun onNewToken(token: String) {
		pushTokenRotationHandler.onPushTokenRotated()
	}

	// A term report that arrives while the app is open is shown as a notification here: with the app in the background the
	// system shows the push itself. A tap on it opens the report.
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)

		val insightId = termReportInsightIdOf(data = message.data)
		val notification = message.notification

		if (insightId == null || notification == null) return

		val isShown = canShowTermReportNotification(
			isPermissionGranted = isNotificationPermissionGranted(),
			areNotificationsEnabled = areNotificationsEnabled()
		)

		if (isShown) {
			showTermReportNotification(
				insightId = insightId,
				title = notification.title.orEmpty(),
				body = notification.body.orEmpty()
			)
		}
	}

	private fun isNotificationPermissionGranted(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

		return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
				PackageManager.PERMISSION_GRANTED
	}

	private fun areNotificationsEnabled(): Boolean {
		return NotificationManagerCompat.from(this).areNotificationsEnabled()
	}

	private fun showTermReportNotification(insightId: String, title: String, body: String) {
		val manager = NotificationManagerCompat.from(this)

		// The push is handled on the thread the system gives the service, and the notification is shown before it returns.
		val channelName = runBlocking { getString(Res.string.term_report_channel_name) }

		manager.createNotificationChannel(
			NotificationChannelCompat.Builder(TERM_REPORT_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
				.setName(channelName)
				.build()
		)

		val tap = Intent(this, MainActivity::class.java).apply {
			termReportExtrasOf(insightId).forEach { (key, value) -> putExtra(key, value) }
			addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		}
		val contentIntent = PendingIntent.getActivity(
			this,
			insightId.hashCode(),
			tap,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)

		val notification = NotificationCompat.Builder(this, TERM_REPORT_CHANNEL_ID)
			.setSmallIcon(applicationInfo.icon)
			.setContentTitle(title)
			.setContentText(body)
			.setAutoCancel(true)
			.setContentIntent(contentIntent)
			.build()

		manager.notify(insightId.hashCode(), notification)
	}

	private companion object {
		const val TERM_REPORT_CHANNEL_ID = "term_report"
	}
}
