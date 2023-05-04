package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.migration.MigrationManager
import com.gdavidpb.tuindice.di.appMockModule
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import com.gdavidpb.tuindice.enrollmentproof.di.enrollmentProofModule
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.login.di.loginModule
import com.gdavidpb.tuindice.persistence.di.persistenceModule
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.summary.di.summaryModule
import com.gdavidpb.tuindice.transactions.di.transactionsModule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*

@KoinReflectAPI
class TuIndiceMockApp : Application() {
	override fun onCreate() {
		super.onCreate()

		Locale.setDefault(DEFAULT_LOCALE)
		TimeZone.setDefault(DEFAULT_TIME_ZONE)

		FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

		MigrationManager.execute(applicationContext)

		startKoin {
			androidLogger(Level.NONE)

			androidContext(this@TuIndiceMockApp)

			androidFileProperties()

			modules(
				appMockModule,
				persistenceModule,
				loginModule,
				summaryModule,
				recordModule,
				enrollmentProofModule,
				evaluationsModule,
				transactionsModule
			)
		}
	}
}