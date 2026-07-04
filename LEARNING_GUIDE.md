# 阿斯塔洛斯 Wiki 项目学习指南

## 🎯 学习目标

通过学习本项目，你将掌握：
- **Spring Boot 3.x** 后端开发核心知识
- **Vue 3** 前端开发最佳实践
- **前后端分离架构**的完整实现
- **RESTful API** 设计规范
- **JWT 认证**与权限控制
- **数据库设计**与 JPA 操作

---

## 📚 推荐学习路径

### 第一阶段：项目整体理解（1-2天）

#### 1. 项目结构概览
```
AstarosWiki/
├── AstarosWiki/                  # 后端项目
│   ├── src/main/java/            # Java 源码
│   │   └── com/example/astaroswiki/
│   │       ├── entity/           # 实体类（数据库表对应的类）
│   │       ├── repository/       # 数据访问层
│   │       ├── service/          # 业务逻辑层
│   │       ├── controller/       # 控制器层（API接口）
│   │       ├── dto/              # 数据传输对象
│   │       ├── security/         # 安全认证
│   │       ├── common/           # 公共组件
│   │       └── exception/        # 异常处理
│   └── src/main/resources/       # 配置文件
│       ├── application.yml       # 主配置文件
│       └── db/                   # 数据库脚本
│
├── astaros-wiki-frontend/        # 前端项目
│   ├── src/
│   │   ├── api/                  # API 请求封装
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── router/               # Vue Router 路由
│   │   ├── views/                # 页面组件
│   │   ├── assets/               # 静态资源（CSS等）
│   │   ├── App.vue               # 根组件
│   │   └── main.ts               # 入口文件
│   ├── vite.config.ts            # Vite 配置
│   └── package.json              # 项目依赖
│
└── db/                           # 数据库初始化脚本
    └── idea_init.sql             # 完整建表脚本
```

#### 2. 阅读顺序建议

**先理解整体架构**：
1. `AstarosWiki/pom.xml` - 了解项目依赖
2. `application.yml` - 了解配置结构
3. `idea_init.sql` - 了解数据库设计
4. `astaros-wiki-frontend/package.json` - 了解前端依赖

---

### 第二阶段：Spring Boot 后端学习（3-5天）

#### 📌 第1天：配置与实体类

**学习重点**：Spring Boot 基础配置、JPA 实体映射

**文件顺序**：
1. `pom.xml` - Maven 依赖管理
   - 学习：Spring Boot Starter、JPA、Security、JWT 等
   
2. `application.yml` - 配置文件
   - 学习：数据源配置、JPA 配置、服务器配置
   
3. `entity/User.java` - 用户实体类
   - 学习：@Entity、@Table、@Column、枚举类型
   
4. `entity/WikiPage.java` - 页面实体类
   - 学习：关联关系（@ManyToOne、@OneToMany）

#### 📌 第2天：数据访问层

**学习重点**：Spring Data JPA、Repository 接口

**文件顺序**：
1. `repository/UserRepository.java`
   - 学习：JpaRepository 基础方法、自定义查询方法
   
2. `repository/PageRepository.java`
   - 学习：@Query 自定义查询、分页查询

#### 📌 第3天：业务逻辑层

**学习重点**：Service 层设计、业务逻辑处理

**文件顺序**：
1. `service/AuthService.java` - 认证服务
   - 学习：用户注册、登录、JWT 生成
   
2. `service/PageService.java` - 页面服务
   - 学习：复杂业务逻辑、数据转换

#### 📌 第4天：控制器层

**学习重点**：RESTful API 设计、Controller 注解

**文件顺序**：
1. `controller/AuthController.java`
   - 学习：@RestController、@PostMapping、@RequestBody
   
2. `controller/PageController.java`
   - 学习：路径参数、查询参数、分页

#### 📌 第5天：安全与异常处理

**学习重点**：JWT 认证、Spring Security、全局异常处理

**文件顺序**：
1. `security/JwtUtils.java` - JWT 工具类
   - 学习：JWT 生成、验证、解析
   
2. `security/SecurityConfig.java` - 安全配置
   - 学习：过滤器链、路径权限控制
   
3. `exception/GlobalExceptionHandler.java`
   - 学习：统一异常处理、@ControllerAdvice

---

### 第三阶段：Vue 3 前端学习（3-5天）

#### 📌 第1天：项目配置与入口

**学习重点**：Vite、Vue 3 入口、项目结构

**文件顺序**：
1. `package.json` - 依赖管理
   - 学习：Vue 3、Vite、Element Plus、Pinia
   
2. `vite.config.ts` - Vite 配置
   - 学习：开发服务器、API 代理
   
3. `main.ts` - 应用入口
   - 学习：createApp、插件注册
   
4. `App.vue` - 根组件
   - 学习：组件结构、导航栏、全局布局

#### 📌 第2天：API 封装与请求

**学习重点**：Axios、API 模块化、请求拦截

**文件顺序**：
1. `api/request.ts` - Axios 配置
   - 学习：请求拦截器、响应拦截器、错误处理
   
2. `api/auth.ts` - 认证 API
   - 学习：登录、注册请求
   
3. `api/page.ts` - 页面 API
   - 学习：GET/POST/PUT/DELETE 请求

#### 📌 第3天：状态管理

**学习重点**：Pinia 状态管理、响应式数据

**文件顺序**：
1. `stores/user.ts` - 用户状态
   - 学习：defineStore、state、actions
   
2. `stores/page.ts` - 页面状态
   - 学习：异步 action、状态持久化

#### 📌 第4天：路由与页面

