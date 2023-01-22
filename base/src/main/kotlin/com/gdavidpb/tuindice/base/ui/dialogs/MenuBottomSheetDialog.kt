package com.gdavidpb.tuindice.base.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.adapters.BottomMenuAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_menu.*

open class MenuBottomSheetDialog : BottomSheetDialogFragment() {
	@StringRes
	var titleResource: Int = 0
	var titleText: CharSequence = ""

	private var menuItems = listOf<BottomMenuItem>()
	private var onItemSelected: (Int) -> Unit = {}

	fun setItems(items: List<BottomMenuItem>, onSelected: (Int) -> Unit) {
		menuItems = items
		onItemSelected = onSelected
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.dialog_bottom_menu, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		when {
			titleResource != 0 -> tViewMenuTitle.setText(titleResource)
			titleText.isNotEmpty() -> tViewMenuTitle.text = titleText
			else -> error("'titleResource' or 'titleText' is missed")
		}

		require(menuItems.isNotEmpty()) { "menuItems is empty" }

		rViewMenu.adapter = BottomMenuAdapter(items = menuItems, manager = MenuManager())
	}

	private inner class MenuManager : BottomMenuAdapter.AdapterManager {
		override fun onMenuItemClicked(itemId: Int) {
			onItemSelected(itemId)

			dismiss()
		}
	}
}