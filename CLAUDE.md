# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **高校教资刷题** (college teacher certification exam practice) system with three sub-projects:

| Sub-project | Directory | Tech Stack |
|-------------|-----------|------------|
| Mini-program frontend | `./` (root) | UniApp Vue3 + TypeScript + Pinia + Vant Weapp |
| Backend | `./backend/` | SpringBoot 3.2 + MyBatis-Plus 3.5 + MySQL + Redis + Redisson |
| Admin frontend | `./admin-frontend/` | Vue3 + Element Plus + TypeScript + Vite |

## Common Commands

### Mini-program (root)
```bash
pnpm dev:mp-weixin          # Dev mode (output to dist/dev/mp-weixin)
pnpm build:mp-weixin        # Production build
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
