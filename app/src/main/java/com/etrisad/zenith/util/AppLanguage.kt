package com.etrisad.zenith.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Central place for the app language.
 *
 * The selection lives in plain [android.content.SharedPreferences] (not DataStore)
 * because it has to be readable from [android.app.Application.attachBaseContext],
 * long before the asynchronous preference stores are ready.
 *
 * [SYSTEM] keeps whatever locale Android or the system per-app language picker
 * supplies; [ENGLISH] and [SIMPLIFIED_CHINESE] override it for this process only.
 */
object AppLanguage {
    const val SYSTEM = "system"
    const val ENGLISH = "en"
    const val SIMPLIFIED_CHINESE = "zh-CN"

    /** Every value that can be stored in the preferences file, in display order. */
    val options = listOf(SYSTEM, ENGLISH, SIMPLIFIED_CHINESE)

    private const val PREFS_NAME = "zenith_language"
    private const val KEY_LANGUAGE = "app_language"

    /**
     * The locale the process started with. Captured before any override so that
     * switching back to [SYSTEM] at runtime can restore it.
     */
    private val systemLocale: Locale = Locale.getDefault()

    fun selection(context: Context): String = runCatching {
        // During Application.attachBaseContext there is no application yet, so
        // fall back to the base context instead of context.applicationContext.
        (context.applicationContext ?: context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, SYSTEM)
    }.getOrNull()?.takeIf { it in options } ?: SYSTEM

    /** Stores [selection] and immediately applies it to the process resources. */
    fun setSelection(context: Context, selection: String) {
        val sanitized = selection.takeIf { it in options } ?: SYSTEM
        runCatching {
            (context.applicationContext ?: context)
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, sanitized)
                .apply()
        }
        applyTo(context.applicationContext ?: context)
    }

    fun isChinese(context: Context): Boolean = selection(context) == SIMPLIFIED_CHINESE ||
        (selection(context) == SYSTEM && systemLocale.language == "zh")

    fun localeFor(selection: String): Locale? = when (selection) {
        ENGLISH -> Locale.ENGLISH
        SIMPLIFIED_CHINESE -> Locale.SIMPLIFIED_CHINESE
        else -> null
    }

    /**
     * Returns a context whose resources resolve against the selected language.
     * Intended for [android.app.Application.attachBaseContext] and every activity.
     */
    fun wrap(base: Context): Context {
        val locale = localeFor(selection(base)) ?: return base
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }

    /**
     * Applies the selection to the long-lived application resources so that
     * services, notifications and widgets created later pick it up without a
     * process restart.
     */
    @Suppress("DEPRECATION")
    fun applyTo(context: Context) {
        val locale = localeFor(selection(context)) ?: systemLocale
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
}