**学习重点**：Vue Router、路由守卫、页面组件

**文件顺序**：
1. `router/index.ts` - 路由配置
   - 学习：路由定义、导航守卫
   
2. `views/Home.vue` - 首页
   - 学习：组件生命周期、数据加载
   
3. `views/Login.vue` - 登录页
   - 学习：表单处理、事件处理

#### 📌 第5天：复杂页面与管理后台

**学习重点**：组件通信、Element Plus、管理后台

**文件顺序**：
1. `views/PageDetail.vue` - 页面详情
   - 学习：路由参数、Markdown 渲染
   
2. `views/admin/Dashboard.vue` - 管理仪表盘
   - 学习：表格、分页、数据管理
   
3. `views/admin/Users.vue` - 用户管理
   - 学习：复杂交互、状态切换

---

### 第四阶段：前后端联调（2-3天）

#### 学习重点：前后端交互、接口调试

**实践任务**：
1. 启动项目，观察前后端如何通信
2. 使用 Swagger 文档理解 API
3. 修改一个功能，观察前后端变化

---

## 🔧 关键知识点对照表

### Spring Boot 知识点

| 知识点 | 文件位置 | 学习要点 |
|--------|----------|----------|
| **依赖管理** | `pom.xml` | Spring Boot Starter 体系 |
| **配置文件** | `application.yml` | YAML 格式、配置注入 |
| **实体类** | `entity/*.java` | JPA 注解、关联关系 |
| **Repository** | `repository/*.java` | Spring Data JPA 查询 |
| **Service** | `service/*.java` | 业务逻辑、事务管理 |
| **Controller** | `controller/*.java` | RESTful API 设计 |
| **DTO** | `dto/*.java` | 数据传输对象设计 |
| **JWT** | `security/JwtUtils.java` | Token 生成与验证 |
| **Security** | `security/SecurityConfig.java` | 权限控制、过滤器 |
| **异常处理** | `exception/*.java` | 统一异常处理 |

### Vue 3 知识点

| 知识点 | 文件位置 | 学习要点 |
|--------|----------|----------|
| **项目配置** | `vite.config.ts` | Vite 配置、代理设置 |
| **入口文件** | `main.ts` | 应用初始化、插件注册 |
| **根组件** | `App.vue` | 全局布局、导航栏 |
| **API 封装** | `api/request.ts` | Axios 配置、拦截器 |
| **状态管理** | `stores/*.ts` | Pinia Store 定义 |
| **路由配置** | `router/index.ts` | 路由定义、导航守卫 |
| **页面组件** | `views/*.vue` | 组件结构、生命周期 |
| **样式** | `assets/*.css` | CSS 变量、主题设计 |

---

## 💡 学习建议

### Spring Boot 学习建议

1. **先理解注解**
   - `@Entity` = 数据库表
   - `@RestController` = API 接口
   - `@Service` = 业务逻辑
   - `@Autowired` = 自动注入

2. **理解分层架构**
   ```
   Controller → Service → Repository → Database
   (接收请求)  (处理业务)  (访问数据)   (存储数据)
   ```

3. **实践建议**
   - 尝试添加新的实体类
   - 尝试添加新的 API 接口
   - 尝试修改现有功能

### Vue 3 学习建议

1. **先理解组件结构**
   ```vue
   <template>  <!-- HTML 结构 -->
   <script>    <!-- JavaScript 逻辑 -->
   <style>     <!-- CSS 样式 -->
   ```

2. **理解响应式数据**
   - `ref()` - 单个值
   - `reactive()` - 对象
   - `computed()` - 计算属性

3. **实践建议**
   - 尝试修改页面样式
   - 尝试添加新的页面
   - 尝试修改 API 请求

---

## 📖 推荐学习资源

### Spring Boot
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)
- [Spring Security 文档](https://spring.io/projects/spring-security)

### Vue 3
- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [Element Plus 文档](https://element-plus.org/zh-CN/)

### 其他
- [RESTful API 设计指南](https://restfulapi.net/)
- [JWT 入门教程](https://jwt.io/introduction)

---

## 🚀 快速开始学习

### 第一步：运行项目
```bash
# 1. 启动数据库（执行 idea_init.sql）
# 2. 启动后端
cd AstarosWiki
mvn spring-boot:run

# 3. 启动前端
cd astaros-wiki-frontend
pnpm dev
```

### 第二步：观察项目运行
- 访问 http://localhost:3000
- 观察页面如何加载
- 检查浏览器开发者工具的网络请求

### 第三步：阅读关键文件
按照上述学习路径，逐个文件阅读理解

---

## ✅ 学习检查清单

### Spring Boot 知识检查
- [ ] 理解 Maven 依赖管理
- [ ] 理解 application.yml 配置
- [ ] 理解 JPA 实体类注解
- [ ] 理解 Repository 查询方法
- [ ] 理解 Service 业务逻辑
- [ ] 理解 Controller RESTful API
- [ ] 理解 JWT 认证流程
- [ ] 理解 Spring Security 配置

### Vue 3 知识检查
- [ ] 理解 Vite 项目配置
- [ ] 理解 Vue 3 组件结构
- [ ] 理解 Pinia 状态管理
- [ ] 琮解 Vue Router 路由
- [ ] 理解 Axios API 请求
- [ ] 理解 Element Plus 组件

### 架构知识检查
- [ ] 理解前后端分离架构
- [ ] 理解 RESTful API 设计
- [ ] 理解 JWT 认证机制
- [ ] 理解三层架构模式

---

祝你学习顺利！如有问题，可以随时查看代码注释或搜索相关文档。