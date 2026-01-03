# 前后端连接指南

## 1. 配置环境变量

已创建 `.env.local` 文件，配置了后端 API 地址：
```
VITE_API_BASE_URL=http://localhost:8080
```

**注意**：如果后端运行在其他端口，请修改此文件。

## 2. 后端配置（重要：CORS 跨域支持）

由于前端和后端运行在不同的端口（前端默认 5173，后端 8080），需要配置 CORS（跨域资源共享）。

### 在后端添加 CORS 配置

在后端的 Spring Boot 项目中，需要添加 CORS 配置。有几种方式：

#### 方式 1：在 Controller 类上添加注解（推荐）

在 `ReportRestController.java` 类上添加：

```java
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reports")
public class ReportRestController {
    // ...
}
```

#### 方式 2：全局 CORS 配置

创建一个配置类 `CorsConfig.java`：

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
```

## 3. 启动步骤

### 步骤 1：启动后端

```bash
cd ../llm-report-service
./mvnw spring-boot:run
# 或者在 Windows 上：mvnw.cmd spring-boot:run
```

确保后端运行在 `http://localhost:8080`

### 步骤 2：启动前端

```bash
cd frontend
npm run dev
```

前端会运行在 `http://localhost:5173`（或 Vite 显示的端口）

## 4. 验证连接

### 方法 1：浏览器开发者工具

1. 打开前端页面 `http://localhost:5173`
2. 打开浏览器开发者工具（F12）
3. 切换到 Network（网络）标签
4. 提交一个报告
5. 查看是否有 API 请求：
   - `POST http://localhost:8080/api/reports`
   - `GET http://localhost:8080/api/reports/{taskId}`

### 方法 2：检查控制台错误

如果连接失败，浏览器控制台会显示错误信息：
- `CORS policy` 错误 → 后端未配置 CORS
- `Network Error` 或 `ERR_CONNECTION_REFUSED` → 后端未启动或端口不对
- `404 Not Found` → API 路径不正确

## 5. 常见问题

### 问题 1：CORS 错误

**错误信息**：
```
Access to XMLHttpRequest at 'http://localhost:8080/api/reports' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**解决方法**：按照上面的步骤 2 配置后端 CORS

### 问题 2：后端端口不是 8080

**解决方法**：
1. 查看后端 `application.properties` 中的 `server.port` 配置
2. 修改 `.env.local` 中的 `VITE_API_BASE_URL` 为正确的端口

### 问题 3：API 路径不匹配

**检查项**：
- 前端请求：`/api/reports`（在 `reports.ts` 中定义）
- 后端路径：`@RequestMapping("/api/reports")` 应该匹配

### 问题 4：前后端数据类型不匹配

**检查项**：
- 后端返回的 JSON 结构与 `ReportResponse` 类型定义是否匹配
- 字段名是否一致（如 `id`, `status`, `reportResult` 等）

## 6. 生产环境配置

在生产环境中：
1. 修改 `.env.production` 文件（而不是 `.env.local`）
2. 设置正确的后端 API 地址
3. 确保后端 CORS 配置允许生产环境的域名

