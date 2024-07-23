package io.github.annaharri89.stitchcounter.utilities

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import java.util.Locale


fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

fun Context.getActivityOrNull(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

@Suppress("DEPRECATION")
fun FragmentActivity.setStatusBarColor(color: Int) {
    val window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, color)
}

@Suppress("DEPRECATION")
fun FragmentActivity.setNavBarColor(color: Int) {
    ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightNavigationBars = true
    window.navigationBarColor = ContextCompat.getColor(this, color)
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}