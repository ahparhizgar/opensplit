---
name: decompose
description: Decompose readme page
---

## Why Decompose?
Decompose breaks the code down into small and independent components and organizes them into trees. Each parent component is only aware of its immediate children.
Decompose draws clear boundaries between UI and non-UI code, which gives the following benefits:
Better separation of concerns
Pluggable platform-specific UI (Compose, SwiftUI, Kotlin/React, etc.)
Business logic code is testable with pure multiplatform unit tests
Navigation state is fully exposed - plug any UI you want, animate as you like using your favourite UI framework's API or use predefined API.
Navigation is a pure function from the old state to a new one - navigate without limits.
Proper dependency injection (DI) and inversion of control (IoC) via constructor, including but not limited to type-safe arguments.
Shared navigation logic
Lifecycle-aware components
Components in the back stack are not destroyed, they continue working in background without UI
State preservation (automatically on Android, manually on all other targets via kotlinx-serialization)
Instances retaining (aka ViewModels) over configuration changes (mostly useful in Android)

## Decompose components

for each screen create a Decompose component like this for feature "Fx":
file path: `src/commonMain/kotlin/screens/FxComponent.kt`
Imports:

- import com.arkivanov.essenty.lifecycle.doOnResume
- import component.CContext
- import component.apiCallScope
- import io.ktor.client.HttpClient
- import io.ktor.client.call.body
- import io.ktor.client.request.get
- import korlibs.io.async.launch
- import kotlinx.coroutines.flow.MutableStateFlow
- import kotlinx.coroutines.flow.StateFlow
- import kotlinx.coroutines.flow.update

```kotlin
    interface FxComponent {
    val uiState: StateFlow<FxUiState>
    fun clickOnXxButton() {}
    interface Factory {
        fun create(context: CContext): FxComponent
    }
}

data class FxUiState(
    val isLoading: Boolean = true,
)

class DefaultFxComponent(
    context: CContext,
    val httpClient: HttpClient,
) : FxComponent, CContext by context {
    private val _uiState = MutableStateFlow(FxUiState())
    override val uiState: StateFlow<FxUiState> = _uiState
    private val scope = apiCallScope()

    init {
        lifecycle.doOnResume {
            scope.launch {
                getSamples()
            }
        }
    }

    private suspend fun getSamples() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            val samples = httpClient.get("/samples").body<List<SampleDto>>()
            _uiState.update {
                it.copy(samples = samples)
            }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun clickOnXxButton() {
        _uiState.update {

            it.copy(/* handle state change */)
        }
    }

    class Factory(val httpClient: HttpClient) : FxComponent.Factory {
        override fun create(context: CContext): FxComponent {
            return DefaultFxComponent(
                httpClient = httpClient,
                context = context,
            )
        }
    }
}

class FakeFxComponent(
    uiState: FxUiState = FxUiState(
        plans = FakeFxDtoFactory.createList()
    )
) : FxComponent {
    override val uiState: StateFlow<FxUiState> = MutableStateFlow(uiState)
}
```

If a component needs initial data,
Provide Koin bindings in `src/commonMain/kotlin/di/ViewModelModule.kt`
