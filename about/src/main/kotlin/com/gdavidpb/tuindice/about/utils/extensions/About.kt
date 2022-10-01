package com.gdavidpb.tuindice.about.utils.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.about.presentation.model.AboutItem
import com.gdavidpb.tuindice.about.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.base.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.about.utils.mappers.spanAbout

data class AboutBuilder(
	val context: Context,
	val content: MutableList<AboutItemBase>
)

data class AboutItemBuilder(
	val context: Context,
	var content: CharSequence? = null,
	var drawable: Drawable? = null,
	var drawableTint: ColorStateList? = null,
	var onClick: () -> Unit = {}
)

inline fun Context.about(builder: AboutBuilder.() -> Unit) =
	AboutBuilder(this, mutableListOf()).apply(builder)

fun AboutBuilder.build() = content

fun AboutBuilder.header(@StringRes stringRes: Int) {
	val title = context.getString(stringRes)

	val item = AboutHeaderItem(title)

	content.add(item)
}

fun AboutBuilder.item(builder: AboutItemBuilder.() -> Unit) {
	AboutItemBuilder(context).apply(builder).let { built ->
		AboutItem(
			content = built.content ?: error("content"),
			drawable = built.drawable ?: error("drawable"),
			drawableTint = built.drawableTint,
			onClick = built.onClick
		)
	}.also { item -> content.add(item) }
}

fun AboutItemBuilder.title(@StringRes stringRes: Int) {
	title(value = context.getString(stringRes))
}

fun AboutItemBuilder.title(value: String) {
	val titleColor = context.getCompatColor(R.color.color_primary_text)
	val subtitleColor = context.getCompatColor(R.color.color_secondary_text)

	content = value.spanAbout(titleColor, subtitleColor)
}

fun AboutItemBuilder.drawable(
	@DrawableRes drawableRes: Int,
	@ColorRes colorRes: Int = -1,
	@DimenRes dimenRes: Int = -1
) {
	if (colorRes != -1) {
		val color = context.getCompatColor(colorRes)

		drawableTint = ColorStateList.valueOf(color)
	}

	drawable = if (dimenRes == -1) {
		context.getCompatDrawable(drawableRes)
	} else {
		val size = context.resources.getDimensionPixelSize(dimenRes)

		context.getCompatDrawable(drawableRes, size, size)
	}
}

fun AboutItemBuilder.onClick(callback: () -> Unit) {
	this.onClick = callback
}

