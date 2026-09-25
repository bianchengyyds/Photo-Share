# Photo Share · 照片分享平台

一个前后端分离的照片分享与轻社交平台：上传照片、组织相册、生成带密码的分享链接，并支持点赞、嵌套评论、标签、关注、动态流、站内通知和多条件搜索。

- 前端：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Tailwind CSS
- 后端：Spring Boot 3.2 / Java 21 + MyBatis（注解式，无 XML）+ Spring Security（JWT）
- 数据库：MySQL 8（10 张表）
- 图片存储：可切换本地磁盘 / 七牛云对象存储
- 接口文档：SpringDoc / Swagger UI

## 功能一览

| 模块 | 说明 |
| --- | --- |
| 账号 | 注册、登录（BCrypt + JWT）、管理员角色 |
| 相册 | 新建/编辑/软删除、可见性开关（私密 / 公开）、封面 |
| 照片 | 多图上传、待审核流程、移动相册、回收站 |
| 分享 | 生成分享码与链接、访问密码、浏览量、通过链接协作上传 |
| 社交 | 点赞（相册与照片）、一级评论 + 多级回复（自动折叠到同一线程）、@ 提及、关注 / 粉丝 |
| 动态 | 关注的人公开出来的相册按时间倒序呈现 |
| 通知 | 点赞、评论、回复、@、关注等站内通知 |
| 标签 | 相册与照片打标签，按标签筛选 |
| 搜索 | 关键词 / 标签 / 相册 / 作者 / 时间范围组合检索，含用户搜索 |
| 管理 | 后台审核待审照片与内容 |

后端共 58 个接口，前端 13 个页面。

## 目录结构

```
.
├─ backend/                    Spring Boot 服务
│  ├─ application-secrets.yml.example   本地凭据模板（复制成 application-secrets.yml）
│  └─ src/main/resources/application.yml  只放 ${ENV:default} 占位符，不含真实密钥
├─ frontend/                   Vue 3 单页应用
├─ database/
│  ├─ schema.sql               完整建表脚本（结构，不含数据）
│  └─ migration_v3_*.sql       增量改表脚本
└─ docs/                       需求、技术设计与数据库文档
```

## 快速开始

### 1. 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 21 |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Node.js | 18+ |

### 2. 初始化数据库

```bash
mysql -u root -p < database/schema.sql
```

脚本会创建 `photo_share` 库和 10 张表，不含任何示例数据。账号注册后即可获得普通用户身份；管理员需手工把 `user.role` 改为 `ADMIN`。

### 3. 配置你的凭据

真实密钥不入库。后端读取顺序是：`环境变量` → `backend/application-secrets.yml` → `application.yml` 里的默认值。

```bash
cd backend
cp application-secrets.yml.example application-secrets.yml
# 编辑 application-secrets.yml，填入自己的 MySQL 账号密码
```

如果不想建这个文件，也可以直接给环境变量：

| 环境变量 | 默认值 | 用途 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/photo_share?...` | 数据源地址 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `STORAGE_TYPE` | `local` | `local` 写本机 `uploads/`；`qiniu` 走七牛 |
| `QINIU_ACCESS_KEY` / `QINIU_SECRET_KEY` / `QINIU_BUCKET` / `QINIU_DOMAIN` | 空 | 仅 `STORAGE_TYPE=qiniu` 时需要 |
| `JWT_SECRET` | 一个仅供本地的占位串 | HS256 签名密钥，**部署必须换成随机值**，长度 ≥ 32 字节 |
| `JWT_EXPIRATION` | `86400000` | token 有效期（毫秒） |

默认 `STORAGE_TYPE=local`，也就是说**没有七牛账号也能完整跑通上传**，图片落在 `backend/uploads/`。

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

监听 `http://localhost:8080`，Swagger UI 在 `http://localhost:8080/swagger-ui.html`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:3000`。Vite 已把 `/api` 与 `/uploads` 代理到 `8080`，所以本地开发不需要额外配跨域。

生产构建：`npm run build`（先跑 `vue-tsc` 类型检查），产物在 `frontend/dist/`。

## 关于密钥

仓库是公开的，因此以下内容被 `.gitignore` 排除，不会出现在任何提交里：

- `backend/application-secrets.yml`（本地真实凭据）
- `五部流程/` 整个目录（内部过程文档，其中几份存有第三方 AK/SK 与数据库口令；对外只保留脱敏后的 `docs/数据库设计.md`）
- `.trae/`、`target/`、`dist/`、`node_modules/`、`uploads/`

排除这些文件**不影响项目运行**：编译打包不需要它们，运行所需的结构定义在 `database/schema.sql`，配置项已改为占位符 + 本地覆盖文件，按上面第 3 步填自己的值即可。

需要提醒的是：`JWT_SECRET` 用默认值启动时，任何人都能伪造 token，仅可用于本机调试。

## 已知限制

- 相册搜索接口暂未支持按 `albumId` 过滤（前端筛选器里的相册条件对相册结果不生效，对照片结果生效）
- 分享链接的访问密码为明文存储，未做 BCrypt
- 评论 `created_at` 为秒级精度，同一秒内多条回复的顺序不保证稳定
- 全局异常处理会把底层 SQL 报错原文返回给客户端，生产环境应收敛
- CORS 目前是 `allowedOriginPatterns("*")` + `allowCredentials(true)`，上线需收紧到具体域名

## 文档

- `docs/数据库设计.md` — 表结构与字段说明
- `docs/admin-review-feature.md` — 后台审核功能设计
- `database/schema.sql` — 可直接执行的建表脚本
