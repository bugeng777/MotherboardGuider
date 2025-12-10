package com.xzd.motherboardguider.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleHelper {
    /**
     * 设置应用语言
     * @param context 上下文
     * @param languageCode 语言代码，如 "zh"、"en"
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "zh" -> Locale("zh", "CN")
            "en" -> Locale("en", "US")
            "de" -> Locale("de", "DE")
            else -> Locale("zh", "CN") // 默认中文
        }
        
        return updateResources(context, locale)
    }

    /**
     * 更新资源配置
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        return context
    }

    /**
     * 获取当前语言代码
     */
    fun getCurrentLanguage(context: Context): String {
        val savedLanguage = PrefsManager.getLanguage(context)
        return savedLanguage
    }
}


