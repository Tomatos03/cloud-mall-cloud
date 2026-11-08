# 个人中心待补齐能力设计

> 状态：已按本设计落地首期实现（2026-11-08）。抽奖等可选项未启用；测试数据清理与提交动作不在范围内。
>
> 对应待办：
> 1. 支付密码、实名认证：已实现。
> 2. 积分/优惠券缺失业务接口：已实现签到、任务、兑换、券范围与支付积分。
> 3. 地址省市区：已切换为后端接口。
> 4. 测试数据清理、提交动作：本次不处理。

---

## 1. 背景与现状

个人中心 8 个子页面已完成基础数据对接，但仍有四类能力不完整：

| 能力 | 当前状态 | 缺口 |
|---|---|---|
| 支付密码 | 页面展示「暂未开放」 | 无数据表、无设置/校验接口 |
| 实名认证 | 页面展示「暂未开放」 | 无数据表、无提交/审核/查询接口 |
| 积分权益 | 余额、流水已接通 | 签到、任务、兑换、抽奖无业务接口 |
| 优惠券使用 | 列表已接通 | 「去使用」没有结算选券闭环 |
| 地址省市区 | 前端 `china-region-data` 本地数据 | 无后端区域接口 |

本设计将前三项按「先设计、后执行」处理；测试数据与提交动作不在范围内。

---

## 2. 总体范围与分期

### Phase 1：账户安全（设计确认后执行）

- 新增 `cm_user_security`。
- 支付密码设置、修改、校验。
- 实名认证提交、状态查询、后台审核/外部核验适配。
- 个人中心「安全设置」接入真实状态。
- 支付弹窗在创建支付单前校验支付密码（可配置开关）。

### Phase 2：积分与优惠券闭环

- 在现有 `cm_user_point` / `cm_user_point_log` 上补签到、任务、兑换。
- 积分变动统一收口到 `UserPointService.change`，保证余额与流水一致。
- 优惠券「去使用」增加适用范围查询与结算页预选。
- 抽奖作为可选项，默认不纳入首期。

### Phase 3：地址区域后端化

- 新增 `cm_region` 及区域查询接口。
- 前端 `fetchRegionChildren` 改为真实请求，移除 mock。
- 地址保存补充省/市/区代码，名称继续作为快照。

---

## 3. 账户安全设计

### 3.1 数据模型

新增 `cm_user_security`，与 `cm_user` 一对一：

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `user_id` | bigint | PK | 用户 ID |
| `pay_password_hash` | varchar(255) | NULL | 支付密码 BCrypt 哈希 |
| `pay_password_set_time` | datetime | NULL | 设置/最近修改时间 |
| `real_name` | varchar(64) | NULL | 真实姓名密文（AES-GCM） |
| `real_name_mask` | varchar(32) | NULL | 真实姓名脱敏展示值，如 `张*` |
| `id_card_no` | varchar(255) | NULL | 身份证号密文（AES-GCM） |
| `id_card_mask` | varchar(32) | NULL | 身份证号脱敏展示值，如 `110***********1234` |
| `real_name_status` | varchar(20) | NOT NULL DEFAULT 'UNVERIFIED' | `UNVERIFIED` / `PENDING` / `VERIFIED` / `REJECTED` |
| `real_name_submit_time` | datetime | NULL | 提交时间 |
| `real_name_verify_time` | datetime | NULL | 核验完成时间 |
| `real_name_reject_reason` | varchar(255) | NULL | 驳回原因 |
| `create_time` | datetime | NOT NULL | 创建时间 |
| `update_time` | datetime | NOT NULL | 更新时间 |

索引：

- `PRIMARY KEY (user_id)`
- `KEY idx_real_name_status (real_name_status)`

约束与说明：

