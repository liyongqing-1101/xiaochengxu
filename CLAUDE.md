# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **高校教资刷题** (college teacher certification exam practice) system with three sub-projects:

| Sub-project | Directory | Tech Stack |
|-------------|-----------|------------|
| Mini-program frontend | `./user-frontend/` | UniApp Vue3 + TypeScript + Pinia + Vant Weapp |
| Backend | `./backend/` | SpringBoot 3.2 + MyBatis-Plus 3.5 + MySQL + Redis + Redisson |
| Admin frontend | `./admin-frontend/` | Vue3 + Element Plus + TypeScript + Vite |

## Common Commands

### Mini-program (user-frontend)
```bash
cd user-frontend
pnpm dev:mp-weixin          # Dev mode (output to dist/dev/mp-weixin)
pnpm build:mp-weixin        # Production build → dist/build/mp-weixin
pnpm type-check             # TypeScript type checking
```

### Backend
```bash
cd backend
mvn clean package -DskipTests   # Build JAR
mvn spring-boot:run             # Run dev server on port 8080
```
- API docs at `http://localhost:8080/api/doc.html` (Knife4j)
- Requires MySQL 8 (`exam_db`) and Redis running locally
- Run `src/main/resources/db/schema.sql` to initialize database

### Admin Frontend
```bash
cd admin-frontend
npm install && npm run dev   # Dev server on port 3001, proxies /api to :8080
npm run build                # Production build (includes vue-tsc type check)
```

## Architecture

### API contract (locked)
All API responses use the format `{ code: 0, message: "ok", data: T }`. Code 0 = success. This is defined in `backend/.../common/Result.java` and consumed by `admin-frontend/src/api/request.ts` (interceptor unwraps `res.data` on code 0).

### Dual JWT authentication
- **Mini-program**: 7-day tokens, validated by `JwtAuthFilter`. Token passed via `Authorization: Bearer <token>` header.
- **Admin**: 2-hour tokens with role check, validated by `AdminAuthFilter`. Separate `admin_user` table with BCrypt passwords.
- Both filters are registered in `WebMvcConfig.java` with different path patterns.

### Category hierarchy (4-level tree)
```
ExamCategory (考试大类, e.g. 高校教资=1) → ExamSubject (科目) → ExamChapter (章节) → ExamTag (知识点)
```
- Category 1 = 高校教资 is the only active category; IDs 2 (计算机二级) and 3 (软考) are reserved extension points.
- Entities have `@TableField(exist=false) List children` for tree building.
- Mini-program `ExamController` returns this hierarchy with Redis caching.
- Admin `AdminCategoryController` provides CRUD with type/parentId parameters.

### Answer format
Answers are stored as JSON string arrays in `exam_question.answer`:
- Single choice: `["A"]`
- Multiple choice: `["A","C"]`
- True/False: `["T"]` or `["F"]`
- Excel import converts: `"A"` → `["A"]`, `"A,C"` → `["A","C"]`

### Question types
Only three types: 1=单选, 2=多选, 3=判断. Defined as enum `QuestionType` in the mini-program types and as `type` column in `exam_question`.

### Practice session flow
1. `POST /question/session/start` — picks random questions by filters, creates session in Redis
2. `POST /question/session/submit` — checks answer (normalized sort comparison), records result
3. `POST /question/session/end` — aggregates stats, returns `SessionSummary`

### Excel import pipeline
1. User uploads `.xlsx` → `AdminImportController` saves temp file, creates `ImportTask` (status=PENDING)
2. `@Async` on `ImportServiceImpl.executeImport()`:
   - POI XSSFWorkbook streaming read via `ExcelImportUtil` (NOT loading all rows into memory)
   - Batch validate + dedup (stem MD5) + insert 1000 rows at a time via MyBatis-Plus `saveBatch`
   - Progress written to Redis key `import:progress:{taskId}` (24h TTL)
   - Failed rows collected, exported to Excel, uploaded to OSS as error file
   - Updates `ImportTask` status → COMPLETED/FAILED
3. Admin frontend polls `GET /admin/import/progress/{taskId}` every 2 seconds

### OSS with watermark
- `OssService` wraps Aliyun OSS SDK (configurable via `app.oss.*` in application.yml)
- `OssWatermarkUtil` adds text watermark (bottom-right, 40% opacity, SansSerif) using Java Graphics2D

### Mini-program extension points
- **New category**: Add `CategoryId` enum member → add entry in `CATEGORY_REGISTRY` (`src/config/category.config.ts`) → backend provides data. No page code changes needed.
- **New question type**: Add to `QuestionType` enum → create answer component in `subpackages/answer/components/` → register in component mapping.

