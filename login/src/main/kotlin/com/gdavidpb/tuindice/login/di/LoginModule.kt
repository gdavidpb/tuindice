package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.login.data.repository.AuthApiDataSource
import com.gdavidpb.tuindice.login.data.repository.LoginDataRepository
import com.gdavidpb.tuindice.login.data.repository.MessagingApiDataSource
import com.gdavidpb.tuindice.login.data.repository.MessagingDataSource
import com.gdavidpb.tuindice.login.data.repository.ReportingDataSource
import com.gdavidpb.tuindice.login.data.source.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.login.data.source.FirebaseMessagingDataSource
import com.gdavidpb.tuindice.login.data.source.KtorAuthApiApiDataSource
import com.gdavidpb.tuindice.login.data.source.KtorMessagingApiApiDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
	/* View Models */

	viewModelOf(::SignInViewModel)
	viewModelOf(::SignOutViewModel)
	viewModelOf(::UpdatePasswordViewModel)

	/* Action Processors */

	factoryOf(::SignInActionProcessor)
	factoryOf(::SetUsbIdActionProcessor)
	factoryOf(::SetPasswordActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::SignOutActionProcessor)
	factoryOf(::SetUpdatePasswordActionProcessor)
	factoryOf(::UpdatePasswordActionProcessor)

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::SignOutUseCase)
	factoryOf(::UpdatePasswordUseCase)

	/* Validators */

	factoryOf(::SignInParamsValidator)
	factoryOf(::UpdatePasswordParamsValidator)

	/* Repositories */

	factoryOf(::LoginDataRepository) { bind<LoginRepository>() }

	/* Data sources */

	factoryOf(::KtorAuthApiApiDataSource) { bind<AuthApiDataSource>() }
	factoryOf(::KtorMessagingApiApiDataSource) { bind<MessagingApiDataSource>() }
	factoryOf(::CrashlyticsReportingDataSource) { bind<ReportingDataSource>() }
	factoryOf(::FirebaseMessagingDataSource) { bind<MessagingDataSource>() }

	/* Exception handlers */

	factoryOf(::UpdatePasswordExceptionHandler)
	factoryOf(::SignInExceptionHandler)
}