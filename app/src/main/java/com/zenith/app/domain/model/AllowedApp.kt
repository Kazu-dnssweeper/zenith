package com.zenith.app.domain.model

import android.graphics.drawable.Drawable

/**
 * フォーカスモード中に使用を許可するアプリの情報
 */
data class AllowedApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)
