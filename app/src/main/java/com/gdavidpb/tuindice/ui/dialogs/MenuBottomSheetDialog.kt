package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.ui.adapters.BottomMenuAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_menu.*

open class MenuBottomSheetDialog(
        @StringRes private val titleResource: Int = 0,
        private val titleText: CharSequence = "",
        private val menuItems: List<BottomMenuItem>,
        private val onItemSelected: (Int) -> Unit,
) : BottomSheetDialogFragment() {

    private val menuAdapter by lazy {
        BottomMenuAdapter(items = menuItems, manager = MenuManager())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when {
            titleResource != 0 -> tViewMenuTitle.setText(titleResource)
            titleText.isNotEmpty() -> tViewMenuTitle.text = titleText
            else -> error("'titleResource' or 'titleText' is missed")
        }

        rViewMenu.adapter = menuAdapter
    }

    private inner class MenuManager : BottomMenuAdapter.AdapterManager {
        override fun onMenuItemClicked(position: Int) {
            onItemSelected(position)

            dismiss()
        }
    }
}