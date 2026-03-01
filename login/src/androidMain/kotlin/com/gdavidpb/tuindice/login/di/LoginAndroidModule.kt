package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.login.data.repository.LoginMessagingDataSource
import com.gdavidpb.tuindice.login.data.source.FirebaseLoginMessagingDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val loginAndroidModule = module {
	/* Android data sources */

	factoryOf(::FirebaseLoginMessagingDataSource) { bind<LoginMessagingDataSource>() }
}
