package com.gdavidpb.tuindice.data.source.ui

import android.content.Context
import com.gdavidpb.tuindice.base.R as BaseR
import com.gdavidpb.tuindice.login.R as LoginR
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.ui.resource.HostUiTexts

class AndroidHostUiTextProvider(
	private val context: Context
) : HostUiTextProvider {
	override fun getValues(): HostUiTexts {
		return HostUiTexts(
			googleServicesUnavailableTitle = context.getString(LoginR.string.dialog_title_no_gms_failure),
			googleServicesUnavailableMessage = context.getString(LoginR.string.dialog_message_no_gms_failure),
			googleServicesUnavailableExit = context.getString(LoginR.string.exit),
			externalResourceTitle = context.getString(BaseR.string.dialog_title_warning_external),
			externalResourceMessage = context.getString(BaseR.string.dialog_message_warning_external),
			externalResourceOpen = context.getString(BaseR.string.open),
			externalResourceCancel = context.getString(BaseR.string.cancel)
		)
	}
}
