# Navigation 3 Migration Design

## Goal

Migrate Earthquake Watchdog from the current `androidx.navigation.compose` string-route setup to a Navigation 3 style, Compose-owned navigation model without changing user-facing behavior.

The migration should remove direct `NavHostController` usage from feature screens, replace string route construction with typed destinations, and keep the current intro, home, map, settings, credits, and details flows working exactly as they do today.

## Current Context

The app currently centralizes navigation in [app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationGraph.kt](/Users/simone/MyDOC/ANDROIDPRJ/PROJECT/MINE/EarthQuakeWatchDog_/EarthquakeWatchdog/app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationGraph.kt), with route strings defined in [app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationRoutes.kt](/Users/simone/MyDOC/ANDROIDPRJ/PROJECT/MINE/EarthQuakeWatchDog_/EarthquakeWatchdog/app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationRoutes.kt). `MainActivity` creates a `NavHostController`, `ScaffoldModel` and `AppBottomBar` inspect the controller back stack, and multiple screens receive the controller directly and perform navigation themselves.

This produces three recurring problems:

1. Navigation behavior is spread across the host, shared scaffold code, and feature screens.
2. The app relies on string routes and inline argument encoding for feature-to-feature navigation.
3. UI screens know too much about the navigation framework, which makes testing and incremental refactoring harder.

## Scope

This design covers the full app navigation migration:

- Intro
- Home / earthquake list
- Map
- Settings
- Credits
- Details
- Shared scaffold and bottom bar navigation behavior

This design does not include unrelated UI redesign, business-logic refactors, or feature behavior changes.

## Constraints

- Keep Compose, Hilt, Room, DataStore, Ktor, Google Maps, and current screen structure.
- Preserve current routes and flows from a user perspective.
- Preserve bottom navigation behavior and back navigation behavior.
- Preserve map opening from the list with optional coordinates.
- Preserve settings to credits navigation and details navigation.
- Avoid a big-bang feature rewrite beyond what is required by navigation migration.
- Keep the migration testable in small phases even though the target is a full navigation conversion.

## Recommended Approach

Use an internal compatibility migration:

1. Introduce a typed destination model and an app-owned back stack in Compose.
2. Replace the central host with a single app navigator/state object that renders the active destination.
3. Change screens from receiving `NavHostController` to receiving narrowly scoped callbacks such as `onOpenDetails`, `onOpenMap`, `onBack`, and `onOpenCredits`.
4. Migrate shared scaffold and bottom bar code to derive state from the typed destination stack.
5. Remove the old route constants and `NavHostController` wiring only after all screens are moved over.

This is safer than a pure big-bang rewrite because we can move one seam at a time while keeping the visible behavior stable, but it still reaches a full Navigation 3 style end state rather than stopping halfway.

## Target Architecture

### 1. Typed Destination Model

Add a sealed destination model in the navigation package, for example:

- `Intro`
- `Home`
- `Map(initialLatLng: LatLng?)`
- `Settings`
- `Credits`
- `Details(id: String)`

Rules:

- Each destination represents app intent, not route syntax.
- Destination arguments stay as typed properties.
- No screen builds navigation strings.
- No shared UI code needs to parse or normalize route text.

### 2. App-Owned Navigation State

Replace `rememberNavController()` in `MainActivity` with an app-owned navigation state holder created in Compose. That state holder will own:

- The current back stack
- Push navigation
- Top-level tab switching
- Back handling
- Single-top behavior for top-level destinations
- State restoration rules for bottom navigation

The state holder should expose a small, app-specific API rather than framework-shaped methods. For example:

- `navigateTo(destination)`
- `navigateBack()`
- `switchTopLevel(destination)`
- `replaceWith(destination)` if needed for intro-to-home behavior
- `currentDestination`
- `canNavigateBack`

### 3. Navigation Host Replacement

Replace `NavigationGraph` with a host composable that renders content from the typed destination back stack rather than from route registration.

That host is responsible for:

- Reading the current destination entry
- Rendering the corresponding screen
- Wiring screen callbacks to the navigator
- Keeping all feature-specific navigation decisions at the app shell boundary

This keeps individual screens unaware of the navigation library internals.

### 4. Screen API Refactor

Update screen composables so they no longer accept `NavHostController`.

Planned callback-oriented APIs:

- Intro screen:
  - `onContinueToHome`
- Earthquake list screen:
  - `onOpenDetails(id: String)`
  - `onOpenMap(initialLatLng: LatLng?)`
- Map screen:
  - `onBack`
- Settings screen:
  - `onOpenCredits`
- Credits screen:
  - `onBack`
- Details screen:
  - `onBack`

The goal is to make navigation a dependency injected by the app shell rather than something screens construct for themselves.

### 5. Shared Scaffold and Bottom Bar Refactor

Refactor `ScaffoldModel` and `AppBottomBar` so they read app navigation state instead of `NavHostController`.

They should derive:

- Whether the bottom bar is visible
- Which bottom tab is selected
- Whether a back button should be shown
- What happens when the user taps a tab

