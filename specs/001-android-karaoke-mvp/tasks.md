# Tasks: Android Karaoke MVP

**Input**: Design documents from `/specs/001-android-karaoke-mvp/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are not explicitly requested in the feature specification, so test tasks are omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile + API**: `backend/src/`, `android/app/src/main/java/`
- Paths adjusted based on plan.md Mobile + API structure

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create backend directory structure with src/, tests/, requirements.txt
- [x] T002 Initialize Python project with FastAPI, Demucs, and Spleeter dependencies in backend/requirements.txt
- [x] T003 [P] Configure Python linting and formatting tools (flake8, mypy, black) in backend/pyproject.toml
- [x] T004: Create Android project structure (build.gradle.kts, AndroidManifest.xml)
- [x] T005: Add Android dependencies (Room, OkHttp, Hilt, ExoPlayer)
- [x] T006: Configure Kotlin compiler settings and optimize for performance

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T007: Create Room database entities (Track, Processing, Session) with proper relationships and constraints  
- [x] T008: Set up Room database configuration with type converters, migration support, and database module  
- [x] T009: Create Retrofit API service with OkHttp configuration, authentication and logging interceptors
- [x] T010: Create WebSocket client for real-time progress updates with reconnection and subscription logic  
- [x] T011: Implement data transfer objects (DTOs), domain models, and data mappers for API communication  
- [x] T012: Create repository interfaces and implementations with Clean Architecture pattern  
- [x] T013: Configure Hilt modules for dependency injection (database, network, repository, WebSocket)  
- [x] T014: Set up MainActivity with navigation, common UI components, dialogs, and progress indicators  
- [x] T015: Implement centralized error handling, logging, and user feedback mechanisms

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Voice Separation Processing (Priority: P1) 🎯 MVP

**Goal**: Users can upload audio files and get instrumental versions back

**Independent Test**: Upload one audio file through Android app and receive processed instrumental for playback

### Implementation for User Story 1

- [ ] T016 [P] [US1] Create Song entity and DAO in android/app/src/main/java/com/dentim/karaoke/data/model/Song.kt
- [ ] T017 [P] [US1] Create ProcessingTask entity and DAO in android/app/src/main/java/com/dentim/karaoke/data/model/ProcessingTask.kt  
- [ ] T018 [P] [US1] Create Instrumental entity and DAO in android/app/src/main/java/com/dentim/karaoke/data/model/Instrumental.kt
- [ ] T019 [P] [US1] Implement upload endpoint with multipart file handling in backend/src/api/upload.py
- [ ] T020 [P] [US1] Implement task status endpoint in backend/src/api/tasks.py
- [ ] T021 [P] [US1] Implement download endpoint for processed files in backend/src/api/download.py
- [ ] T022 [US1] Create AI processing service with Demucs integration in backend/src/services/ai_processor.py
- [ ] T023 [US1] Implement file upload repository in android/app/src/main/java/com/dentim/karaoke/data/repository/UploadRepository.kt
- [ ] T024 [US1] Create upload use case with quality selection in android/app/src/main/java/com/dentim/karaoke/domain/UploadUseCase.kt
- [ ] T025 [US1] Implement file picker UI component in android/app/src/main/java/com/dentim/karaoke/ui/upload/FilePickerFragment.kt
- [ ] T026 [US1] Create upload progress tracking with WebSocket or polling in android/app/src/main/java/com/dentim/karaoke/network/ProgressTracker.kt
- [ ] T027 [US1] Implement audio playback functionality in android/app/src/main/java/com/dentim/karaoke/ui/player/AudioPlayer.kt
- [ ] T028 [US1] Add file validation and error handling for size/format limits
- [ ] T029 [US1] Implement automatic file cleanup after processing in backend/src/services/cleanup_service.py
- [ ] T030 [US1] Add retry mechanism with exponential backoff in android/app/src/main/java/com/dentim/karaoke/network/RetryInterceptor.kt

**Checkpoint**: At this point, User Story 1 should be fully functional - users can upload files and get instrumentals back

---

## Phase 4: User Story 2 - Song Selection Interface (Priority: P2)

**Goal**: Users see a convenient interface for managing their song library

**Independent Test**: Navigate interface, select songs, manage library without requiring audio processing

### Implementation for User Story 2

- [ ] T031 [P] [US2] Create song list adapter and ViewHolder in android/app/src/main/java/com/dentim/karaoke/ui/songlist/SongListAdapter.kt
- [ ] T032 [P] [US2] Implement main screen layout with RecyclerView in android/app/src/main/res/layout/fragment_song_list.xml  
- [ ] T033 [P] [US2] Create song detail view layout in android/app/src/main/res/layout/fragment_song_detail.xml
- [ ] T034 [US2] Implement SongRepository for local database operations in android/app/src/main/java/com/dentim/karaoke/data/repository/SongRepository.kt
- [ ] T035 [US2] Create song list ViewModel with LiveData in android/app/src/main/java/com/dentim/karaoke/ui/songlist/SongListViewModel.kt
- [ ] T036 [US2] Implement song detail fragment with processing options in android/app/src/main/java/com/dentim/karaoke/ui/songdetail/SongDetailFragment.kt
- [ ] T037 [US2] Add navigation between song list and detail screens using Navigation Component
- [ ] T038 [US2] Implement file browser integration for adding new songs in android/app/src/main/java/com/dentim/karaoke/ui/filebrowser/FileBrowserFragment.kt
- [ ] T039 [US2] Add song metadata extraction from audio files in android/app/src/main/java/com/dentim/karaoke/utils/MetadataExtractor.kt
- [ ] T040 [US2] Create quality selection UI component in android/app/src/main/java/com/dentim/karaoke/ui/quality/QualitySelectionDialog.kt

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - full song management + processing

---

## Phase 5: User Story 3 - Processing Progress Monitoring (Priority: P3)

**Goal**: Users see detailed progress with cancellation options and notifications

**Independent Test**: Monitor progress and notifications independently of processing quality

### Implementation for User Story 3

- [ ] T041 [P] [US3] Implement WebSocket endpoint for real-time progress in backend/src/api/websockets.py
- [ ] T042 [P] [US3] Create cancel task endpoint in backend/src/api/tasks.py  
- [ ] T043 [P] [US3] Implement detailed progress tracking ViewModel in android/app/src/main/java/com/dentim/karaoke/ui/progress/ProgressViewModel.kt
- [ ] T044 [US3] Create progress monitoring fragment with progress bar in android/app/src/main/java/com/dentim/karaoke/ui/progress/ProgressFragment.kt
- [ ] T045 [US3] Implement WebSocket client for real-time updates in android/app/src/main/java/com/dentim/karaoke/network/WebSocketClient.kt
- [ ] T046 [US3] Add task cancellation functionality with confirmation dialog in android/app/src/main/java/com/dentim/karaoke/ui/progress/
- [ ] T047 [US3] Implement push notifications for completion status in android/app/src/main/java/com/dentim/karaoke/notifications/
- [ ] T048 [US3] Create background processing service with foreground notification in android/app/src/main/java/com/dentim/karaoke/service/ProcessingService.kt
- [ ] T049 [US3] Add estimated time remaining calculation in backend/src/services/progress_estimator.py
- [ ] T050 [US3] Implement fallback HTTP polling when WebSocket fails in android/app/src/main/java/com/dentim/karaoke/network/PollingFallback.kt

**Checkpoint**: All core user stories should now be independently functional with excellent UX

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T051 [P] Create comprehensive error messages and user feedback in android/app/src/main/res/values/strings.xml
- [ ] T052 [P] Add API documentation and health check endpoint in backend/src/api/health.py
- [ ] T053 [P] Implement proper logging throughout backend services in backend/src/core/logging.py
- [ ] T054 Add connection timeout and network error handling across all Android network calls
- [ ] T055 [P] Create app icons and splash screen in android/app/src/main/res/
- [ ] T056 [P] Optimize AI model loading and memory usage in backend/src/services/ai_optimizer.py
- [ ] T057 Add database migration strategy for schema updates in android/app/src/main/java/com/dentim/karaoke/data/migrations/
- [ ] T058 [P] Run quickstart.md validation and update documentation
- [ ] T059 Create Docker configuration for backend deployment in backend/Dockerfile
- [ ] T060 [P] Add analytics and crash reporting setup in android/app/build.gradle.kts

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories  
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Integrates with US1 database models but independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Enhances US1 processing but independently testable

### Within Each User Story

- Models before services (database entities must exist first)
- Services before UI (business logic before presentation)
- Core implementation before integration features
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- Models within a story marked [P] can run in parallel (different entities)
- Backend and Android tasks can run in parallel when working on different files

---

## Parallel Example: User Story 1

```bash
# Launch all models for User Story 1 together:
Task: "Create Song entity and DAO in android/.../Song.kt"
Task: "Create ProcessingTask entity and DAO in android/.../ProcessingTask.kt"  
Task: "Create Instrumental entity and DAO in android/.../Instrumental.kt"

