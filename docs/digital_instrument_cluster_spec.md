# Digital Instrument Cluster Technical Specification

## 1. Overview

This document describes the architecture, functional scope, and delivery plan for a greenfield
Digital Instrument Cluster (DIC) targeting Android Automotive OS deployments. The cluster is the
primary driver-facing display located behind the steering wheel and must present critical vehicle
data, alerts, and contextual information with high reliability and minimal driver distraction.

## 2. Goals

* Deliver a modular, extensible cluster that can render primary driving metrics (speed, RPM,
fuel, temperature, gear state) as well as ADAS, navigation, and multimedia information.
* Support branded day/night themes, localization, and multiple measurement units.
* Maintain deterministic rendering at 60 FPS with sub-100 ms latency between data reception and
display updates.
* Comply with regional regulatory standards (FMVSS, EU GSR, AIS 071) for iconography,
alert behaviour, and fail-safe operation.

## 3. Functional Requirements

### 3.1 Primary Gauges

* Speedometer with configurable units (km/h, mph) and optional cruise-control setpoint marker.
* Tachometer with redline indication and engine warmup range highlighting.
* Fuel level and engine coolant temperature gauges with low/high threshold alerts.
* Gear indicator supporting automatic and manual transmissions, including EV drive modes.
* Odometer and trip computer (Trip A/B) with reset controls.

### 3.2 Indicators and Tell-Tales

* Direction indicators, headlight status (low/high beam), fog lights, hazard lights.
* Safety systems: ABS, ESC, airbag, seatbelt, TPMS, parking brake, battery, oil pressure.
* Diagnostic warnings: check engine, service reminders, system faults with severity levels.
* All tell-tales must support icon assets that comply with ISO 2575 and referenced regional
regulations.

### 3.3 Navigation & ADAS

* Turn-by-turn prompts, distance-to-turn, ETA, lane guidance, speed limit signs.
* ADAS notifications: lane keeping assistance, adaptive cruise control, forward collision warning,
blind spot monitoring.
* Integration with CarNavigationStatusManager for active guidance sessions and
CarAppFocusManager for focus arbitration.

### 3.4 User Interface & Interaction

* Layout presets (Classic analog, Minimal digital, Hybrid navigation focus) switchable via
steering-wheel buttons or infotainment commands.
* Day/Night themes with automatic switching using ambient light sensors; manual override is
available.
* Localization covering at least English, German, French, Spanish, Russian; extendable via
Android string resources and pluralization rules.
* Accessibility options: configurable font scaling, high-contrast mode, color-blind-safe palettes.

### 3.5 Data & Connectivity

* VehicleDataProvider abstraction delivering standardized data classes for vehicle metrics.
* Implementations:
  * `CanBusVehicleDataProvider` consuming CAN frames via JNI/NDK bridge.
  * `CarApiVehicleDataProvider` leveraging Android Automotive Car APIs on supported hardware.
  * `SimulatedVehicleDataProvider` for emulators and CI testing.
* Real-time event streaming with Kotlin Flows, delivering updates at 30 Hz minimum.
* OTA update support with signed packages, delta updates, and rollback capabilities.

### 3.6 Diagnostics & Logging

* Structured logging with log levels, persisted ring buffer for last 30 minutes of telemetry.
* Health monitoring (watchdog, heartbeat) with fail-safe fallback layout if rendering pipeline or
data inputs fail.
* Remote diagnostics hooks via secure WebSocket or gRPC channel, gated by role-based access.

## 4. Non-Functional Requirements

| Attribute          | Target | Notes |
| ------------------ | ------ | ----- |
| Availability       | ≥99.9% | Watchdog restarts, redundancy in data feeds. |
| Rendering latency  | ≤ 100 ms | From data reception to UI update. |
| Frame rate         | 60 FPS | Graceful degradation to 30 FPS under load. |
| Boot time          | ≤ 5 s | From ignition to usable cluster screen. |
| Operating temp     | -40°C to 85°C | Tested via environmental chambers. |
| Security           | TLS 1.3, signed payloads | Includes mutual authentication for remote access. |

Additional requirements include compliance with UNECE R121 and ISO 26262 ASIL-B for safety related
software components where applicable.

## 5. Architecture

### 5.1 High-Level Components

```
+---------------------------------------------------------------+
|                       Digital Instrument Cluster              |
|                                                               |
|  +----------------+   +---------------------+   +-----------+ |
|  | Data Layer     |   | Domain/View Models  |   | UI Layer  | |
|  |--------------- |   |---------------------|   |-----------| |
|  | VehicleData    |-->| ClusterViewModels   |-->| Compose    | |
|  | Providers      |   | Navigation Manager  |   | Surfaces   | |
|  | Diagnostics    |   | Theme Controller    |   | Animations | |
|  +----------------+   +---------------------+   +-----------+ |
|                                                               |
|  +---------------+   +--------------------+   +-------------+ |
|  | Platform Svc  |<->| Renderer Service   |<->| Car Service | |
|  +---------------+   +--------------------+   +-------------+ |
+---------------------------------------------------------------+
```

### 5.2 Module Breakdown