Top-level destination detection should be explicit from the destination type, not inferred from route text or hierarchy parsing.

### 6. Navigation Policy

The migration should preserve these policies:

- App starts on intro.
- Intro continues to home and removes intro from the back stack.
- Home, Map, and Settings are the only bottom-bar destinations.
- Tapping a selected bottom tab does not duplicate it.
- Switching bottom tabs restores their state as closely as current behavior allows.
- Details sits above Home and supports back.
- Credits sits above Settings and supports back.
- Opening Map from the list with earthquake coordinates centers the map on that point.
- Opening the default Map tab with no coordinates keeps the existing default map behavior.

## Destination and Data Rules

### Map Arguments

The current app encodes latitude and longitude in a string route. After migration, map opening should use typed data instead:

- `Map(initialLatLng = null)` for default map entry
- `Map(initialLatLng = LatLng(lat, lon))` when launched from an earthquake item

If `LatLng` proves awkward as a destination property for state saving, the fallback is a small app-local serializable value type such as:

- `MapCoordinate(latitude: Double, longitude: Double)`

and conversion to `LatLng` at the screen boundary.

### Details Arguments

Keep details navigation keyed by earthquake `id: String`, but pass it as a typed property rather than route text.

## File-Level Design

### Create

- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppDestination.kt`
  - Typed destination model
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigator.kt`
  - Navigation state holder and app-specific navigation API
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationHost.kt`
  - Destination rendering host

### Modify

- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/presentation/ui/MainActivity.kt`
  - Create app navigator instead of `rememberNavController()`
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/components/ScaffoldModel.kt`
  - Consume typed navigation state
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppBottomBar.kt`
  - Consume typed navigation state
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_intro/presentation/IntroScreen.kt`
  - Replace controller dependency with callback(s)
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/presentation/ui/EarthquakeListScreen.kt`
  - Replace controller dependency with callback(s)
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/presentation/ui/MapScreen.kt`
  - Replace controller dependency with callback(s)
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/presentation/ui/EarthquakeMapScreen.kt`
  - Thread callback-based navigation through to the scaffold as needed
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_settings/presentation/ui/SettingsScreen.kt`
  - Replace controller dependency with callback(s)
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_settings/presentation/ui/CreditsScreen.kt`
  - Replace controller dependency with callback(s)
- `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_details/presentation/ui/DetailsScreen.kt`
  - Replace controller dependency with callback(s)

### Remove After Migration

- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationGraph.kt`
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/NavigationRoutes.kt`
- Remaining route normalization helpers that only exist for string-navigation compatibility

## Error Handling

Navigation should become structurally safer because destination construction will be typed. The main remaining risks are state restoration and back-stack duplication. The navigation state holder must centralize:

- Rejection or normalization of invalid top-level switches
- Duplicate suppression for already selected top-level destinations
- Safe fallback when optional map coordinates are absent

No screen should need to guard against malformed route strings because those strings will no longer exist in feature code.

## Testing Strategy

### Unit Tests

Add focused tests for the app navigator:

- App starts with `Intro`
- Intro replacement with `Home` removes intro from back stack
- Switching among Home, Map, and Settings does not create duplicate top-level destinations
- Details push from Home adds a back destination
- Credits push from Settings adds a back destination
- Map destination supports both null and non-null initial coordinates
- Back navigation pops leaf destinations and stops correctly at top level

### Compose/UI Tests

Add lightweight navigation-host tests that verify rendering and callback wiring:

- Intro callback opens Home
- Home callback opens Details
- Home callback opens Map with coordinates
- Settings callback opens Credits
- Back from Details returns to Home

The goal is not exhaustive screen testing, but regression protection around the migration seams.

### Verification

Minimum verification before calling the migration complete:

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- Manual smoke test of intro, bottom tabs, details, credits, and list-to-map flow

If exact tab-state restoration from the old controller-based setup is not achievable with the same semantics, the acceptable fallback is:

- Preserve correct destination switching and back behavior first
- Preserve screen state for top-level destinations where practical
- Document any remaining restoration difference explicitly before merge

## Migration Sequence

1. Add typed destination model and navigator.
2. Add host composable that renders from typed destinations.
3. Move scaffold and bottom bar to navigator-driven state.
4. Migrate Intro callbacks and intro-to-home replacement.
5. Migrate Home list callbacks for details and map opening.
6. Migrate Settings and Credits callbacks.
7. Migrate Details back handling.
8. Remove old route graph, string routes, and controller-based helpers.
9. Run full verification and clean up unused imports and dependencies.

## Success Criteria

The migration is successful when:

- No feature screen accepts `NavHostController`.
- No feature screen builds or parses route strings.
- App navigation behavior is unchanged for users.
- Shared scaffold and bottom bar state are driven by typed destinations.
- The old navigation graph and route helper files are no longer needed.
- Any unavoidable top-level state restoration difference is either eliminated or explicitly documented.
- Old route graph files are removable without functional loss.
- Automated tests cover navigator behavior and critical navigation flows.
