package $PACKAGE.di

import $PACKAGE.data.repository.$DATA_REPOSITORY_NAME
import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import $PACKAGE.domain.usecase.$LOAD_USE_CASE_NAME
import $PACKAGE.domain.usecase.exceptionhandler.$EXCEPTION_HANDLER_NAME
import $PACKAGE.presentation.action.$ACTION_PROCESSOR_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val $MODULE_VAR_NAME = module {
	/* View models */

	viewModelOf(::$VIEWMODEL_NAME)

	/* Action processor */

	factoryOf(::$ACTION_PROCESSOR_NAME)

	/* Use cases */

	factoryOf(::$LOAD_USE_CASE_NAME)

	/* Repositories */

	factoryOf(::$DATA_REPOSITORY_NAME) { bind<$REPOSITORY_INTERFACE_NAME>() }

	/* Exception handlers */

	factoryOf(::$EXCEPTION_HANDLER_NAME)
}
