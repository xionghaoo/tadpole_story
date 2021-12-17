package xh.zero.tadpolestory.repo

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * key - value 存储
 */
interface PreferenceStorage {
    var refreshToken: String?
    var accessToken: String?
    var serialNumber: String?
    var nowPlayingAlbumId: String?
    var nowPlayingTrackId: String?
    var nowPlayingAlbumTitle: String?
    var selectedMultipleIndex: Int
    var selectedTimingIndex: Int

    fun clearCache()
}

class SharedPreferenceStorage @Inject constructor(@ApplicationContext context: Context) : PreferenceStorage {
    private val prefs: Lazy<SharedPreferences> = lazy {
        context.applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        ).apply {

        }
    }

    override var refreshToken by StringPreference(prefs, PREF_REFRESH_TOKEN, null)
    override var accessToken by StringPreference(prefs, PREF_ACCESS_TOKEN, null)
    override var serialNumber: String? by StringPreference(prefs, PREF_SERIAL_NUMBER, null)
    override var nowPlayingAlbumId: String? by StringPreference(prefs, PREF_NOW_PLAYING_ALBUM_ID, null)
    override var nowPlayingTrackId: String? by StringPreference(prefs, PREF_NOW_PLAYING_TRACK_ID, null)
    override var nowPlayingAlbumTitle: String? by StringPreference(prefs, PREF_NOW_PLAYING_ALBUM_TITLE, null)
    override var selectedMultipleIndex: Int by IntPreference(prefs, PREF_SELECTED_MULTIPLE_INDEX, 2)
    override var selectedTimingIndex: Int by IntPreference(prefs, PREF_SELECTED_TIMING_INDEX, 0)

    init {
//        deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // 登出时清理缓存
    override fun clearCache() {
        refreshToken = null
        accessToken = null
    }

    companion object {
        const val PREFS_NAME = "ubt_pref"
        const val PREF_REFRESH_TOKEN = "pref_refresh_token"
        const val PREF_ACCESS_TOKEN = "pref_access_token"
        const val PREF_SERIAL_NUMBER = "pref_serial_number"
        const val PREF_NOW_PLAYING_ALBUM_ID = "pref_now_playing_album_id"
        const val PREF_NOW_PLAYING_TRACK_ID = "pref_now_playing_track_id"
        const val PREF_NOW_PLAYING_ALBUM_TITLE = "pref_now_playing_album_title"
        const val PREF_SELECTED_MULTIPLE_INDEX = "pref_selected_multiple_index"
        const val PREF_SELECTED_TIMING_INDEX = "pref_selected_timing_index"
    }
}

class BooleanPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.value.getBoolean(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.value.edit { putBoolean(name, value) }
    }
}

class StringPreference(private val preferences: Lazy<SharedPreferences>,
                       private val key: String,
                       private val defaultValue: String?) : ReadWriteProperty<Any, String?> {
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.value.getString(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.value.edit { putString(key, value) }
    }
}

class IntPreference(private val preferences: Lazy<SharedPreferences>,
                    private val key: String,
                    private val defaultValue: Int = 0) : ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return preferences.value.getInt(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        preferences.value.edit { putInt(key, value) }
    }
}

class LongPreference(private val preferences: Lazy<SharedPreferences>,
                    private val key: String,
                    private val defaultValue: Long = -1) : ReadWriteProperty<Any, Long> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.value.getLong(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.value.edit { putLong(key, value) }
    }
}
