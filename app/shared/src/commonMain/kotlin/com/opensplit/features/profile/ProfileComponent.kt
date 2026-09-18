package com.opensplit.features.profile

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.opensplit.component.CContext
import com.opensplit.component.componentScope
import com.opensplit.db.AppDatabase
import com.opensplit.features.auth.AuthComponent
import com.opensplit.features.auth.TokenStorage
import com.opensplit.repository.ProfileRepository
import com.opensplit.root.TopLevelDestinationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface ProfileComponent {
  val uiState: Value<ProfileUiState>

  fun onBack(): Unit

  fun onEditClicked(): Unit

  fun onLogoutClicked(): Job

  @Serializable data object Config : TopLevelDestinationConfig
}

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
)

interface ProfileComponentFactory {
  fun create(context: CContext): ProfileComponent
}

class DefaultProfileComponent(
    context: CContext,
    private val profileRepository: ProfileRepository,
    private val tokenStorage: TokenStorage,
    private val database: AppDatabase,
) : ProfileComponent, CContext by context {
  private val _uiState = MutableValue(ProfileUiState())
  override val uiState: Value<ProfileUiState> = _uiState
  private val scope = componentScope()

  init {
    doOnCreate {
      scope.launch {
        profileRepository.profile.collect { userProfile ->
          _uiState.update {
            it.copy(
                name = userProfile?.name.orEmpty(),
                email = userProfile?.email.orEmpty(),
            )
          }
        }
      }
    }
  }

  override fun onBack() {
    navigation.pop()
  }

  override fun onEditClicked() {}

  override fun onLogoutClicked(): Job = scope.launch {
    tokenStorage.clearAccessToken()
    profileRepository.setProfile(null)
    database.useWriterConnection { connection ->
      connection.immediateTransaction { database.clearAllTables() }
    }
    navigation.replaceAll(AuthComponent.Config)
  }
}

class FakeProfileComponent(
    uiState: ProfileUiState =
        ProfileUiState(
            name = "AmirHossein",
            email = "amparhizgar@gmail.com",
        )
) : ProfileComponent {
  override val uiState: Value<ProfileUiState> = MutableValue(uiState)

  override fun onBack() {}

  override fun onEditClicked() {}

  override fun onLogoutClicked(): Job = Job()
}

class DefaultProfileComponentFactory(
    private val profileRepository: ProfileRepository,
    private val tokenStorage: TokenStorage,
    private val database: AppDatabase,
) : ProfileComponentFactory {
  override fun create(context: CContext): ProfileComponent =
      DefaultProfileComponent(context, profileRepository, tokenStorage, database)
}
