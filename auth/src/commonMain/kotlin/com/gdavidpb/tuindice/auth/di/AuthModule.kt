package com.gdavidpb.tuindice.auth.di

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.data.source.AuthDataSource
import com.gdavidpb.tuindice.auth.data.source.KtorAuthApiDataSource
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.auth.presentation.action.ConfirmSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.FlushAndSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ForceSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.InitializeSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUsageDataCollectionEnabledActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.TogglePasswordVisibilityActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ToggleUpdatePasswordVisibilityActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
	/* View models */

	viewModelOf(::SignInViewModel)
	viewModelOf(::SignOutViewModel)
	viewModelOf(::UpdatePasswordViewModel)

	/* State machines */

	factoryOf(::SignInMachine)

	/* Action Processors */

	factoryOf(::SignInActionProcessor)
	factoryOf(::SetUsbIdActionProcessor)
	factoryOf(::SetPasswordActionProcessor)
	factoryOf(::TogglePasswordVisibilityActionProcessor)
	factoryOf(::SetUsageDataCollectionEnabledActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::InitializeSignOutActionProcessor)
	factoryOf(::ConfirmSignOutActionProcessor)
	factoryOf(::FlushAndSignOutActionProcessor)
	factoryOf(::ForceSignOutActionProcessor)
	factoryOf(::OpenUpdatePasswordActionProcessor)
	factoryOf(::SetUpdatePasswordActionProcessor)
	factoryOf(::ToggleUpdatePasswordVisibilityActionProcessor)
	factoryOf(::UpdatePasswordActionProcessor)

	/* Use cases */

	factoryOf(::ConfirmSignOutUseCase)
	factoryOf(::FlushPendingChangesUseCase)
	factoryOf(::SignInUseCase)
	factoryOf(::SignOutUseCase)
	factoryOf(::UpdatePasswordUseCase)

	/* Validators */

	factoryOf(::SignInParamsValidator)
	factoryOf(::UpdatePasswordParamsValidator)

	/* Repositories */

	factoryOf(::AuthDataSource) { bind<AuthRepository>() }

	/* Data sources */

	factoryOf(::KtorAuthApiDataSource) { bind<AuthApiDataRepository>() }
	/* Exception handlers */

	factoryOf(::UpdatePasswordExceptionHandler)
	factoryOf(::SignInExceptionHandler)
}
