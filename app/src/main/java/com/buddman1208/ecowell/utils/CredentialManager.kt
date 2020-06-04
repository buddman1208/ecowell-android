package com.buddman1208.ecowell.utils

/**
 * Created by Junseok Oh on 2017-07-18.
 */

import android.content.Context
import android.content.SharedPreferences
import com.buddman1208.ecowell.AppController
import com.google.gson.Gson
import java.util.*

class CredentialManager private constructor() {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor
    private val context: Context by lazy { AppController.context }

    /* Credential Data */

    init {
        preferences = context.getSharedPreferences("ecowell", Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    var language: String
        set(value) {
            editor.putString("language", value).apply()
        }
        get() {
            return preferences.getString("language", "ko") ?: "ko"
        }

    var setting1: SettingCache
        set(value) {
            editor
                .putString(
                    "setting1", Gson().toJson(value, SettingCache::class.java)
                )
                .apply()
        }
        get() {
            val setting1 = preferences.getString("setting1", "") ?: ""
            return if (setting1.isNotBlank()) {
                Gson().fromJson(setting1, SettingCache::class.java)
            } else SettingCache()
        }

    var setting2: SettingCache
        set(value) {
            editor
                .putString(
                    "setting2", Gson().toJson(value, SettingCache::class.java)
                )
                .apply()
        }
        get() {
            val setting2 = preferences.getString("setting2", "") ?: ""
            return if (setting2.isNotBlank()) {
                Gson().fromJson(setting2, SettingCache::class.java)
            } else SettingCache()
        }

    var deviceCache : DeviceCache?
        set(value) {
            editor
                .putString(
                    "deviceCache", Gson().toJson(value, DeviceCache::class.java)
                )
                .apply()
        }
        get() {
            val deviceCache = preferences.getString("deviceCache", "") ?: ""
            return if (deviceCache.isNotBlank()) {
                Gson().fromJson(deviceCache, DeviceCache::class.java)
            } else null
        }

    companion object {

        /* Data Keys */
        private var manager: CredentialManager? = null

        val instance: CredentialManager
            get() {
                if (manager == null) manager =
                    CredentialManager()
                return manager as CredentialManager
            }
    }

}

data class SettingCache(
    var ledLevel: Int = 3,
    var microCurrent: Int = 5
)

data class DeviceCache(
    val macAddress: String,
    val writeUUID: UUID,
    val notifyUUID: UUID
)