- 支付密码固定 6 位数字，禁止明文落库、禁止日志输出。
- `real_name`、`id_card_no` 必须加密存储；`real_name_mask`、`id_card_mask` 仅用于展示，不参与身份校验。
- 两个脱敏字段在实名提交时与密文同事务写入：姓名保留首字符、其余替换为 `*`（如 `张三` → `张*`，`欧阳娜娜` → `欧***`）；身份证号保留前 3 位与后 4 位，中间替换为 `*`。
- 查询详情、列表、审核列表默认只读取脱敏字段，不为了展示解密姓名/身份证；只有实名审核、内部核验等必要场景才解密。
- 历史数据或迁移数据若脱敏字段为空，需在迁移脚本中解密回填一次，之后统一按写入时生成。
- 一个用户只有一条安全记录；首次访问时懒初始化。
- 实名认证状态变更必须记录审计日志（可先复用应用日志，后续补 `cm_user_security_log`）。

### 3.2 接口设计

统一挂在 `/users/me` 下，复用网关既有 `/users/**` 路由。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/users/me/security` | 查询支付密码是否设置、实名状态、脱敏姓名/身份证 |
| PUT | `/users/me/payment-password` | 设置或修改支付密码 |
| POST | `/users/me/payment-password/verify` | 校验支付密码 |
| POST | `/users/me/real-name` | 提交实名认证 |
| GET | `/users/me/real-name` | 查询实名认证状态 |

#### 查询安全信息

`GET /users/me/security`

响应：

```json
{
  "payPasswordSet": true,
  "realNameStatus": "VERIFIED",
  "realNameMasked": "张*",
  "idCardMasked": "110***********1234"
}
```

说明：`realNameMasked` / `idCardMasked` 分别直接取自 `real_name_mask` / `id_card_mask`，普通查询不执行解密。

#### 设置/修改支付密码

`PUT /users/me/payment-password`

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

规则：

- 首次设置时 `oldPassword` 可为空。
- 已设置时必须校验旧密码。
- 新密码必须为 6 位数字，且不能与旧密码相同。
- 更新后清除该用户支付密码校验失败计数。

#### 校验支付密码

`POST /users/me/payment-password/verify`

```json
{ "password": "123456" }
```

响应：`Result<Boolean>`。

安全要求：

- Redis 记录失败次数：同一用户 10 分钟内最多 5 次，超限返回 `PAY_PASSWORD_RETRY_LIMIT`。
- 校验成功不返回密码哈希、不写敏感日志。
- 支付密码与登录密码独立，互不复用。

#### 提交实名认证

`POST /users/me/real-name`

```json
{
  "realName": "张三",
  "idCardNo": "110101199001011234"
}
```

规则：

- 姓名 2-50 字符；身份证号按 18 位校验规则校验。
- 提交时同一事务写入 `real_name`、`real_name_mask`、`id_card_no`、`id_card_mask`；接口返回和后续查询只读取脱敏字段。
- 提交后状态置为 `PENDING`，禁止重复提交；`VERIFIED` 后不可修改。
- 首期采用「提交后待审核」模式，由运营后台/内部接口审核；接入第三方实名核验时替换审核实现，接口不变。
- 身份证号加密密钥从配置读取，不进入代码仓库。
- 审核通过/驳回时只更新状态与原因，不重新生成脱敏值。

#### 查询实名认证

`GET /users/me/real-name`

响应：

```json
{
  "status": "VERIFIED",
  "realNameMasked": "张*",
  "idCardMasked": "110***********1234",
  "rejectReason": null,
  "submitTime": "2026-11-08T21:00:00",
  "verifyTime": "2026-11-08T21:05:00"
}
```

### 3.3 支付密码前置校验

现有订单支付流程：

`OrderManager -> PaymentModal -> POST /orders/payment/{orderNo}`

调整后：

1. 若用户已设置支付密码，`PaymentModal` 打开后先展示支付密码输入步骤。
2. 调用 `POST /users/me/payment-password/verify`。
3. 校验通过后再创建支付单。
4. 未设置支付密码时提示「请先在账户设置中设置支付密码」，不阻止浏览订单，但阻止支付。
5. 是否强制启用由配置 `cloudmall.payment.require-pay-password` 控制，默认关闭，联调后再开启。

### 3.4 前端改造

| 文件 | 改动 |
|---|---|
| `PersonalInfo.vue` | 用 `/users/me/security` 替换「暂未开放」；新增设置/修改支付密码、实名认证提交/查看弹窗 |
| `PaymentModal.vue` | 增加支付密码步骤及校验调用 |
| `domain/user/api/index.ts` | 增加安全信息、支付密码、实名认证 API |
| `packages/contracts/src/user.ts` | 增加安全相关 DTO |

### 3.5 风险与边界

- 实名认证涉及敏感个人信息，首期不建议自动通过；建议接第三方核验或人工审核。
- 身份证号加密方案需与运维确认密钥管理方式（环境变量/KMS）。
- 支付密码忘记后的找回流程本期不设计，先由客服人工重置。

---

## 4. 积分与优惠券业务接口设计

### 4.1 现有基础

已存在：

- `cm_user_point`：余额、累计获得、累计消耗。
- `cm_user_point_log`：积分流水。
- `GET /users/me/points`、`GET /users/me/points/logs`。
- 订单创建支持 `userCouponIds`，结算页已有优惠券选择组件。

本期在现有基础上补齐业务动作，不重建积分账户。

### 4.2 数据模型

#### 签到记录 `cm_user_sign_in`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `id` | bigint | PK, AUTO | 主键 |
| `user_id` | bigint | NOT NULL | 用户 ID |
| `sign_date` | date | NOT NULL | 签到日期 |
| `streak_days` | int | NOT NULL | 连续签到天数 |
| `reward_points` | int | NOT NULL | 本次奖励积分 |
| `create_time` | datetime | NOT NULL | 创建时间 |

唯一键：`UNIQUE (user_id, sign_date)`，防止重复签到。

#### 积分任务定义 `cm_point_task`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `id` | bigint | PK, AUTO | 主键 |
| `code` | varchar(50) | NOT NULL, UNIQUE | 任务编码，如 `DAILY_BROWSE` |
| `name` | varchar(100) | NOT NULL | 任务名称 |
| `description` | varchar(255) | NULL | 任务说明 |
| `reward_points` | int | NOT NULL | 奖励积分 |
| `frequency` | varchar(20) | NOT NULL | `DAILY` / `ONCE` |
| `enabled` | tinyint(1) | NOT NULL DEFAULT 1 | 是否启用 |
| `sort` | int | NOT NULL DEFAULT 0 | 排序 |

#### 任务完成记录 `cm_point_task_record`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `id` | bigint | PK, AUTO | 主键 |
| `user_id` | bigint | NOT NULL | 用户 ID |
| `task_code` | varchar(50) | NOT NULL | 任务编码 |
| `task_date` | date | NOT NULL | 任务日期（一次性任务用固定日期） |
| `status` | varchar(20) | NOT NULL | `COMPLETED` / `CLAIMED` |
| `reward_points` | int | NOT NULL | 实际发放积分 |
| `create_time` | datetime | NOT NULL | 完成时间 |
| `claim_time` | datetime | NULL | 领取时间 |

唯一键：`UNIQUE (user_id, task_code, task_date)`。

#### 兑换商品 `cm_point_exchange_item`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `id` | bigint | PK, AUTO | 主键 |
| `name` | varchar(100) | NOT NULL | 商品名称 |
| `type` | varchar(20) | NOT NULL | `COUPON` / `PHYSICAL` |
| `points_cost` | int | NOT NULL | 所需积分 |
| `coupon_id` | bigint | NULL | 优惠券模板 ID（COUPON 类型必填） |
| `stock` | int | NOT NULL DEFAULT 0 | 剩余库存 |
| `enabled` | tinyint(1) | NOT NULL DEFAULT 1 | 是否上架 |
| `sort` | int | NOT NULL DEFAULT 0 | 排序 |

#### 兑换记录 `cm_point_exchange_order`

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `id` | bigint | PK, AUTO | 主键 |
| `user_id` | bigint | NOT NULL | 用户 ID |
| `item_id` | bigint | NOT NULL | 兑换商品 ID |
| `points_cost` | int | NOT NULL | 消耗积分快照 |
| `status` | varchar(20) | NOT NULL | `SUCCESS` / `FAILED` |
| `user_coupon_id` | bigint | NULL | 发券成功后的用户券 ID |
| `idempotency_key` | varchar(64) | NULL | 幂等键 |
| `create_time` | datetime | NOT NULL | 创建时间 |

唯一键：`UNIQUE (user_id, idempotency_key)`。

### 4.3 积分变动统一入口

新增 `UserPointService.change(ChangePointCmd)`：

```text
change(userId, changeAmount, type, description, idempotencyKey)
```

规则：

- 在一个事务内更新 `cm_user_point` 并插入 `cm_user_point_log`。
- 消耗积分使用条件更新：`balance >= cost`，失败抛 `POINT_BALANCE_NOT_ENOUGH`。
- 获得积分按幂等键去重，避免支付回调/消息重试重复发放。
- 大于 0 时累加 `total_earned`；小于 0 时累加 `total_spent`。

### 4.4 接口设计

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/users/me/points/sign-in/status` | 今日是否已签到、连续天数、今日奖励 |
| POST | `/users/me/points/sign-in` | 今日签到 |
| GET | `/users/me/points/tasks` | 任务列表及完成/领取状态 |
| POST | `/users/me/points/tasks/{code}/claim` | 领取任务奖励 |
| GET | `/users/me/points/exchange/items` | 积分兑换商品列表 |
| POST | `/users/me/points/exchange` | 兑换商品 |
| POST | `/users/me/points/lottery` | 积分抽奖（Phase 2 可选，不纳入首期） |

