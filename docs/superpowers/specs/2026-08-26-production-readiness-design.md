# Earthquake Watchdog Production Readiness Design

## Objective

Prepare Earthquake Watchdog for repeatable, observable, secure Google Play production releases. Readiness is defined by automated release gates, verified user journeys, accurate privacy declarations, measured performance, and a staged rollout procedure with explicit stop and rollback conditions.

## Scope

This work covers:

- release build hardening and secret validation;
- reliability and removal of known crash paths;
- automated GitHub Actions quality and release workflows;
- Firebase Crashlytics integration with a privacy-safe reporting policy;
- unit, migration, Compose, navigation, and release smoke tests;
- Baseline Profiles and Macrobenchmark performance budgets;
- dependency and static-analysis governance;
- Play Console compliance and release operations;
- internal, closed, and staged production rollout gates.

It does not add new user-facing earthquake features or redesign existing screens. Product changes discovered during readiness testing are fixed only when they block an existing core journey.

## Current Baseline

The project already has feature ViewModels, repository abstractions, Room migrations, unit tests, Compose tests, navigation host tests, release signing configuration, Google Maps, AdMob consent handling, and a production application ID.

Known readiness gaps include:

- release minification and resource shrinking are disabled;
- dead legacy code containing `TODO("Not yet implemented")` remains in `DisclaimerDialog`;
- release signing and Maps values can silently resolve to empty strings;
- debug/location-related logging remains in production source;
- application backup is enabled without explicit backup rules;
- no CI workflow enforces tests, lint, or release assembly;
- no production crash reporting or release-health dashboard exists;
- no Baseline Profile or Macrobenchmark module exists;
- dependency declarations contain duplicates, alpha networking dependencies, and obsolete commented Gradle configuration;
- store compliance and rollout checks are not recorded as release gates.

## Architecture

Production readiness is organized as five sequential gates. A gate must be green before work is promoted to the next release stage.

### Gate 1: Release Safety

The release variant must fail during configuration when required signing or Maps credentials are absent. Secrets remain outside Git and are supplied locally through `keystore.properties` and in CI through encrypted GitHub secrets. Debug and release configurations must use distinct Maps credentials where possible, each restricted by package name and signing certificate.

Release builds enable R8 optimization and resource shrinking. Keep rules are added only when a verified release test demonstrates that reflection or serialization requires them. The application defines explicit Android backup and data-extraction rules, excludes location/preferences/cache data that should not leave the device, and removes obsolete permissions and manifest entries after verification.

Known crash placeholders, debug prints, and verbose logs containing coordinates, filter parameters, or internal state are removed. Purposeful failures are represented by domain errors and reported as sanitized non-fatal events where useful.

### Gate 2: Automated Quality

GitHub Actions uses a pinned Java and Gradle environment with dependency caching. Pull requests and pushes to `develop` run:

- Gradle wrapper validation;
- debug unit tests;
- Android lint with errors treated as failures;
- debug assembly;
- release compilation using non-production placeholder signing where signing is not required;
- dependency vulnerability/update reporting;
- checks that secrets and generated artifacts are not committed.

Instrumentation tests run on an Android emulator in a dedicated job. They include Room migration tests, Navigation 3 host journeys, and Compose rendering tests. Release-candidate tags trigger a separate signed AAB workflow that uses GitHub environments and encrypted secrets. Production deployment remains approval-gated; CI creates the artifact but does not silently publish it.

### Gate 3: Observability and Performance

Firebase Crashlytics is enabled for release builds and disabled or clearly separated for local debug builds. Collection follows the app's consent/privacy policy and never adds exact coordinates, addresses, advertising identifiers, or earthquake-user distance as custom keys. Reports may include app version, screen/destination name, operation category, and sanitized error category.

Expected network, storage, and location failures remain user-facing domain errors. Unexpected exceptions and high-value non-fatal failures are recorded at repository/ViewModel boundaries without duplicate reporting.

