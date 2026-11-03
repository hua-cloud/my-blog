# R2 文件上传与 Nginx 稳定资源路径设计

- 日期：2026-11-03
- 状态：需求与方案已确认；详细设计已通过独立 builder check，待用户审阅，尚未实施。
- 范围：现有 Java 项目接入 Cloudflare R2，只新增一个文件上传接口；交付配套 Nginx 部署配置说明。
- 本文优先于早期 R2 调研报告以及既有领域设计中 `/files/{id}` 读取入口的建议。此次选择 `/file/<对象键后缀>` 直接代理，不增加 Java 文件读取接口。

## 1. 目的与已确认需求

用户希望先将 R2 作为核心文件存储来源，完成一次上传即可使用的最小闭环：后端上传对象并写入 `blog_file`，前端不用另发一次元数据保存请求。

已确认的要求：

1. 上传仅需选择文件，无需手动填写当前表中的其他字段。
2. 一次接口调用完成 R2 上传和数据库记录写入，二者都成功后才返回成功。
3. 首版预留文件类型限制的扩展位置，但不因扩展名或 MIME 类型拒绝文件。
4. 对外返回 `/file/...` 形式的站点根路径；文章以后可以保存该路径，避免重复存储上游域名。
5. Nginx 将该路径代理到 R2 公开资源域名，图片字节经过 Nginx 返回浏览器，无 Java 查库或签名重定向。
6. 将来替换资源域名时调整 Nginx 上游；存储迁移时仍需复制对象并保持 key 不变。
7. 草稿图片权限、文章模块和正文处理本轮不考虑。

用户当前没有绑定 R2 公开资源域名。真实上传只需要 S3 endpoint 和凭据；通过 `/file/` 预览还需要可公开读取的上游及前端入口代理，这两项应独立验收。

## 2. 范围与约束

### 2.1 本次实现目标

- AWS SDK for Java v2 连接 R2，配置化端点、凭据、桶名。
- 登录保护的单文件 multipart 上传接口。
- 文件大小、非空和元数据长度检查；类型策略扩展点。
- R2 对象存储、MySQL 元数据持久化、失败补偿。
- 返回 fileId、objectKey、稳定 url、原名、MIME、字节数。
- 一份含路径规则、Host、SNI 和验证方法的 Nginx 配置样例。

### 2.2 不包含的能力

- Java 文件读取、下载、302、签名 URL 接口。
- 文件列表、图片素材库、删除管理、文章引用检查。
- Markdown 编辑器、文章新增/编辑、草稿可见性或正文替换。
- 浏览器直传、分片、断点续传、批量上传、去重、压缩、转码。
- 自动可靠清理、分布式事务、新增文件微服务或 Maven 模块。
- 本轮只写设计，不安装依赖、修改业务代码、部署 Nginx 或操作云资源。

### 2.3 已核实的工程约束

- Spring Boot 3.4.3、Java 17、MyBatis-Plus、Sa-Token。
- `app` 装配，`trigger` HTTP，`domain` 用例和模型，`infrastructure` 存储与持久化。
- `application.yml` 已开启 multipart，单文件上限 5MB、请求上限 10MB，首版沿用。按 Spring Boot DataSize 对应单文件 5 × 1024 × 1024 字节。
- 当前所有路由除 `/auth/login` 外都要求登录，上传继续沿用该策略。
- `blog_file` 已存在定义；尚无文件业务实现，不需要迁移已有 Local 文件实现。
- 文件 key 列上限 128 字符，原名 255、MIME 128、桶名 64。
- `AuditMetaObjectHandler` 对 BasePo 的插入填写创建/修改人和时间，可供文件 PO 复用。
- 根 POM 没有将 `my-blog-api` 纳入 modules；本次不将 DTO 放到该未构建目录。

## 3. 已比较的方案与选择

### 3.1 上传方式

| 方案 | 特点 | 决策 |
| --- | --- | --- |
| 后端接收 multipart 并上传 R2 | 容易集中校验、写元数据和补偿；占用后端上传带宽 | 选用，满足单接口与当前文件大小约束 |
| 后端签名、浏览器直传 R2 | 降低后端上传流量；需要完成确认、会话绑定、CORS 等 | 留待后续，不在首版增加接口 |

### 3.2 文件访问方式