#### 签到

- 每日一次；连续签到第 7 天额外奖励，具体梯度由配置/任务表决定。
- 返回本次奖励、连续天数、最新余额。
- 并发签到通过唯一键兜底，重复请求返回已签到结果。

#### 任务

- 任务完成由后端事件驱动，不信任前端直接「完成」。
- 首期任务：
  - `DAILY_BROWSE`：当日浏览 1 个商品。
  - `DAILY_COMMENT`：当日发表 1 条评价。
  - `DAILY_SIGN_IN`：完成签到。
- 前端只负责展示与领取；领取时校验完成状态并幂等发放。

#### 兑换

- 首期只支持兑换优惠券。
- 流程：扣积分 -> 调用 coupon-service 内部发券接口 -> 写兑换记录。
- 发券失败必须回滚积分扣减；若采用最终一致性，则先记录 `FAILED` 并补偿。
- coupon-service 新增内部接口 `POST /internal/coupon/issue`：
  - 参数：`userId`、`couponId`（模板）、`idempotencyKey`。
  - 返回：`userCouponId`。
  - 与现有 `auto-issue-register` 分离，避免语义混用。

#### 积分获得事件

- 订单支付成功：按实付金额发放积分（如 1 元 = 1 积分），幂等键 `orderNo`。
- 评价成功：固定积分，幂等键 `commentId`。
- 签到/任务：由对应接口发放。
- 订单支付事件由 order-service 支付成功流程调用 user-service 内部接口，或通过 MQ 消费；优先复用现有订单事件链路，避免同步耦合。

