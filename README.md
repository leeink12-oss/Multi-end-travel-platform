# 基于多端协同的综合旅游服务平台

一个面向游客、商家和管理员的多端旅游服务平台，提供景点、酒店、机票、火车票、餐饮团购、订单、收藏、评价、优惠券、消息通知和智能客服等功能。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vue Router、Element Plus、Axios、Vite |
| 后端 | Java 17、Spring Boot 3、Spring Security、Spring Data JPA、JWT |
| 数据库 | MySQL 8 |
| 构建 | Maven、npm |

## 项目结构

```text
travel-platform/
├── .github/workflows/       # GitHub Actions 自动构建
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/       # Java 源码
│   ├── src/main/resources/  # 应用配置
│   └── uploads/             # 本地运行时的上传目录（内容不提交）
├── frontend/                # Vue 3 前端
│   └── src/
├── database/schema.sql      # 已移除真实数据的数据库结构
├── docs/DEPLOYMENT.md       # 部署说明
├── .editorconfig
├── .gitattributes
└── .gitignore
```

## 本地运行

### 环境要求

- JDK 17 或更高版本
- Maven 3.9 或更高版本
- Node.js 20 或更高版本
- MySQL 8

### 1. 创建数据库

```sql
CREATE DATABASE nadouqu
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

项目默认使用 `ddl-auto=update` 自动创建或更新表。如果需要固定的数据库结构，也可以导入：

```text
database/schema.sql
```

### 2. 启动后端

在 PowerShell 中设置本次终端会话所需的环境变量：

```powershell
$env:DB_PASSWORD = "你的数据库密码"
$env:JWT_SECRET = "至少32位的随机字符串"
$env:ADMIN_PASSWORD = "你设置的管理员密码"
$env:SPARK_API_PASSWORD = "你的星火API密码"

cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

管理员用户名为 `admin`。如果没有设置 `ADMIN_PASSWORD`，本地默认密码为 `admin123`，部署前必须修改。

### 3. 启动前端

另开一个终端：

```powershell
cd frontend
npm ci
npm run dev
```

前端默认地址：`http://localhost:5173`

### 4. 构建

```powershell
cd backend
mvn clean package

cd ..\frontend
npm ci
npm run build
```

构建产物分别位于：

- 后端：`backend/target/*.jar`
- 前端：`frontend/dist/`

## 环境变量

后端配置模板位于 `backend/.env.example`，前端配置模板位于 `frontend/.env.example`。

| 变量 | 必填 | 说明 |
| --- | --- | --- |
| `DB_URL` | 否 | MySQL 连接地址 |
| `DB_USERNAME` | 否 | 数据库用户名 |
| `DB_PASSWORD` | 是 | 数据库密码 |
| `JWT_SECRET` | 是 | JWT 签名密钥，至少 32 个字符 |
| `ADMIN_PASSWORD` | 生产必填 | 初始化管理员密码 |
| `SPARK_API_PASSWORD` | 否 | 科大讯飞星火 API 凭证 |
| `DDL_AUTO` | 否 | JPA 建表策略，生产建议使用 `validate` |
| `SERVER_PORT` | 否 | 后端端口，默认 `8080` |
| `VITE_API_BASE_URL` | 否 | 前端接口前缀，默认 `/api` |
| `VITE_API_PROXY_TARGET` | 否 | 本地开发时的后端代理地址 |

不要提交真实的 `.env` 文件、数据库密码、JWT 密钥或第三方 API 密钥。

## 上传到 GitHub

如果电脑尚未安装 Git，请先从 [Git 官网](https://git-scm.com/downloads) 安装，然后在本目录执行：

```powershell
git init
git add .
git commit -m "chore: initialize travel platform repository"
git branch -M main
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

创建 GitHub 仓库时，不要勾选自动生成 README、`.gitignore` 或 License，避免与本地文件冲突。

## 数据与安全说明

- 原始数据库导出包含用户、订单、消息和联系方式，因此没有放入公开仓库。
- `database/schema.sql` 只保留表结构，不包含原始业务数据。
- `backend/uploads/` 中的运行时图片没有提交，部署时应使用持久化磁盘或对象存储。
- 原有数据库密码和星火 API 密钥已从源码配置中移除。如果旧项目曾经上传过这些信息，请立即更换对应密码和密钥。

部署步骤见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)。