### Admin frontend patterns
- **Stores**: `auth.ts` (token/nickname/role + localStorage persistence), `question.ts` (list/filters/selectedIds)
- **API layer**: Axios interceptor injects `admin_token` from localStorage, unwraps `res.data`, redirects to `/login` on 401
- **Route guard**: `router.beforeEach` checks `admin_token` in localStorage, redirects to `/login` if absent
- **Layout**: `AdminLayout.vue` with fixed sidebar (220px) + header (56px) + scrollable main area
- Default admin credentials: `admin` / `admin123` (BCrypt hashed in schema.sql seed data)

### Thread pool configuration
Import executor defined in `ThreadPoolConfig.java`: core=4, max=8, queue=200, `CallerRunsPolicy`. This is the `@Async("importExecutor")` target.

### Key utility classes
- `RedisKeyUtil` — constants for all Redis key patterns (SESSION_PREFIX, IMPORT_PROGRESS_PREFIX, CATEGORY_CACHE_KEY)
- `ExcelImportUtil` — streaming POI reader with `ImportParseResult` (parsed rows + errors)
- `ExcelExportUtil` — template generation with data validation dropdowns + instruction sheet + error export

## ECS Deployment

### Server Information
- **Public IP**: `47.97.193.230`
- **SSH User**: `root`
- **SSH Password**: Stored in memory (not hardcoded)
- **OS**: Ubuntu 22.04

### Services Running
| Service | Port | Notes |
|---------|------|-------|
| MySQL 8.0 | 3306 | Database: `mini_wx_db`, running in Docker |
| Redis 7 | 6379 | Password-protected, running in Docker |
| Backend API | 8080 | SpringBoot JAR at `/root/xiaochengxu/backend/target/exam-backend-1.0.0.jar` |

### Deployment Workflow
1. **Build backend locally**:
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```
2. **Upload JAR via SCP/SFTP** to `/root/xiaochengxu/backend/target/`
3. **Restart backend**:
   ```bash
   # Kill old process
   fuser -k 8080/tcp
   # Start new process
   cd /root/xiaochengxu/backend/target
   nohup java -jar exam-backend-1.0.0.jar > /root/xiaochengxu/backend/app.log 2>&1 &
   ```
4. **Build mini-program frontend**:
   ```bash
   cd user-frontend
   pnpm build:mp-weixin
   ```
5. Import `dist/build/mp-weixin` into WeChat DevTools

### Docker Compose Location
- Path: `/root/docker-compose.yml`
- MySQL container name: `mysql8`
- Redis container name: `redis7`

## Recent Feature Implementations

### Random Exam Popup (Bottom Sheet)
- **File**: `user-frontend/src/components/exam/RandomExamPopup.vue`
- **Trigger**: Click "随机刷题" on home page
- **Behavior**:
  - Slides up from bottom as sheet with semi-transparent mask
  - Close on mask click or X button
  - 4 subjects to choose (mutually exclusive radio selection)
  - Start button disabled initially until subject selected
  - Gray button (disabled) → black button (enabled) on selection
  - No "all subjects" option (must pick one)
- **Type**: Native Vue component with CSS transform animation (not Vant Popup)

### Random Exam Session Logic
- **API Flow**: `POST /question/session/start` with subject filter
- **Exam Structure**: Single choice 40 × 1pt + Multiple choice 20 × 2pt + True/False 20 × 1pt = 100 pts
- **Timer**: 30 minutes countdown enforced by frontend
- **Backend stats endpoint**: `GET /question/subject/{subjectId}/stats` returns question counts per type

## Important Gotchas

### UniApp Mini-Program Specific
- **Dynamic imports may fail**: `const { fn } = await import(...)` destructuring can return `undefined` in WeChat mini-program environment. Use static imports for stores, API modules, and request utilities.
- **Pinia store availability**: `useUserStore()` at top-level in `App.vue` may return `undefined`. Access store inside functions/lifecycle hooks instead.
- **TabBar redirect loops**: On `pages/index/index.vue` and other tab pages, call `restoreSession()` BEFORE checking `isLoggedIn` in `onShow`. Token is in storage but Pinia state resets on page reload.
- **Popup z-index**: Fixed-position popups need `z-index: 999` to appear above everything.

### API Interceptor Pattern
- **Response format**: `{ code: 0, message: "ok", data: T }` is **locked**. Code 0 = success.
- **Interceptor MUST unwrap**: The mini-program response interceptor extracts `res.data` when `code === 0`. If not unwrapped, `result.token` will be undefined.

### Frontend Build
- Always use `pnpm build:mp-weixin` for production builds.
- The `dist/build/mp-weixin` folder is what gets imported into WeChat DevTools.
- If TypeScript errors block build, use `pnpm type-check` to see them first.

### Backend
- Excel import uses streaming POI reader (`ExcelImportUtil`) — never loads entire workbook into memory.
- Import task status is tracked in Redis via `ImportTask`.
- Question answer comparison uses normalized sort (order-agnostic).