### 4.5 优惠券「去使用」闭环

现状：个人中心优惠券点击「去使用」只弹提示。

目标：

1. `GET /coupons/me/{id}/scope` 返回该券适用范围、门槛、可用状态。
2. 前端点击「去使用」：
   - 购物车有商品：跳转 `/checkout?userCouponId={id}`。
   - 购物车为空：跳转搜索/分类页，并带 `couponId` 标记，提示「已为你筛选可用商品」。
3. 结算页读取 `userCouponId`，在优惠券列表中预选；不可用时提示并取消预选。
4. 下单时继续走现有 `CreateOrderReq.userCouponIds`，无需新增下单接口。

新增接口建议放在 coupon-service：

- `GET /coupons/me/{id}/scope` -> `{ scopeType, categoryIds, productIds, threshold, usable }`
- 可选：`GET /coupons/me/{id}/applicable-products?page=&pageSize=`，直接返回可用商品分页，便于前端筛选。

### 4.6 前端改造

| 文件 | 改动 |
|---|---|
| `Points.vue` | 签到按钮、任务列表、兑换入口、积分刷新 |
| `Coupons.vue` | 「去使用」跳转与适用范围提示 |
| `checkout` | 读取 `userCouponId` 预选优惠券 |
| `domain/user/api` | 签到、任务、兑换 API |
| `domain/coupon/api` | 券适用范围 API |
| `packages/contracts` | 新增积分/兑换/券范围 DTO |

