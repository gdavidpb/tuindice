package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.utils.mappers.spanAbout

data class AboutBuilder(
        val context: Context,
        val content: MutableList<AboutItemBase>
)

data class AboutItemBuilder(
        val context: Context,
        var content: CharSequence? = null,
        var drawable: Drawable? = null,
        var onClick: () -> Unit = {}
)

inline fun Context.about(builder: AboutBuilder.() -> Unit) = AboutBuilder(this, mutableListOf()).apply(builder)

fun AboutBuilder.build() = content

fun AboutBuilder.header(@StringRes stringRes: Int) {
    val title = context.getString(stringRes)

    val item = AboutHeaderItem(title)

    content.add(item)
}

fun AboutBuilder.item(builder: AboutItemBuilder.() -> Unit) {
    AboutItemBuilder(context).apply(builder).let { built ->
        AboutItem(
                content = built.content ?: throw NullPointerException("content"),
                drawable = built.drawable ?: throw NullPointerException("drawable"),
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

fun AboutItemBuilder.tintedDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int) {
    drawable = context.getCompatDrawable(drawableRes, colorRes)
}

fun AboutItemBuilder.sizedDrawable(@DrawableRes drawableRes: Int, @DimenRes dimenRes: Int) {
    val size = context.resources.getDimensionPixelSize(dimenRes)

    drawable = context.getCompatVector(drawableRes, size, size)
}

fun AboutItemBuilder.onClick(callback: () -> Unit) {
    this.onClick = callback
}