| 方案 | 收益与代价 | 决策 |
| --- | --- | --- |
| 正文保存资源域名完整 URL | 浏览器直接使用 CDN；上游域名进入内容 | 用户不选用 |
| `/files/{id}` 查库后签名并 302 | 可按 ID 解析位置；每次访问引入应用与临时授权 | 用户不选用，本轮无访问授权需求 |
| `/file/...` 由 Nginx 直接代理公开 R2 域名 | 内容只保存路径，域名由代理配置管理；图片流量经过站点服务器 | 用户明确选用 |

S3 写入端点与公开读取上游分开：Java 上传访问 R2 S3 endpoint，Nginx 读取访问公开自定义域名。R2 S3 endpoint 不能仅靠普通无签名反代就公开读取私有对象。[R2 公开 Bucket 文档](https://developers.cloudflare.com/r2/buckets/public-buckets/)

## 4. 资源身份与路径契约

首版固定以下关系，避免 Nginx 与后端各自添加一次前缀：

| 项目 | 示例 | 规则 |
| --- | --- | --- |
| R2 object key | `file/2026/11/03/18a4d21c315249218fe455f3d978d213.png` | 无前导 `/`，包含 `file/` 前缀 |
| `blog_file.storage_key` | 同上 | 保存完整桶内 key，不包含桶名或域名 |
| 返回 `objectKey` | 同上 | 与数据库字段含义一致 |
| 返回 `url` | `/file/2026/11/03/18a4d21c315249218fe455f3d978d213.png` | `"/" + objectKey`，不再重复拼 `/file/` |
| Nginx 上游 URL | `https://assets.example.com/file/2026/11/03/18a4d21c315249218fe455f3d978d213.png` | 公开域名加原请求路径，不剥离前缀 |

`url` 是从站点根开始的路径，不是相对当前文章目录的路径，也不是按数据库 ID 查找的地址。任意前端页面渲染它时，其所在站点都需要实现相同的 `/file/` 代理契约。

对象 key 由服务端生成：`file/yyyy/MM/dd/<无连字符UUID>[.<安全扩展名>]`。日期取服务端配置时区，采用注入 Clock 便于测试；生产与开发按 Asia/Shanghai 对齐。扩展名只取最后一个后缀，限定为不超过 16 位 ASCII 字母/数字并转小写，不合规则省略。**这只是生成安全名称，不是文件类型允许列表，不影响文件上传是否接受。**

原始名称与对象 key 分离。去除客户端路径，只保留文件名，去除控制字符，清理后为空或超过 255 字符返回参数错误，不静默截断。文件字节不因改名而变化。

生成 key 后检查长度与前缀。唯一性采用随机 UUID；对象写入使用 `If-None-Match: *` 防止覆盖已有对象。条件冲突视为 key 冲突，生成新 key 后最多重试 2 次。补偿只删除本次确认创建的 key，不能删除碰撞到的既有对象。[R2 条件写兼容说明](https://developers.cloudflare.com/r2/api/s3/api/)

## 5. 上传接口契约

### 5.1 请求

- 方法：`POST /admin/files/upload`
- Content-Type：`multipart/form-data`
- 认证：沿用 `Authorization: Bearer <token>`。
- 参数：名为 `file` 的单个文件 part，不接受批量列表。若重复提交多个 `file` part，应明确拒绝，不静默取第一个。
- 无业务表单字段；客户端不传 storageType、bucket、key、大小、审计人或时间来决定落库值。
- 不额外提供“保存文件记录”的接口。

### 5.2 成功响应

沿用项目 `R<T>`，业务成功码为当前的 1000：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "fileId": 123,
    "objectKey": "file/2026/11/03/18a4d21c315249218fe455f3d978d213.png",
    "url": "/file/2026/11/03/18a4d21c315249218fe455f3d978d213.png",
    "originalName": "示意图.png",
    "mimeType": "image/png",
    "fileSize": 102400
  }
}
```

仅在元数据事务提交后构造成功响应；fileId 必须是数据库生成的主键，不是 insert 的受影响行数。域名、R2 凭据、桶名、临时签名和过期时间不进入响应。`url` 保持不含签名；对象或代理不可用时仍可能加载失败，接口不承诺“永久在线”。

当前调用方对 Long 主键的处理沿用项目既有约定；后续若进入 JavaScript 安全整数之外，统一规划 ID 序列化，不在此模块单独更改协议。

### 5.3 校验与错误

| 场景 | 对外行为 | 是否产生对象/记录 |
| --- | --- | --- |
| 未登录、Token 失效 | 现有 UNAUTHORIZED（业务码 401） | 无 |
| 缺少 part、重复文件、空文件、文件名不合规 | BAD_REQUEST（业务码 400）及清楚提示 | 无 |
| 超过 5MB 或 multipart 请求超过 10MB | BAD_REQUEST 及大小提示 | 不进入存储流程 |
| 任意扩展名、任意 MIME、不识别内容 | 首版允许，未知 MIME 兜底 | 正常上传 |
| R2 失败、超时或连接异常 | 新增 FILE_UPLOAD_FAILED（建议业务码 5001） | 不写元数据，远端不明结果需记录 |
| 数据库插入或提交失败 | FILE_UPLOAD_FAILED | 回滚元数据并尝试补偿对象 |
| 补偿失败 | 保持原上传失败结果，记录清理线索 | 可能留对象，不伪报成功 |

本次继续沿用项目以响应体 `code` 表达业务错误的风格，不承诺每种业务码均对应实际 HTTP 状态。multipart 解析异常需专门映射（例如大小超限、缺少请求 part），避免落到通用 500。代理层直接拒绝的请求可能是 HTTP 413，此类请求尚未到 Java，不能保证返回统一 `R`。

客户端重试不是幂等协议：重复请求允许产生新文件和新记录。内容去重、幂等键不在范围内。

## 6. 文件类型锚点与内容信息

新增一个简单 `FileTypePolicy`（或同职责接口），由上传用例在校验阶段调用。首版注入 `AllowAllFileTypePolicy`，方法不因文件类型抛拒绝异常；后续允许替换为配置驱动的限制实现。

不增加空置的类型配置项、无用规则引擎或多层策略工厂。文件非空、大小、文件名和 key 长度检查独立于该策略，仍正常生效。

MIME 用于描述和 HTTP 响应，不作为首版上传准入条件。对内容做轻量探测；可靠识别时写入识别值，未知或探测失败时统一使用 `application/octet-stream`，不直接把未经确认的客户端 MIME 当作可信结果。探测过程不能丢失流头部，实际上传字节必须与原文件一致。探测组件选择与具体库留给实现者，但不为此引入完整文档处理框架。

任意类型均可存储，并不保证任意文件都能通过 `<img>` 预览；前端应仅对浏览器可显示的图片使用图片组件。

## 7. 模块与对象职责

采用现有业务包风格，在 domain 增加 `file`，不重构现有分类和认证代码。

| 模块 | 内容（建议名） | 职责 |
| --- | --- | --- |
| app | `R2StorageProperties`、`R2StorageConfig` | 绑定并校验配置，装配单例客户端与短事务执行器 |
| trigger | `BlogFileController` | 认证后的单文件 multipart 适配，转换普通 Java 上传源，返回 `R<FileUploadResponseDTO>` |
| domain.file.service | `FileService` | 校验、策略调用、key 生成、上传与事务编排、补偿 |
| domain.file.model | `FileUploadSource`、`FileAsset`、`StorageLocation`、响应 DTO | 文件输入、持久化数据与输出结果 |
| domain.file.adapter.port | `IFileStoragePort`、类型策略 | 存储与失败补偿所需的窄接口 |
| domain.file.adapter.repository | `IFileRepository` | 插入元数据并返回生成 ID |
| infrastructure.adapter.port.file | `R2FileStorageAdapter` | SDK PutObject、条件写和补偿 DeleteObject、异常映射 |
| infrastructure.adapter.repository | `BlogFileRepository` | Entity/PO 转换、调用 Mapper、返回生成主键 |
| infrastructure.persistence | `BlogFile extends BasePo`、`IBlogFileMapper` | 对齐现有表与审计填充 |
| types | 文件上传失败业务状态 | 接入统一错误响应 |

端口仅提供当前必要能力：上传（key、已知长度、MIME、可重开输入流源）与按定位删除。删除方法供补偿使用，不意味着提供删除 HTTP 接口。不加入读取 URL 解析或 S3Presigner。

上传源通过普通 Java 数据表达，不将 MultipartFile、Servlet、Sa-Token、MyBatis PO 或 AWS SDK 类型放进领域模型。源在请求结束前有效，每次打开返回从头开始的新流；上传用例/存储适配器明确用 try-with-resources 关闭自己打开的流，不跨请求保存它。可重开源也支持 SDK 重试；实际实现应提供可重新读取的 body 或关闭无法重放时的自动重试，不能重用已经消耗的流。

Maven：根 POM 引入 AWS SDK BOM 固定明确 2.x 版本，基础设施引入 `software.amazon.awssdk:s3`；app 直接使用 SDK 配置类型时显式声明对应依赖。domain 需要 `spring-tx` 支持短事务；版本交由现有 Spring Boot 依赖管理。具体 AWS SDK 版本由实施时选定、锁定，并以真实 R2 冒烟验证，不以旧调研示例冒充已验证版本。

## 8. 上传顺序与事务边界

1. Controller 校验只有一个 file part，将请求中的 MultipartFile 转换成请求期可重开的上传源。
2. 用例校验非空、5MB、名称和长度，取得大小、探测 MIME，执行放行型类型策略。
3. 生成 key；构造 `storageType=R2`、配置桶名、key 的定位信息。
4. 在数据库事务外执行 R2 上传，明确成功创建对象后继续。
5. 使用 `TransactionTemplate.execute(...)` 开启短事务插入元数据，并取回自增 ID。execute 正常返回意味着提交完成。
6. 提交成功后生成响应；稳定路径直接由 key 构造，无网络请求。
7. 若第 5 步插入或提交失败，在 execute 外 catch，尝试删除本次对象，然后向外抛原上传失败。

不要给整个上传入口加覆盖网络调用的 `@Transactional`。要捕获事务“提交失败”，不能只在注解方法内部 catch insert。TransactionTemplate 可由 app 装配注入 domain，参与现有 DataSource 事务管理。

| 异常位置 | 一致性结果与处理 |
| --- | --- |
| 校验失败 | 无远端对象，无记录 |
| R2 明确拒绝且无创建成功 | 不登记元数据，不删除其他既有 key |
| R2 超时，创建结果未知 | 不登记；记录本次 key、阶段和错误以供核查，不能声称远端一定未写入 |
| 插入失败或事务明确回滚 | 删除已确认上传对象；删除失败留下待清理对象 |
| 数据库提交确认丢失 | 提交结果可能不明；先通过 key 查询确认是否已存在，确认已提交则保留对象，确认无记录才补偿；数据库仍不可用时保留对象并记录待核查，不能盲删 |
| 已提交但响应传输失败 | 保留对象与记录；重试可能新增另一文件 |

事务结果不明时宁可保留待核查对象，也不主动制造已提交记录指向缺失文件。实现者需将“确定回滚”和“提交结果未知”的异常处理分开，核查查询使用独立连接/新事务；判断不了时按未知处理。

补偿删除失败不得覆盖原异常。结构化日志包含阶段、storageType、bucket、key 和可关联请求信息，不输出凭据、Authorization、完整文件内容或签名。本次允许人工核查残留，不能宣称自动可靠清理或 R2/MySQL 原子事务。

## 9. 表字段映射

复用 `blog_file`，不修改表结构，不执行含 DROP 的 `sql/init.sql`：

| 列 | 写入来源 |
| --- | --- |
| id | MySQL AUTO_INCREMENT，Mapper 回填后返回 |
| original_name | 经路径与控制字符清理、长度检查的上传文件名 |
| storage_type | 显式 R2，不能依赖当前 LOCAL 默认值 |
| bucket_name | 配置的目标 Bucket |
| storage_key | 完整 object key，无前导斜杠 |
| mime_type | 探测结果或 application/octet-stream |
| file_size | 已校验的实际 multipart 文件字节数 |
| create_by / update_by | 现有审计填充器读取登录用户 |
| create_time / update_time | 现有审计填充器生成当前时间 |

首版不新增 url 列。`url` 可以由 key 推导，资源域名归代理配置管理。文件表无存储定位唯一索引，本次使用条件对象写入及随机 key；未来加唯一索引应独立增量迁移。

## 10. R2 配置与客户端

建议配置契约（示意，尚未写入运行配置）：

```yaml
file:
  storage:
    r2:
      endpoint: ${R2_ENDPOINT}
      access-key-id: ${R2_ACCESS_KEY_ID}
      secret-access-key: ${R2_SECRET_ACCESS_KEY}
      bucket: ${R2_BUCKET}
