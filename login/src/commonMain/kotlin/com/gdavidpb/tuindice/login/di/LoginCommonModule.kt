package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.login.data.repository.KtorAuthApiApiDataRepository
import com.gdavidpb.tuindice.login.data.repository.KtorMessagingApiDataRepository
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository
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
import com.gdavidpb.tuindice.login.presentation.resource.DefaultLoginTextProvider
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val loginCommonModule = module {
	/* View models */

	factoryOf(::SignInViewModel)
	factoryOf(::SignOutViewModel)
	factoryOf(::UpdatePasswordViewModel)

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

	factoryOf(::KtorAuthApiApiDataRepository) { bind<AuthApiRepository>() }
	factoryOf(::KtorMessagingApiDataRepository) { bind<MessagingApiRepository>() }

	/* Shared text resources */

	factoryOf(::DefaultLoginTextProvider) { bind<LoginTextProvider>() }

	/* Exception handlers */

	factoryOf(::UpdatePasswordExceptionHandler)
	factoryOf(::SignInExceptionHandler)
}
