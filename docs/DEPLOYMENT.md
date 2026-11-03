# 部署说明

## 推荐架构

```text
浏览器
  └── HTTPS 域名 / Nginx
        ├── /          -> frontend/dist
        ├── /api/      -> Spring Boot :8080
        └── /uploads/  -> Spring Boot :8080（持久化上传目录）
                         └── MySQL 8
```

前端和后端使用同一域名可以减少跨域配置，并让前端现有的 `/api` 路径直接工作。

## 准备云数据库

在云数据库控制台创建 MySQL 8 数据库，例如 `nadouqu`，并设置独立的强密码。不要使用 `root` 账号运行线上服务。

建议先用 `database/schema.sql` 初始化表结构，正式环境将 `DDL_AUTO` 设置为 `validate`，避免应用自动修改数据库结构。

## 部署后端

构建：

```bash
cd backend
mvn clean package
```

上传 `target/*.jar` 到服务器并设置以下环境变量：

```text
DB_URL=jdbc:mysql://数据库地址:3306/nadouqu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
DB_USERNAME=数据库用户
DB_PASSWORD=数据库密码
JWT_SECRET=至少32位的随机密钥
SPARK_API_PASSWORD=星火API凭证
ADMIN_PASSWORD=管理员密码
DDL_AUTO=validate
SHOW_SQL=false
```

启动：

```bash
java -jar nadouqu-backend-1.0.0-SNAPSHOT.jar
```

上线时建议使用 systemd、Docker 或其他进程管理工具，保证服务异常退出后可以自动重启。

## 部署前端

```bash
cd frontend
npm ci
npm run build
```

将 `frontend/dist/` 上传到服务器静态目录，并使用 Nginx 同时代理后端：

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/travel-platform;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
}
```

使用 Certbot 或云平台证书功能启用 HTTPS。

## 上线检查

1. 用手机移动网络打开域名，确认不是只在本机可访问。
2. 测试注册、登录、搜索、下单、图片上传和消息功能。
3. 确认数据库和上传目录使用持久化存储。
4. 修改默认管理员密码，并确认 `.env`、密钥和数据库备份没有提交到仓库。
5. 配置数据库备份、日志轮转和服务器监控。
6. 面向中国内地长期提供服务时，确认服务器、域名和备案要求。