### 4.7 风险与边界

- 积分发放必须幂等，否则支付重试会造成重复积分。
- 兑换涉及跨服务（user-service、coupon-service），必须设计补偿或事务消息，不能只做本地事务。
- 任务完成事件需要防刷：浏览任务按商品去重，评价任务按评论 ID 去重。
- 抽奖涉及概率与合规，首期不建议上线。

---

## 5. 地址区域后端化设计

### 5.1 现状

- 前端 `packages/utils/src/china-region-data.ts` 内置省/市/区数据（约 3700 行）。
- `domain/address/api/index.ts` 的 `fetchRegionChildren` 仍调用 `region-mock.ts`。
- 地址表只保存省/市/区名称，没有代码。

### 5.2 方案对比

| 方案 | 优点 | 缺点 | 结论 |
|---|---|---|---|
| 后端静态类内置区域数据 | 改动最小、无建表 | 数据更新需发版 | 备选 |
| `cm_region` 表 + 查询接口 | 可运营维护、可扩展 | 需导数据、建表 | 推荐 |
| 独立 region-service | 职责清晰 | 当前规模过重 | 不采用 |

### 5.3 数据模型

新增 `cm_region`：

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| `code` | varchar(12) | PK | 行政区代码 |
| `parent_code` | varchar(12) | NULL | 父级代码，省级为 NULL |
| `name` | varchar(64) | NOT NULL | 名称 |
| `level` | tinyint | NOT NULL | 1 省 / 2 市 / 3 区县 |
| `sort` | int | NOT NULL DEFAULT 0 | 排序 |
| `enabled` | tinyint(1) | NOT NULL DEFAULT 1 | 是否启用 |

索引：`KEY idx_parent_level (parent_code, level, sort)`。

数据来源：将 `china-region-data.ts` 转换为 SQL 种子脚本，保留现有代码与名称；后续可替换为官方行政区划数据。

