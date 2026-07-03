package com.opensplit.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.opensplit.features.auth.TokenStorage
import kotlinx.coroutines.flow.first

class DataStoreTokenStorage(private val dataStore: DataStore<Preferences>) : TokenStorage {
  override suspend fun saveAccessToken(token: String) {
    dataStore.edit { prefs -> prefs[ACCESS_TOKEN] = token }
  }

  override suspend fun getAccessToken(): String? = dataStore.data.first()[ACCESS_TOKEN]

  override suspend fun clearAccessToken() {
    dataStore.edit { prefs -> prefs.remove(ACCESS_TOKEN) }
  }

  companion object {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
  }
}
