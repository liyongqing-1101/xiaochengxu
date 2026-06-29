# 高校教资题库

基于 UniApp Vue3 + TypeScript + Pinia + Vant Weapp 的微信刷题小程序

## 技术栈

- **框架**: UniApp 3.0 + Vue 3 + TypeScript
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **UI 组件库**: Vant Weapp (按需引入)
- **样式**: SCSS + 全局变量/混入
- **后端对接**: SpringBoot 3 + JWT

## 项目结构

```
src/
├── pages/          # 主包页面 (index/login/study)
├── subpackages/    # 分包 (answer/wrong-book/profile/placeholder)
├── components/     # 公共组件 (common/question/tabbar/search/category)
├── composables/    # 组合式函数
├── stores/         # Pinia 状态管理
├── api/            # API 接口层 + 拦截器
├── types/          # TypeScript 类型定义
├── utils/          # 工具函数 (storage/request/format)
├── config/         # 配置 (category/question/theme)
├── styles/         # 全局样式 (variables/mixins/reset)
└── static/         # 静态资源
```

## 快速开始

### 环境要求

- Node.js >= 18
- pnpm >= 8 (推荐) 或 npm
- 微信开发者工具

### 安装依赖

```bash
pnpm install
```

### 开发运行

```bash
# 微信小程序开发模式
pnpm dev:mp-weixin
```

### 生产构建

```bash
pnpm build:mp-weixin
```

### 导入微信开发者工具

1. 打开微信开发者工具
2. 导入项目，选择 `dist/dev/mp-weixin` (开发) 或 `dist/build/mp-weixin` (生产)
3. 在 `src/manifest.json` 中替换 `__WEIXIN_APPID__` 为你的小程序 AppID
4. 在 `.env.development` 中配置后端 API 地址 `VITE_API_BASE`

### 类型检查

```bash
pnpm type-check
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE` | 后端 API 基础地址 | `http://localhost:8080/api` |
| `VITE_WX_APPID` | 微信小程序 AppID | `wx0000000000000000` |

## 扩展指南

### 新增考试分类

1. 在 `src/types/enums.ts` 中 `CategoryId` 枚举添加新成员
2. 在 `src/config/category.config.ts` 中 `CATEGORY_REGISTRY` 添加新配置
3. 后端提供对应 `categoryId` 的数据接口

无需修改任何页面代码。

### 新增题目类型

1. 在 `src/types/enums.ts` 中 `QuestionType` 枚举添加新成员
2. 在 `src/subpackages/answer/components/` 中创建新题型组件
3. 在答题页 `pages/index.vue` 中添加题型到组件映射

## 后端接口

所有接口需携带:
- Header `Authorization: Bearer <token>` (JWT 认证)
- Header `X-Category-Id: 1` (考试分类)
- 401 自动跳转登录页

接口模块: `src/api/modules/`

## License

MIT