* **app**: Jetpack Compose UI, themes, navigation between cluster layouts, localization resources.
* **data**: Data models, repository interfaces, VehicleDataProvider implementations, caching,
security verification.
* **service**: InstrumentClusterRendererService, CarService bindings, surface management for
secondary display, message routing to UI process.
* **navigation**: Optional module providing map rendering surfaces, route previews, and
integration with navigation providers.
* **diagnostics**: Watchdog, logging, OTA client, remote maintenance utilities.

Gradle configuration will treat each module as an Android library, with `app` packaging the final
APK that declares the renderer service in the manifest.

### 5.3 Data Flow

1. Vehicle sensors publish CAN frames or Car API updates.
2. Corresponding VehicleDataProvider parses values into strongly typed DTOs.
3. Data repositories normalize units, validate ranges, and expose Kotlin Flows.
4. ClusterViewModels combine streams (e.g., speed + speed limit) and expose UI state models.
5. Compose UI renders state with smooth animations; theme controller applies color scheme.
6. InstrumentClusterRendererService binds surfaces, forwards user inputs, and enforces
cluster lifecycle with CarService.

### 5.4 Navigation Integration

* Use `CarNavigationStatusManager` to receive navigation status and metadata.
* Register for `NavigationState` updates containing turn lists, distance, ETA, lane guidance.
* Provide UI surfaces to navigation apps via `ClusterNavigationSurfaceController`.
* Handle focus changes through `CarAppFocusManager` to prioritize navigation prompts over media.

### 5.5 Security & Safety

* All VehicleDataProvider implementations must validate message authenticity; CAN frames use
hardware security module derived keys where available.
* Implement tamper detection; suspicious data triggers fail-safe mode with limited functionality
and driver alert.
* OTA updates signed with OEM private key; bootloader verifies signature before applying.
* Separation of concerns between safety-critical rendering (speed, RPM) and auxiliary info;
non-critical modules must not block critical updates.

## 6. Data Model

| Entity | Fields | Description |
| ------ | ------ | ----------- |
| `VehicleSpeed` | value, unit, timestamp, isValid | Primary speed reading. |
| `EngineRpm` | value, redline, warmupThreshold, timestamp | Engine RPM. |
| `FuelLevel` | percentage, rangeEstimateKm, warningThreshold | Fuel gauge data. |
| `EngineTemp` | temperatureC, warningThresholds | Coolant temperature. |
| `GearState` | gear, mode, isSport, isEco | Transmission state. |
| `Odometer` | totalKm, tripKmA, tripKmB | Distance metrics. |
| `WarningIndicator` | type, severity, iconId, message | For tell-tales. |
| `NavigationPrompt` | maneuverType, distanceMeters, laneInfo, speedLimit | Guidance data. |
| `ThemeConfig` | name, palette, typographyScale, isNight | Theme metadata. |

DTOs reside in the `data` module with serialization helpers for diagnostics export.

## 7. UI/UX Guidelines

* Compose surfaces should leverage `Canvas` APIs for analog gauges and `AnimatedContent` for smooth
transitions.
* Maintain safe areas to accommodate different screen sizes and aspect ratios; use vector assets
for scalability.
* Provide a notification ribbon at the top/bottom for high-priority warnings with color-coded
severity (green, amber, red) following regulatory guidance.
* Avoid dense text; show at most two lines of supplemental information per gauge.
* Include demo mode overlays for showroom or testing scenarios.

## 8. Testing Strategy

* **Unit Tests**: ViewModel logic, data normalization, warning threshold calculations.
* **Instrumented Tests**: Compose UI rendering on emulator with virtual secondary display
configuration.
* **Integration Tests**: Simulated CAN data playback to validate animation smoothness and latency.
* **Performance Tests**: Frame rate and latency profiling using Android GPU Profiler.
* **Safety Validation**: Fault injection to ensure fail-safe layout activation.

## 9. Deployment & Maintenance

* CI/CD pipeline using GitHub Actions or Jenkins: lint, unit/instrumented tests, static analysis
(Detekt, ktlint), signed build artifacts.
* OTA delivery via secure update manager; incremental updates supported through bsdiff or
Android App Bundles.
* Logging aggregation compatible with OEM telemetry backend; data retention policies aligned with
privacy regulations.

## 10. Roadmap

| Phase | Duration | Deliverables |
| ----- | -------- | ------------ |
| Phase 0: Research | 4 weeks | Requirements validation, hardware interface study, prototype UI mocks. |
| Phase 1: Platform Setup | 3 weeks | Gradle project, module scaffolding, basic renderer service. |
| Phase 2: Core Gauges | 6 weeks | Compose components for speedometer, tachometer, fuel, temp; simulator provider. |
| Phase 3: Navigation & ADAS | 5 weeks | Navigation integration, ADAS alerts, focus management. |
| Phase 4: Diagnostics & OTA | 4 weeks | Logging, health monitoring, OTA pipeline. |
| Phase 5: Compliance & Validation | 6 weeks | Regulatory review, fault injection, field testing. |
| Phase 6: Production Readiness | 3 weeks | Performance optimization, documentation, release candidate. |

## 11. Open Questions

* Hardware abstraction for security modules (HSM) availability per target vehicle program.
* Required level of integration with head-unit infotainment (media controls, voice assistant?).
* Scope of multi-cluster (driver + passenger) synchronization.
* Regulatory certification process timeline per target markets.