```

- 凭据使用 R2 提供的 Access Key ID / Secret Access Key，限定目标 Bucket 对象读写权限。
- endpoint 复制控制台值，普通桶通常是 `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`，有区域管辖限制时使用其对应端点。
- 必填值缺失或非法时启动失败并明确指出配置名；不提供虚假默认凭据，不在 Bean 初始化时联网列桶或写测试对象。
- region 固定 `auto`，path-style 开启，`chunkedEncodingEnabled(false)`；单例 S3Client 由容器关闭。
- 客户端配置连接超时 5 秒、单次尝试 30 秒、总 API 调用 60 秒。首版关闭 PutObject 的 SDK 自动网络重试（仅 1 次尝试），避免条件创建在成功响应丢失后重试变成 412 而误判为旧 key 冲突；明确 412 的 key 冲突重试按第 4 节由用例单独完成。补偿 DeleteObject 可保留有限重试。这些是设计默认值，实施时在具体 SDK/HTTP client 上落实。
- 已知长度上传；MIME 和对象内容保持一致，不设置 public-read ACL。
- 若锁定的 SDK 与 R2 联调发现默认 checksum/trailer 不兼容，结合官方兼容表在 R2 客户端局部调整 checksum 设置，并记录所选版本与验证结果，不全局关闭所有 SDK 校验。

公开资源域名仅由 Nginx 管理，Java 不需要 public-base-url 或 presign-ttl 配置。[R2 Java 示例](https://developers.cloudflare.com/r2/examples/aws/aws-sdk-java/)、[R2 凭据](https://developers.cloudflare.com/r2/api/tokens/)

## 11. Nginx 部署契约

### 11.1 生产资源代理示例

以下片段放入站点已有 server 配置。`assets.example.com` 替换为实际 R2 公开资源域名；CA bundle 路径按部署系统调整。本次不直接修改服务器配置。

```nginx
location ^~ /file/ {
    # 不带 URI 和尾部 /，保留完整 /file/... 路径。
    proxy_pass https://assets.example.com;
    proxy_http_version 1.1;
    proxy_set_header Host assets.example.com;
    proxy_set_header Connection "";
    proxy_set_header Authorization "";
    proxy_set_header Cookie "";

    proxy_ssl_server_name on;
    proxy_ssl_name assets.example.com;
    proxy_ssl_verify on;
    proxy_ssl_trusted_certificate /etc/ssl/certs/ca-certificates.crt;

    proxy_connect_timeout 5s;
    proxy_read_timeout 30s;
    limit_except GET { deny all; }  # GET 同时允许 HEAD。

    add_header X-Content-Type-Options nosniff always;
    add_header Content-Security-Policy "sandbox; default-src 'none'" always;
}
```

不附加 `/` 的 proxy_pass 保留完整 key 路径。配置不得将未知文件转入 SPA 的 index.html，也不应把文件域名指回当前站点形成循环。固定上游域名部署后需验证 DNS 解析；DNS 记录变更后按运维流程重新解析/重载，动态解析策略留给实际 Nginx 版本与环境决定。[Nginx proxy_pass / HTTPS 配置](https://nginx.org/en/docs/http/ngx_http_proxy_module.html)

这里的读代理不携带后端上传密钥。它只能读取已经公开的对象；公开域名自身也可以直接访问，因此该方案不以 Nginx 隐藏上游作为权限保证。

首版任意类型均可上传，同源公开文件必须同时考虑 HTML/SVG 等可执行内容。以上独立文件 location 设置 nosniff 和 CSP sandbox，抑制文件作为文档打开时取得博客同源脚本能力；普通图片仍可经 img 加载。未知 MIME 为 octet-stream，后续可增加下载 disposition，但不以此拒绝上传。实施验收必须覆盖 HTML/SVG 的响应头，确保错误页或代理规则未覆盖它们。已有 server 安全头的继承应在部署时核查，不因 location 的 add_header 无意丢失必要头。

### 11.2 上传代理与请求大小

`/admin/files/upload` 仍转发 Java 后端。部署者在现有 API location/server 作用域设置 `client_max_body_size 10m`，读取超时至少覆盖后端 60 秒调用及事务时间（建议 90 秒）。Java 再执行单文件 5MB 检查。此请求包含 Authorization；只对公开 `/file/` 读代理清空该头。

### 11.3 开发与上线依赖

- 开发可用显式开启的 R2 `r2.dev` 公开开发地址作为上游，替换 proxy_pass、Host、SNI 三处；它仅用于测试，不作为生产承诺。
- 前端开发服务器在其浏览器 Origin 下将 `/file/` 代理到同一公开上游或本地 Nginx。仅直接调用 Java 8080 时没有 `/file/` 读取路由，不能期望该路径自动可用。
- 正式上线前创建公开资源域名并绑定 Bucket。Cloudflare 接入应使用 Bucket 自定义域名功能，单独建一个指向 S3 endpoint 的 DNS 记录不等同于完成公开访问。
- 同源资源请求和后端到 R2 上传不需要浏览器 R2 CORS。若前后端上传不同源，单独处理应用 API 的 CORS，不因此启用前端直传。

### 11.4 缓存、成本与迁移边界

- 图片字节经过 Nginx，占用站点出站带宽；Cloudflare 上游缓存不会免除这一段流量。
- 首版保留上游缓存响应头，不强制长期缓存所有类型，不增加未经验证的 proxy_cache。后续按真实需要增加缓存；404 不长期缓存。
- 资源 key 不原地更新，替换图片生成新 key，为后续长缓存留条件。
- 改上游域名且仍指向相同 key 空间时可只改 Nginx。切换 Bucket/供应商时还需要迁移对象、校验路径与响应内容，不能仅改域名。
- 未来多客户端、邮件或站外展示，需要在展示边界补齐站点 Origin；这不改变当前正文保存站点根路径的选择。

## 12. 验收标准

### 12.1 自动化验证

1. 登录后的单文件上传调用存储并生成一条正确元数据，返回真实主键与精确的 `/` + key 路径。
2. storage_type 显式 R2，Bucket 正确，审计人与时间由服务端填写；不信任客户端伪造值。
3. 缺 file、重复 file、零字节、超限与名称不合规被拒绝，存储和数据库无写入。
4. PNG、未知后缀、无扩展名及未知内容均可上传；不识别 MIME 兜底，类型锚点实际被调用，首版策略无类型拒绝。
5. key 安全、短于 128、无路径穿越，扩展名清理不改变文件内容；类型探测后上传内容未丢头。
6. R2 明确失败不写库；条件冲突不覆盖旧对象；碰撞重试只消费本次可重开的输入源。
7. 数据库插入失败、确定提交失败触发正确补偿；补偿失败保留原异常并记录清理位置。
8. 提交结果未知时保留已提交对象或留待核查，不盲删；响应失败不会回删已提交文件。
9. 新增异常映射符合 `R` 约定，错误响应不泄露密钥；S3Client 生命周期与配置校验正确。

### 12.2 真实环境验收（实施后执行，本次尚未执行）

1. 用独立测试 Bucket 和测试 key 验证真实 R2 Put、Head/Get 内容一致、补偿 Delete，不动现有业务对象。
2. 通过上传接口上传图片，核查一条真实 blog_file 记录和 R2 对象；重启后记录与对象仍存在。
3. 对实际配置运行 `nginx -t`，重载后访问返回的 `/file/...`，确认 HTTP 200、图片内容与原文件一致、Host/SNI 正确，浏览器没有临时签名跳转。
4. 深层文章路径下访问根路径图片仍正确；缺失对象返回缺失结果，不能出现 SPA HTML 冒充图片。
5. 通过错误凭据、错误 endpoint、数据库失败、超限场景验证错误与补偿边界。
6. 验证文件响应安全头，HTML/SVG 直接打开不执行博客同源脚本；管理 API Authorization 正常转发，公开上游不接收站点 Cookie/Authorization。
7. 在测试环境切换上游为持有同 key 对象的另一个地址，正文和上传返回 url 不变；记录开发上游与生产上游差异。

仅 Mock 测试不能证明 R2 协议、权限或 Nginx 联通。生产域名未绑定时，可完成 Java 接入验证，稳定路径的生产读取验收仍有明确部署依赖。

## 13. 助手补全的默认决定与实现者余量

用户明确确认了第 1 节的范围与 `/file/` 代理方向。以下为使方案可实施而提出的默认决定，不表示用户逐项指定：

- 接口命名 `/admin/files/upload`，只接收一个名为 file 的 part。
- 沿用 5MB/10MB 和现有业务响应码风格。
- key 自身包含 `file/`，url 为前导斜杠加 key，使用日期和 UUID。
- 未知 MIME 使用 octet-stream；类型策略首版放行。
- 使用 TransactionTemplate 短事务，结果不明时优先保留对象并核查。
- 使用条件上传避免覆盖、保留人工清理边界，不建立自动清理服务。
- Nginx 固定公开上游，保留路径，处理同源任意文件类型响应。

具体 Java 类名、小包布局、SDK 的精确锁定版本、MIME 探测工具、HTTP client 实现以及部署系统 CA/DNS 配置留给实现者。不得借此扩大到文章模块、增加上传类型允许列表、增加读取接口或将域名写进返回路径。

## 14. 文档状态与后续

本文件已做独立 builder check，审阅结论为 `nothing important left`。用户批准详细设计后才进入实施计划；本次交付不包含业务实现、依赖安装或真实云联调。
