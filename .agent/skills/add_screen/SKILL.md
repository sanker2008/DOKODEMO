---
name: Add New Screen
description: Instructions for adding a new feature screen to the DokoDemo application.
---

# Add New Screen

Follow this workflow when adding a new functional screen to the application.

## 1. Define Route
- Open `app/src/main/java/com/dokodemo/navigation/NavRoutes.kt`.
- Add a new `Route` object for your screen.

## 2. Create ViewModel
- Create a new Kotlin class annotated with `@HiltViewModel`.
- Define a `UiState` data class to hold the screen's state.
- Expose the UI state as a `StateFlow`.

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    // ... dependencies
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
}

data class MyUiState(
    val isLoading: Boolean = false
    // ... other fields
)
```

## 3. Create Screen Composable
- Create a new Composable function.
- Use `Scaffold` as the root container.
- **CRITICAL**: Set `containerColor = IndustrialBlack` (from `ui.theme.Color`).
- Inject the ViewModel using `hiltViewModel()`.
- Adhere strictly to **Industrial Neubrutalism** (Pure Black background, No Rounded Corners).

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = IndustrialBlack,
        topBar = { /* ... */ }
    ) { paddingValues ->
        // Content
    }
}
```

## 4. Register Navigation
- Open `app/src/main/java/com/dokodemo/navigation/DokoNavHost.kt`.
- Add a `composable` entry for your new route.

```kotlin
composable(Route.MyNewScreen.path) {
    MyScreen(onNavigateBack = { navController.popBackStack() })
}
```