### 5.4 接口设计

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/regions/children?parentCode=` | 查询下级区域；不传 `parentCode` 返回省级 |
| GET | `/regions/path?code=` | 查询某区域的省/市/区路径 |

响应示例：

```json
[
  { "code": "110000", "name": "北京市", "level": 1, "leaf": false },
  { "code": "120000", "name": "天津市", "level": 1, "leaf": false }
]
```

规则：

- 只返回 `enabled = 1` 的区域。
- 按 `sort, code` 排序。
- 结果可加本地缓存（如 Caffeine/Redis，按 `parentCode` 缓存）。
- 区域查询为只读，可加入网关白名单；若不做白名单，则要求登录，与地址页使用场景一致。

### 5.5 地址表补充

`cm_address` 增加可选代码字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `province_code` | varchar(12) | 省级代码 |
| `city_code` | varchar(12) | 市级代码 |
| `district_code` | varchar(12) | 区县代码 |

保留 `province` / `city` / `district` 名称作为快照，避免区域改名影响历史订单地址。

### 5.6 前端改造

- `fetchRegionChildren` 改为 `GET /regions/children`，删除 `region-mock.ts`。
- `RegionPicker` 无需改结构，只替换数据源。
- 地址表单提交时同时提交代码与名称。
- 增加内存缓存/`sessionStorage`，避免重复请求同一层级。
- 保留区域加载失败提示与重试。

### 5.7 验收

- 省/市/区三级懒加载正常，切换省份后城市/区县刷新。
- 断网或接口失败时显示可重试提示，不阻塞表单其余字段。
- 地址保存后重新编辑，能正确回显省市区。
- 现有历史地址（无代码）仍能正常展示和编辑。

---

## 6. 公共接口与错误码

### 6.1 新增错误码建议

| 错误码 | 含义 |
|---|---|
| `PAY_PASSWORD_NOT_SET` | 未设置支付密码 |
| `PAY_PASSWORD_ERROR` | 支付密码错误 |
| `PAY_PASSWORD_RETRY_LIMIT` | 支付密码尝试次数过多 |
| `REAL_NAME_ALREADY_VERIFIED` | 已实名，不能重复提交 |
| `REAL_NAME_PENDING` | 实名认证审核中 |
| `REAL_NAME_INVALID` | 姓名/身份证格式不合法 |
| `POINT_BALANCE_NOT_ENOUGH` | 积分不足 |
| `POINT_TASK_NOT_COMPLETED` | 任务未完成，不能领取 |
| `POINT_TASK_ALREADY_CLAIMED` | 任务奖励已领取 |
| `POINT_EXCHANGE_OUT_OF_STOCK` | 兑换商品库存不足 |
| `REGION_NOT_FOUND` | 区域不存在 |
| `REGION_LEVEL_INVALID` | 区域层级不合法 |

### 6.2 统一约束

- 所有接口继续返回 `Result<T>`。
- 用户 ID 一律从 `X-Auth-User`/`UserContextHolder` 取，不信任请求参数。
- 金额、积分、库存变更使用事务 + 条件更新，禁止先查后改的无锁写法。
- 所有新增表遵循 `cm_` 前缀、`create_time` / `update_time` / 逻辑删除约定（流水表可只保留创建时间）。

---

## 7. 实施顺序与验收

### Step 1：账户安全（需确认后执行）

1. 建 `cm_user_security`。
2. 实现安全信息查询、支付密码设置/校验、实名提交/查询。
3. 前端 PersonalInfo 接入。
4. 支付弹窗增加支付密码步骤（配置开关默认关闭）。

验收：

- 首次设置支付密码成功，旧密码错误被拒绝。
- 连续错误 5 次触发限流。
- 实名提交后状态为 `PENDING`，审核通过/驳回可查询。
- 页面不再出现「暂未开放」。

### Step 2：积分与优惠券

1. 建签到、任务、兑换相关表。
2. 实现签到、任务查询/领取、兑换接口。
3. 订单支付/评价事件发放积分（幂等）。
4. 优惠券适用范围接口 + 结算页预选。

验收：

- 每日签到只能成功一次，连续天数正确。
- 任务奖励只能领取一次，积分余额与流水一致。
- 积分不足不能兑换，兑换失败不扣积分。
- 优惠券「去使用」能进入结算并预选正确优惠券。

### Step 3：区域后端化

1. 建 `cm_region` 并导入现有区域数据。
2. 实现 children/path 接口。
3. 前端替换 mock，地址表补代码字段。

验收：

- 三级联动正常，接口失败可重试。
- 新建/编辑地址代码与名称一致。
- 历史地址不受影响。

### Step 4：不处理项

- 测试数据清理。
- 自动提交/PR 创建。
- 区域数据运营后台（本期只提供只读查询）。

---

## 8. 待确认问题

请确认以下决策后我再开始编码：

1. 支付密码是否必须接入支付流程，还是仅做账户设置展示？建议先做设置与校验接口，支付前置校验用配置开关控制。
2. 实名认证首期采用「提交后人工审核」还是直接接第三方核验？人工审核需要补运营审核入口。
3. 积分规则是否按「1 元 = 1 积分」？是否需要订单退款时扣回积分？
4. 积分兑换首期是否只支持优惠券？是否需要积分抽奖？
5. 区域数据是否接受「从现有前端数据导入 `cm_region`」？还是要求使用官方最新行政区划数据？
6. 区域接口是否允许匿名访问？建议仅登录用户可访问，不加入网关白名单。

确认后按 Phase 1 → Phase 2 → Phase 3 顺序执行；Phase 1 只做设计与评审，不提前改代码。

---

## 9. 实现记录（2026-11-08）

### 9.1 账户安全

- 新增 `cm_user_security`，包含支付密码哈希、实名密文与 `real_name_mask` / `id_card_mask` 脱敏字段。
- 新增 `UserSecurityController`、`InternalUserSecurityController`、`IdCardCipher`、`IdCardValidator`、`MaskUtil`。
- 支付密码使用 BCrypt；身份证号使用 AES-GCM；查询只读脱敏字段。
- 支付密码连续失败次数使用 Redis 限制（10 分钟 5 次）。
- 实名认证支持提交、查询和内部审核；前端「支付密码」「实名认证」已替换「暂未开放」。
- 支付弹窗已支持支付密码校验步骤；当前通过 `VITE_REQUIRE_PAY_PASSWORD` 控制，默认关闭，联调完成后可开启。

### 9.2 积分与优惠券

- 新增 `cm_user_sign_in`、`cm_point_task`、`cm_point_task_record`、`cm_point_exchange_item`、`cm_point_exchange_order`。
- 新增签到、签到状态、任务列表、任务领取、兑换商品、积分兑换接口。
- `cm_user_point_log` 增加 `idempotency_key`，订单支付积分按订单号幂等发放，规则为 1 元 = 1 积分。
- 浏览商品完成任务 `DAILY_BROWSE`；评价创建后调用内部任务完成接口（评价主流程失败不阻塞）。
- 兑换首期只支持优惠券；兑换成功后调用 coupon-service 内部发券接口。
- 因 `cm_user_coupon` 的用户-模板唯一约束，首期同一兑换商品每人只能兑换一次，前端展示「已兑换」。
- 新增 `GET /coupons/me/{id}/scope`；个人中心「去使用」按购物车状态跳转结算页/搜索页，结算页支持 `userCouponId` 预选。
- 积分抽奖未启用（设计中为可选项）。

### 9.3 地址区域

- 新增 `cm_region`，从现有前端行政区数据导入 3431 条省/市/区县记录。
- 新增 `GET /regions/children`、`GET /regions/path`，网关 `/regions/**` 已路由到 user-service。
- 前端 `fetchRegionChildren` 已改为后端请求，移除 `region-mock.ts`。
- `cm_address` 增加 `province_code` / `city_code` / `district_code`，地址表单同时提交代码与名称。
- 历史地址无代码时仍按名称展示，编辑时回退本地名称映射。

### 9.4 验证

- 后端新增代码已通过 `mvn -pl services/user-service,services/coupon-service,services/order-service -am -DskipTests package`。
- 前端通过 `npm run type-check`、涉及文件 ESLint 与 `npm run build-only`。
- 浏览器验证：签到、任务领取、积分兑换、兑换后「已兑换」、支付积分到账、实名/支付密码展示、区域下拉、优惠券结算预选均正常。

### 9.5 未纳入本期

- 积分抽奖。
- 积分兑换实物商品。
- 支付密码找回/客服重置流程。
- 实名认证外部第三方核验对接（当前使用内部审核接口）。
- 测试数据清理与自动提交。