# Launch backend endpoints in parallel:
Task: "Implement upload endpoint in backend/src/api/upload.py"
Task: "Implement task status endpoint in backend/src/api/tasks.py"
Task: "Implement download endpoint in backend/src/api/download.py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T006)
2. Complete Phase 2: Foundational (T007-T015) - CRITICAL foundation
3. Complete Phase 3: User Story 1 (T016-T030)
4. **STOP and VALIDATE**: Test complete upload → processing → download → playback flow
5. Deploy/demo basic karaoke functionality

### Incremental Delivery  

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP - basic karaoke!)
3. Add User Story 2 → Test independently → Deploy/Demo (+ song library management)  
4. Add User Story 3 → Test independently → Deploy/Demo (+ advanced progress tracking)
5. Each story adds value without breaking previous functionality

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (T001-T015)
2. Once Foundational is done:
   - Android Developer: User Story 1 Android tasks (T016-T018, T023-T028, T030)
   - Backend Developer: User Story 1 Backend tasks (T019-T022, T029)  
   - Additional Developer: User Story 2 preparation or parallel story work
3. Stories integrate through defined APIs and complete independently

---

## Notes

- [P] tasks = different files, no dependencies, can run simultaneously
- [Story] label maps task to specific user story for traceability  
- Each user story delivers independently testable functionality
- Mobile + API architecture allows backend/Android parallel development
- MVP achievable with just User Story 1 (core karaoke functionality)
- User Stories 2-3 add UX improvements without breaking core features
- Avoid: vague tasks, same file conflicts, cross-story blocking dependencies