package com.example.modernui.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. DataStore Extension (Singleton instance per Context - Memory leak se bachne ke liye)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fintech_session")

// 2. Data Class for Clean State Management
data class UserSession(
    val token: String? = null,
    val name: String? = null,
    val roleId: String? = null,
    val userId: String? = null,
    val mobile: String? = null
)

// 3. The Session Manager Class
class SessionManager(private val context: Context) {

    // Keys definition (DRY Principle - Taaki typos na ho)
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val NAME_KEY = stringPreferencesKey("name")
        private val ROLE_ID_KEY = stringPreferencesKey("role_id")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val MOBILE_KEY = stringPreferencesKey("mobile")
    }

    /**
     * User login ke baad saara data ek saath save karne ke liye.
     */
    suspend fun saveSession(
        token: String,
        name: String,
        roleId: String,
        userId: String,
        mobile: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[NAME_KEY] = name
            preferences[ROLE_ID_KEY] = roleId
            preferences[USER_ID_KEY] = userId
            preferences[MOBILE_KEY] = mobile
        }
    }

    /**
     * Flow of UserSession.
     * Ise hum Retrofit Interceptor (token bhejne ke liye) aur
     * Navigation (user logged in hai ya nahi check karne ke liye) me use karenge.
     */
    val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { preferences ->
        UserSession(
            token = preferences[TOKEN_KEY],
            name = preferences[NAME_KEY],
            roleId = preferences[ROLE_ID_KEY],
            userId = preferences[USER_ID_KEY],
            mobile = preferences[MOBILE_KEY]
        )
    }

    /**
     * User logout ke waqt session clear karne ke liye.
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}