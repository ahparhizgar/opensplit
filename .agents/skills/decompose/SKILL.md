---
name: decompose
description: Decompose readme page
---

## Why Decompose?
Decompose breaks the code down into small and independent components and organizes them into trees. Each parent component is only aware of its immediate children.
Business logic code is testable with pure multiplatform unit tests.
Navigation state is fully exposed.
Navigation is a pure function from the old state to a new one - navigate without limits.
Proper dependency injection (DI) and inversion of control (IoC) via constructor.
Shared navigation logic
Lifecycle-aware components.
Components in the back stack are not destroyed, they continue working in background without UI
State preservation.

## Decompose components

for each screen create a Decompose component like this for feature "Fx":
file path: `src/commonMain/kotlin/feature/FxComponent.kt`
Imports:

- import com.arkivanov.essenty.lifecycle.doOnResume
- import component.CContext
- import component.apiCallScope
- import io.ktor.client.HttpClient
- import io.ktor.client.call.body
- import io.ktor.client.request.get
- import korlibs.io.async.launch
- import com.arkivanov.decompose.value.MutableValue
- import com.arkivanov.decompose.value.Value

```kotlin
    interface FxComponent {
    val uiState: Value<FxUiState>
    fun clickOnXxButton() {}
    interface Factory {
        fun create(context: CContext): FxComponent
    }
}

data class FxUiState(
    val data1: String = "Hello!",
    val data2: Boolean = true,
)

class DefaultFxComponent(
    context: CContext,
    val httpClient: HttpClient,
) : FxComponent, CContext by context {
    private val _uiState = MutableValue(FxUiState())
    override val uiState: Value<FxUiState> = _uiState
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
        data1 = "Hello!",
        data2 = true
    )
) : FxComponent {
    override val uiState: Value<FxUiState> = MutableValue(uiState)
}
```

Always provide Koin bindings in `DecompoesModule.kt`
