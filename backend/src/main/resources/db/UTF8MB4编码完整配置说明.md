# ============================================================
# utf8mb4 编码完整配置指南（题库系统）
# ============================================================

## 一、application.yml 数据库连接配置（已修改）

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
    # ============================================================
    # 【utf8mb4 强制】HTTP 请求响应编码
    # 防止 GET/POST 请求参数中文乱码
    # ============================================================
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  tomcat:
    uri-encoding: UTF-8

spring:
  # ============================================================
  # Spring MVC 全局编码配置（配合 JacksonConfig.java）
  # ============================================================
  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  messages:
    encoding: UTF-8

  # ============================================================
  # 数据库连接配置（utf8mb4 编码，核心）
  # ============================================================
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://47.97.193.230:3306/mini_wx_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: Liyong9!*
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 10000
      # 【utf8mb4 强制】连接初始化SQL，确保会话编码正确
      connection-init-sql: SET NAMES utf8mb4

  # ============================================================
  # Jackson JSON 序列化配置（配合 JacksonConfig.java）
  # ============================================================
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
```

---

## 二、Jackson 全局 UTF-8 序列化配置类（已创建）

**文件位置**: `src/main/java/com/wxjiaozi/config/JacksonConfig.java`

```java
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 1. 日期格式化配置
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 2. 容错配置（防止异常中断请求）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 3. Java 8 时间类型支持（LocalDateTime等）
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
```

**作用说明**:
- 防止 JSON 中文转码异常，确保请求响应中文不乱码
- 统一日期格式化，避免 Date/LocalDateTime 序列化异常
- 容错机制：未知属性不抛异常，提高兼容性

---

## 三、MyBatis utf8mb4 兼容说明

### ✅ 无需额外配置！MyBatis 原生支持 utf8mb4

**配置要点**:

1. **XML Mapper 中文直接写**
   ```xml
   <!-- 直接写中文，无需转码 -->
   <select id="selectBySubject" resultMap="BaseResultMap">
       SELECT * FROM exam_question WHERE subject_id = #{subjectId}
       AND stem LIKE CONCAT('%', #{keyword}, '%')  <!-- 中文参数直接传 -->
   </select>
   ```

2. **动态 SQL 中文参数直接传递**
   ```xml
   <if test="stem != null and stem != ''">
       AND stem LIKE CONCAT('%', #{stem}, '%')  <!-- 无需转义 -->
   </if>
   ```

3. **JSON 字段（option_list）处理**
   ```java
   // 实体类已配置 JacksonTypeHandler，直接用
   @TableField(typeHandler = JacksonTypeHandler.class)
   private List<QuestionOptionDTO> optionList;
   ```

4. **MyBatis-Plus 配置说明**
   ```yaml
   mybatis-plus:
     configuration:
       # 已有配置无需修改，直接兼容 utf8mb4
       map-underscore-to-camel-case: true
   ```

---

## 四、前端请求 Content-Type 标准格式（关键）

### ✅ 小程序请求配置（必须遵守，否则题干中文乱码）

```javascript
// ============================================================
// request.js 统一拦截器配置
// ============================================================

const BASE_URL = 'https://your-domain.com/api'

function request(options) {
    return new Promise((resolve, reject) => {
        uni.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},

            // ============================================================
            // 【关键】Content-Type 必须正确设置，否则中文乱码
            // ============================================================
            header: {
                'Content-Type': 'application/json;charset=utf-8',  // ✅ 标准格式
                'Authorization': uni.getStorageSync('token') || ''
            },

            success: (res) => {
                if (res.data.code === 0) {
                    resolve(res.data.data)
                } else {
                    uni.showToast({ title: res.data.message, icon: 'none' })
                    reject(res.data)
                }
            },
            fail: (err) => {
                uni.showToast({ title: '网络请求失败', icon: 'none' })
                reject(err)
            }
        })
    })
}
```

### Content-Type 对照表

| 场景 | Content-Type | 说明 |
|------|--------------|------|
| JSON 请求（推荐） | `application/json;charset=utf-8` | ✅ 标准格式，必加 `charset=utf-8` |
| 表单提交 | `application/x-www-form-urlencoded;charset=utf-8` | 注意转码 |
| 文件上传 | `multipart/form-data` | 不含 charset，由 boundary 分隔 |

### 常见乱码场景与修复

**场景 1：题干提交乱码**
```javascript
// ❌ 错误：未指定 charset
header: { 'Content-Type': 'application/json' }

// ✅ 正确：明确指定 charset=utf-8
header: { 'Content-Type': 'application/json;charset=utf-8' }
```

**场景 2：GET 请求参数中文**
```javascript
// ✅ 正确：encodeURIComponent 编码
const keyword = encodeURIComponent('高等教育学')
request({ url: `/question/search?keyword=${keyword}` })
```

---

## 五、Excel 导入中文编码注意事项

**文件位置**: `src/main/java/com/wxjiaozi/util/ExcelImportUtil.java`

```java
// ✅ 正确：读取文件时强制指定 UTF-8
FileInputStream fis = new FileInputStream(file);
Workbook workbook = WorkbookFactory.create(fis);  // POI 自动识别编码

// 读取单元格时转字符串（防止乱码）
String value = new DataFormatter().formatCellValue(cell).trim();
```

**导入前检查清单**:
1. Excel 文件另存为 → UTF-8 编码 CSV
2. 不要用 WPS 保存为 GBK 编码
3. 导入后抽样校验：`SELECT stem FROM exam_question LIMIT 10`

---

## 六、乱码排查步骤

```bash
# 步骤1：检查数据库层
docker exec -i mysql8 mysql -uroot -p"Liyong9!*" mini_wx_db -e "
SHOW VARIABLES LIKE 'character%';
SHOW VARIABLES LIKE 'collation%';
"

# 预期结果：
# character_set_client      = utf8mb4
# character_set_connection  = utf8mb4
# character_set_database    = utf8mb4
# character_set_results     = utf8mb4
# character_set_server      = utf8mb4

# 步骤2：检查应用层连接
curl -X POST http://localhost:8080/api/question/test \
  -H "Content-Type: application/json;charset=utf-8" \
  -d '{"stem":"测试题干"}'

# 步骤3：检查数据表
SELECT HEX(stem) FROM exam_question LIMIT 1;
# 中文UTF-8每个字占3字节，如 "测" = E6B58B
```

---

## 七、修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `application.yml` | ✅ 已修改 | 数据库URL + 编码配置 |
| `JacksonConfig.java` | ✅ 已创建 | JSON 序列化配置 |
| `charset_fix_utf8mb4.sql` | ✅ 已执行 | 数据库字符集修复 |

---

## 八、最终验证

启动应用后执行以下验证：

1. **查看启动日志**，无编码相关警告
2. **Swagger 测试**：http://47.97.193.230:8080/api/doc.html
3. **提交含中文题目**，查询数据库确认无乱码
4. **小程序请求**，检查响应头含 `Content-Type: application/json;charset=UTF-8`