A benchmark module generates a Baseline Profile for cold start and the primary journeys: intro-to-home, list refresh/filter, map opening and marker selection, statistics opening, and settings opening. Macrobenchmarks establish repeatable startup and frame-timing measurements. Initial budgets are recorded from a release-like baseline, then CI blocks only material regressions relative to that baseline rather than imposing arbitrary device-independent timings.

### Gate 4: Product and Store Compliance

Core journeys are tested on API 26, a current API level, and at least one physical device. Testing covers fresh install, upgrade from the latest Play version, offline first launch, cached offline launch, permission grant/deny/permanent-deny, manual location, map key behavior, filter persistence, database migrations, theme/unit settings, ad consent, and process recreation.

Accessibility checks cover content descriptions, touch targets, font scaling, contrast, TalkBack traversal, and small-screen layouts. English and Italian resources are checked for missing or stale entries.

The Play Console Data safety form and privacy policy must match actual behavior for location, advertising, diagnostics, network access, retention, deletion, and third-party SDK sharing. AdMob/UMP consent is tested for EEA and non-EEA debug geography. Maps and ad identifiers are confirmed as production identifiers in the signed artifact.

### Gate 5: Controlled Release

The signed AAB is promoted through internal testing, closed testing, and production staged rollout. Each stage uses the same immutable artifact.

Promotion criteria include:

- all CI gates green;
- no open release-blocker defects;
- fresh-install and upgrade smoke tests passed;
- Crashlytics and Android Vitals dashboards operational;
- store listing, privacy policy, Data safety, ads declaration, content rating, and release notes reviewed;
- product owner approval recorded.

Production rollout starts at 5%, then advances to 20%, 50%, and 100% after observation windows. Rollout pauses when crash-free users materially regress from the previous stable version, ANR or crash rates exceed Play bad-behavior thresholds, a core journey fails, data loss occurs, consent behavior is wrong, or Maps/network configuration fails broadly. The rollback response is to halt rollout, retain diagnostics, prepare a higher-version-code hotfix, and document the incident; published Android artifacts are never replaced in place.

## Testing Strategy

Tests are added before production changes and demonstrate a red-green cycle where practical.

- Unit tests validate release-independent business behavior, sanitization, error mapping, and repository fallbacks.
- Room migration tests cover every supported schema upgrade from Play-distributed versions to the current schema.
- Compose tests render screens from state and verify critical actions and accessibility semantics.
- Navigation tests cover top-level destinations, details, back behavior, and process-state restoration where supported.
- Instrumentation journeys cover permission and location boundaries that cannot be represented reliably in local tests.
- Macrobenchmarks measure release-like startup and primary navigation performance.
- Manual release tests cover external systems: Play upgrade delivery, real Maps credentials, UMP geography, ads, Android Vitals, and Crashlytics delivery.

## Security and Privacy Policy

- No secret, keystore, service-account JSON, Maps key, or signing password is committed.
- CI secrets are scoped to a protected GitHub environment and unavailable to ordinary pull requests.
- Maps keys are restricted by Android package name, SHA-1/SHA-256 certificate fingerprints, and required APIs.
- Network traffic remains HTTPS-only unless a documented test-only exception exists.
- Logs and telemetry exclude exact user location, resolved address, consent payloads, and credentials.
- Backup rules exclude sensitive or unnecessary preferences and caches.
- Third-party SDKs are inventoried and reconciled with the privacy policy and Play Data safety declarations.

## Deliverables

- hardened release Gradle and manifest configuration;
- explicit backup/data-extraction and network-security policies;
- removed crash placeholders and production-noisy logs;
- Firebase Crashlytics integration and sanitized reporting facade;
- GitHub Actions pull-request, emulator-test, and signed-AAB workflows;
- completed regression and migration test matrix;
- benchmark module and Baseline Profile;
- dependency cleanup and automated update/security reporting;
- production-readiness checklist and release runbook;
- Play Console compliance checklist and staged-rollout record template.

## Completion Criteria

The initiative is complete when a single release-candidate commit produces a signed AAB through GitHub Actions, all automated gates pass, the artifact succeeds in internal and closed testing, Crashlytics and performance telemetry are verified, Play compliance records match app behavior, and the staged production rollout reaches 100% without a stop condition.
