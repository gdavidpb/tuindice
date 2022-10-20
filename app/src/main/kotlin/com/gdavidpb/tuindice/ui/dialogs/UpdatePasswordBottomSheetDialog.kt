package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.Validation
import com.gdavidpb.tuindice.base.utils.`do`
import com.gdavidpb.tuindice.base.utils.`when`
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.base.utils.firstInvalid
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.*
import org.koin.android.ext.android.inject

class UpdatePasswordBottomSheetDialog(
	private val manager: DialogManager
) : BottomSheetDialogFragment() {

	interface DialogManager {
		fun onConfirmClicked(password: String)
	}

	private val inputMethodManager by inject<InputMethodManager>()

	private val validations by lazy {
		arrayOf<Validation<*>>(
			`when`(tInputPassword) { isEmpty() } `do` { setError(R.string.error_empty) }
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.dialog_update_password, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		tInputPassword.setAction {
			onConfirmClick()
		}

		isCancelable = false

		btnConfirm.onClickOnce(::onConfirmClick)
		btnCancel.onClickOnce(::onCancelClick)
	}

	fun setLoading(value: Boolean) {
		pBarUpdate.isVisible = value
		tInputPassword.isEnabled = !value
		btnConfirm.isEnabled = !value
		btnConfirm.text =
			if (value) null else getString(R.string.dialog_button_update_password_confirm)
	}

	fun setError(@StringRes resource: Int) {
		tInputPassword.setError(resource)
	}

	private fun onConfirmClick() {
		validations.firstInvalid {
			when (this) {
				is View -> {
					requestFocus()

					animateLookAtMe()
				}
			}
		}.isNull {
			hideSoftKeyboard(inputMethodManager)

			manager.onConfirmClicked(password = tInputPassword.getPassword())
		}
	}

	private fun onCancelClick() {
		dismiss()
	}
}