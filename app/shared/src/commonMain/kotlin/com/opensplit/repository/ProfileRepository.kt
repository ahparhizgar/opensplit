package com.opensplit.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.opensplit.dto.auth.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface ProfileRepository {
  val profile: Flow<UserProfile?>

  suspend fun setProfile(profile: UserProfile?)
}

class InMemoryProfileRepository : ProfileRepository {
  private val _profile = MutableStateFlow<UserProfile?>(null)
  override val profile: Flow<UserProfile?> = _profile

  override suspend fun setProfile(profile: UserProfile?) {
    _profile.value = profile
  }
}

class DataStoreProfileRepository(private val dataStore: DataStore<Preferences>) :
    ProfileRepository {

  override val profile: Flow<UserProfile?> =
      dataStore.data.map { prefs ->
        val id = prefs[KEY_ID] ?: return@map null
        val name = prefs[KEY_NAME]
        val email = prefs[KEY_EMAIL] ?: return@map null
        UserProfile(id = id, name = name, email = email)
      }

  override suspend fun setProfile(profile: UserProfile?) {
    dataStore.edit { prefs ->
      if (profile == null) {
        prefs.remove(KEY_ID)
        prefs.remove(KEY_NAME)
        prefs.remove(KEY_EMAIL)
      } else {
        prefs[KEY_ID] = profile.id
        profile.name?.let { prefs[KEY_NAME] = it } ?: prefs.remove(KEY_NAME)
        prefs[KEY_EMAIL] = profile.email
      }
    }
  }

  companion object {
    private val KEY_ID = stringPreferencesKey("profile_id")
    private val KEY_NAME = stringPreferencesKey("profile_name")
    private val KEY_EMAIL = stringPreferencesKey("profile_email")
  }
}
