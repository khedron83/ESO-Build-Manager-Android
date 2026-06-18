# ESO Build Manager (Android)

A Kotlin/Compose Android app for managing Elder Scrolls Online character builds. Mirrors the desktop app's data model and syncs via Nextcloud WebDAV.

- **Package**: `com.cubicserenity.esobuildmanager`
- **Min SDK**: 26 (Android 8.0) / **Target SDK**: 36
- **Version**: 1.0.0

## Tech Stack

| Area | Library |
|------|---------|
| UI | Jetpack Compose + Material3 |
| DI | Hilt |
| Navigation | Navigation Compose |
| Networking | OkHttp 4 |
| JSON | Gson |
| Local DB | Room |
| Preferences | DataStore |
| Async | Kotlin Coroutines |

No Retrofit (no REST API — sync uses raw WebDAV via OkHttp). No Coil (no images).

## Project Layout

```
app/src/main/java/com/cubicserenity/esobuildmanager/
  MainActivity.kt
  EsoBuildManagerApp.kt           Hilt Application class
  ui/
    NavGraph.kt                   Compose navigation (builds → detail → editor → settings)
    builds/                       Build list with search + role filter chips
    detail/                       Read-only build sheet
    editor/                       Tabbed build editor (Info/Skills/Gear/Stats/Notes)
    settings/                     Nextcloud server config
    theme/Theme.kt                ESO gold/dark color scheme + dynamic color
  data/
    local/
      AppDatabase.kt              Room database (builds, skills, gear)
      dao/BuildDao.kt
      entity/BuildEntity.kt       BuildEntity + SkillEntity + GearEntity + BuildWithDetails
    preferences/
      PreferencesDataStore.kt     ServerConfig stored in DataStore
    repository/
      BuildRepository.kt          Maps entities ↔ domain models, handles save/upsert
    sync/
      WebDavSyncClient.kt         Two-way WebDAV sync to ESO-Builds/ on Nextcloud
  di/
    AppModule.kt
    NetworkModule.kt              OkHttp client builder (shared with sync client)
  domain/model/
    Build.kt                      Build + Skill + GearPiece domain models
  util/
    Constants.kt                  ESO_CLASSES, ROLES, GEAR_SLOTS, traits, etc.
```

## Building

```bash
./gradlew assembleDebug
```

Build outputs land in `app/build/outputs/apk/`.

## Architecture

MVVM with a Repository layer. `BuildRepository` maps Room entities to domain models and handles all DB operations. `WebDavSyncClient` uploads all local builds to `ESO-Builds/{slug}.json` on Nextcloud, then downloads remote-only or newer same-name builds.

## Data Model

Mirrors the desktop app's SQLite schema exactly. JSON sync format is identical to the desktop `export_build_dict` format (with an added `_sync_updated_at` field for conflict resolution).

- `cp_slots`: JSON list[str] — 12 CP star names (4 per tree: Craft/Warfare/Fitness)  
- `class_masteries`: JSON list[str] — selected Class Mastery passive names

## Navigation

```
builds (BuildsScreen)
  ↓ tap build
detail/{buildId} (BuildDetailScreen)
  ↓ tap Edit
editor/{buildId} (BuildEditorScreen)

builds
  ↓ tap +
editor/0 (new build)

builds
  ↓ tap Settings
settings (SettingsScreen)
```

## Sync

Sync is triggered manually from the builds screen (sync icon in top bar). The `WebDavSyncClient.sync()` coroutine:
1. MKCOL `ESO-Builds/` (no-op if exists)
2. Upload all local builds as `ESO-Builds/{slug}.json`
3. PROPFIND to list remote files
4. Download and upsert any remote builds not uploaded in step 2
5. Returns `SyncResult(uploaded, downloaded, errors)`

JSON format is compatible with the desktop Python app's `export_build_dict` / `import_build_dict`.
