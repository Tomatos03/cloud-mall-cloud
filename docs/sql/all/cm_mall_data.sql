-- ============================================================
-- cm_mall — 种子数据（供开发 / 测试环境使用）
-- 执行前请先执行 cm_mall.sql 初始化表结构
-- ============================================================

USE cm_mall;

-- ============================================================
-- 用户模块 - 用户表
-- 密码统一为 "123456"（bcrypt cost=10）
-- ============================================================
INSERT INTO cm_user (id, username, password, nickname, phone, email, bio, avatar_url, types, store_id) VALUES
(1, 'admin',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '系统管理员', '13800000001', 'admin@cloudmall.com', '系统管理员', NULL, 'ADMIN', NULL),
(2, 'merchant1', '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '李明',       '13800000002', 'liming@shop.com',    '数码产品爱好者', NULL, 'MERCHANT', 1),
(3, 'merchant2', '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '王芳',       '13800000003', 'wangfang@shop.com',  '时尚穿搭达人',   NULL, 'MERCHANT', 2),
(4, 'user1',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '张三',       '13800000004', 'zhangsan@mail.com',  '普通用户一枚',   NULL, 'NORMAL',   NULL),
(5, 'user2',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '赵六',       '13800000005', 'zhaoliu@mail.com',   NULL,             NULL, 'NORMAL',   NULL);

ALTER TABLE cm_user AUTO_INCREMENT = 100;

-- ============================================================
-- 角色表（扩展已有数据；id=1 admin 已存在）
-- ============================================================
INSERT INTO cm_role (id, name, built_in, enabled, description) VALUES
(2, 'merchant', 1, 1, '商家角色 — 管理商品、处理订单'),
(3, 'user',     1, 1, '普通用户角色 — 浏览、下单');

ALTER TABLE cm_role AUTO_INCREMENT = 100;

-- ============================================================
-- 用户-角色 关联
-- ============================================================
INSERT INTO cm_user_role (user_id, role_id) VALUES
(1, 1),  -- admin → admin
(2, 2),  -- merchant1 → merchant
(3, 2),  -- merchant2 → merchant
(4, 3),  -- user1 → user
(5, 3);  -- user2 → user

-- ============================================================
-- 资源权限（扩展已有数据；id=1,10~13,20~23,30~33,40,50,60 已存在）
-- ============================================================
INSERT INTO cm_resource (id, code, name, type, parent_id, sort, enabled) VALUES
(100, 'store:view',        '店铺查看',      'api', NULL, 1, 1),
(101, 'goods:audit',       '商品审核',      'api', NULL, 1, 1),
(102, 'comment:manage',    '评论管理',      'api', NULL, 1, 1),
(103, 'report:view',       '数据报表查看',   'api', NULL, 1, 1),
(104, 'system:config',     '系统配置',      'api', NULL, 1, 1),
(200, 'user:menu',         '用户管理菜单',   'menu', NULL, 10, 1),
(201, 'goods:menu',        '商品管理菜单',   'menu', NULL, 20, 1),
(202, 'order:menu',        '订单管理菜单',   'menu', NULL, 30, 1),
(300, 'order:export',      '订单导出',      'button', NULL, 1, 1),
(301, 'goods:batch-op',    '商品批量操作',   'button', NULL, 1, 1);

ALTER TABLE cm_resource AUTO_INCREMENT = 1000;

-- 为 merchant 角色授权
INSERT INTO cm_role_resource (role_id, resource_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13),  -- goods CRUD
(2, 20), (2, 21), (2, 22),            -- order 查看/创建/更新
(2, 40),                               -- store:manage
(2, 100);                              -- store:view

-- 为 user 角色授权
INSERT INTO cm_role_resource (role_id, resource_id) VALUES
(3, 10),  -- goods:view
(3, 20);  -- order:view

-- ============================================================
-- 商家店铺
-- ============================================================
INSERT INTO cm_store (id, user_id, store_name, store_logo, description, status) VALUES
(1, 2, '锐意数码旗舰店',  NULL, '专注正品数码产品，手机、电脑、智能设备一站式购齐', 1),
(2, 3, '芳华服饰精品店',  NULL, '时尚女装、潮流男装，品质生活从这里开始',             1);

ALTER TABLE cm_store AUTO_INCREMENT = 100;

-- ============================================================
-- 用户收货地址
-- ============================================================
INSERT INTO cm_address (id, user_id, consignee, phone, province, city, district, detail, zip_code, is_default) VALUES
(1, 4, '张三',   '13800000004', '广东省', '深圳市', '南山区', '科技园南区A栋1201', '518000', 1),
(2, 4, '张三',   '13800000004', '广东省', '广州市', '天河区', '珠江新城B座806',   '510000', 0),
(3, 5, '赵六',   '13800000005', '北京市', '北京市',  '朝阳区', '望京SOHO T3 2204', '100000', 1);

ALTER TABLE cm_address AUTO_INCREMENT = 100;

-- ============================================================
-- 商品分类（三级）
-- ============================================================
-- 一级
INSERT INTO cm_category (id, name, parent_id, level, icon, sort_order, status) VALUES
(1,  '数码电子', 0, 1, NULL, 1, 1),
(2,  '服饰鞋包', 0, 1, NULL, 2, 1),
(3,  '食品生鲜', 0, 1, NULL, 3, 1),
(4,  '家居生活', 0, 1, NULL, 4, 1);

-- 二级
INSERT INTO cm_category (id, name, parent_id, level, icon, sort_order, status) VALUES
(10, '手机通讯', 1, 2, NULL, 1, 1),
(11, '电脑办公', 1, 2, NULL, 2, 1),
(12, '音频耳机', 1, 2, NULL, 3, 1),
(20, '女装',     2, 2, NULL, 1, 1),
(21, '男装',     2, 2, NULL, 2, 1),
(22, '鞋履',     2, 2, NULL, 3, 1),
(30, '休闲零食', 3, 2, NULL, 1, 1),
(31, '饮料冲调', 3, 2, NULL, 2, 1),
(40, '厨房用品', 4, 2, NULL, 1, 1);

-- 三级
INSERT INTO cm_category (id, name, parent_id, level, icon, sort_order, status) VALUES
(101, '智能手机',   10, 3, NULL, 1, 1),
(102, '手机配件',   10, 3, NULL, 2, 1),
(111, '笔记本',     11, 3, NULL, 1, 1),
(112, '台式机',     11, 3, NULL, 2, 1),
(121, '蓝牙耳机',   12, 3, NULL, 1, 1),
(122, '有线耳机',   12, 3, NULL, 2, 1),
(201, '连衣裙',     20, 3, NULL, 1, 1),
(202, 'T恤',        20, 3, NULL, 2, 1),
(211, '衬衫',       21, 3, NULL, 1, 1),
(212, '休闲裤',     21, 3, NULL, 2, 1),
(301, '坚果炒货',   30, 3, NULL, 1, 1),
(302, '饼干糕点',   30, 3, NULL, 2, 1);

ALTER TABLE cm_category AUTO_INCREMENT = 1000;

-- ============================================================
-- 商品单位
-- ============================================================
INSERT INTO cm_unit (id, name, status, sort) VALUES
(1, '件', 1, 1),
(2, '个', 1, 2),
(3, '斤', 1, 3),
(4, '台', 1, 4),
(5, '双', 1, 5),
(6, '箱', 1, 6),
(7, '盒', 1, 7);

ALTER TABLE cm_unit AUTO_INCREMENT = 100;

-- ============================================================
-- 商品信息
-- ============================================================
INSERT INTO cm_goods (id, name, spu, image, images, description, category_id, brand, price, market_price, stock, sales_count, status) VALUES
(1,  '华为 Mate 70 Pro',         'SPU20250001', NULL, NULL, '搭载麒麟芯片，超强影像系统，鸿蒙OS 5.0',             101, '华为',   6999.00, 7999.00, 100, 1280, 1),
(2,  'Apple iPhone 16 Pro Max', 'SPU20250002', NULL, NULL, 'A18 Pro 芯片，钛金属设计，4800万像素专业级拍摄',       101, 'Apple',  9999.00, 10999.00, 80,  980, 1),
(3,  '联想 ThinkPad X1 Carbon', 'SPU20250003', NULL, NULL, '第14代酷睿Ultra，14英寸2.8K OLED屏，轻薄商务',         111, '联想',   12999.00, 14999.00, 50,  356, 1),
(4,  'Sony WH-1000XM6 降噪耳机','SPU20250004', NULL, NULL, '旗舰级降噪，30小时续航，Hi-Res认证',                    121, 'Sony',   2999.00, 3499.00,  200, 2150, 1),
(5,  '女士蕾丝刺绣连衣裙',       'SPU20250005', NULL, NULL, '法式优雅蕾丝设计，修身显瘦，夏季新款',                  201, '芳华',   399.00,  599.00,  300, 3200, 1),
(6,  '男士纯棉商务衬衫',         'SPU20250006', NULL, NULL, '免烫抗皱面料，经典修身版型，商务休闲两穿',              211, '芳华',   259.00,  359.00,  500, 4500, 1),
(7,  '三只松鼠每日坚果礼盒',     'SPU20250007', NULL, NULL, '7种坚果混合，每日一包，营养均衡',                       301, '三只松鼠', 89.90, 129.00, 1000, 8200, 1),
(8,  '青岛啤酒经典1903 500ml*12','SPU20250008', NULL, NULL, '百年经典工艺，麦香浓郁，纯正德国风味',                  31,  '青岛啤酒', 69.90, 89.90,  600, 5600, 1);

ALTER TABLE cm_goods AUTO_INCREMENT = 1000;

-- ============================================================
-- 商品规格
-- ============================================================
INSERT INTO cm_goods_spec (id, goods_id, name, value, sort_order) VALUES
-- 华为 Mate 70 Pro（颜色 + 存储组合）
(1,  1, '颜色', '曜金黑',   1),
(2,  1, '颜色', '雪域白',   2),
(3,  1, '颜色', '罗兰紫',   3),
(4,  1, '存储', '256GB',    1),
(5,  1, '存储', '512GB',    2),
(6,  1, '存储', '1TB',      3),
-- iPhone 16 Pro Max（颜色 + 存储）
(7,  2, '颜色', '原色钛金属', 1),
(8,  2, '颜色', '白色钛金属', 2),
(9,  2, '存储', '256GB',     1),
(10, 2, '存储', '512GB',     2),
(11, 2, '存储', '1TB',       3),
-- Sony 耳机（颜色）
(12, 4, '颜色', '黑色',      1),
(13, 4, '颜色', '铂金银',    2),
(14, 4, '颜色', '午夜蓝',    3),
-- 连衣裙（尺码 + 颜色）
(15, 5, '尺码', 'S',         1),
(16, 5, '尺码', 'M',         2),
(17, 5, '尺码', 'L',         3),
(18, 5, '颜色', '米白色',    1),
(19, 5, '颜色', '樱花粉',    2);

ALTER TABLE cm_goods_spec AUTO_INCREMENT = 1000;

-- ============================================================
-- 商品 SKU
-- ============================================================
INSERT INTO cm_goods_sku (id, goods_id, spec_ids, image, price, stock) VALUES
-- 华为 Mate 70 Pro：曜金黑 256GB / 512GB / 1TB
(1,  1, '[1,4]',  NULL, 6999.00,  30),
(2,  1, '[1,5]',  NULL, 7999.00,  25),
(3,  1, '[1,6]',  NULL, 9999.00,  10),
(4,  1, '[2,5]',  NULL, 7999.00,  20),
-- iPhone 16 Pro Max：原色钛金属 256GB / 512GB
(5,  2, '[7,9]',  NULL, 9999.00,  25),
(6,  2, '[7,10]', NULL, 11999.00, 20),
(7,  2, '[8,9]',  NULL, 9999.00,  15),
-- 联想 ThinkPad X1 Carbon（单规格）
(8,  3, '[]',     NULL, 12999.00, 50),
-- Sony WH-1000XM6：黑色 / 铂金银
(9,  4, '[12]',   NULL, 2999.00,  80),
(10, 4, '[13]',   NULL, 2999.00,  60),
(11, 4, '[14]',   NULL, 3099.00,  60),
-- 连衣裙：S码米白色 / M码米白色 / M码樱花粉
(12, 5, '[15,18]', NULL, 399.00, 100),
(13, 5, '[16,18]', NULL, 399.00, 100),
(14, 5, '[16,19]', NULL, 399.00, 80),
-- 衬衫：单规格
(15, 6, '[]',     NULL, 259.00,  300),
-- 每日坚果（单规格）
(16, 7, '[]',     NULL, 89.90,   600),
-- 青岛啤酒（单规格）
(17, 8, '[]',     NULL, 69.90,   400);

ALTER TABLE cm_goods_sku AUTO_INCREMENT = 1000;

-- ============================================================
-- 首页轮播图
-- ============================================================
INSERT INTO cm_banner (id, title, image, link_url, sort_order, status) VALUES
(1, '华为 Mate 70 Pro 新品首发',   NULL, '/goods/1',  1, 1),
(2, '夏季清仓 全场低至5折',        NULL, '/category/20', 2, 1),
(3, 'iPhone 16 Pro Max 限时特惠',  NULL, '/goods/2',  3, 1),
(4, '新用户注册即领100元优惠券',   NULL, '/register', 4, 0);  -- 已下架

ALTER TABLE cm_banner AUTO_INCREMENT = 100;

-- ============================================================
-- 用户收藏
-- ============================================================
INSERT INTO cm_favorite (id, user_id, goods_id) VALUES
(1, 4, 1),
(2, 4, 4),
(3, 4, 7),
(4, 5, 2),
(5, 5, 6);

ALTER TABLE cm_favorite AUTO_INCREMENT = 100;

-- ============================================================
-- 商品评论
-- ============================================================
INSERT INTO cm_comment (id, goods_id, user_id, order_id, content, rating, images, status) VALUES
(1, 1, 4, 1, '手机非常好用，拍照效果一流，鸿蒙系统超级流畅！', 5, NULL, 1),
(2, 1, 5, 2, '用了两周，续航很给力，就是价格有点贵',        4, NULL, 1),
(3, 4, 4, 1, '降噪效果惊艳，坐地铁完全听不到噪音，强烈推荐',  5, NULL, 1),
(4, 5, 5, 2, '裙子很漂亮，材质舒适，尺码标准',              5, NULL, 1),
(5, 7, 4, 1, '坚果很新鲜，每天一包很方便，会回购',          4, NULL, 1),
(6, 6, 5, 2, '衬衫版型很好，免烫确实省心，值得购买',        5, NULL, 1);

ALTER TABLE cm_comment AUTO_INCREMENT = 1000;

-- ============================================================
-- 优惠券模板
-- ============================================================
INSERT INTO cm_coupon (id, name, type, threshold, discount, total_count, claimed_count, status, start_time, expire_time) VALUES
(1, '新人满减券',   'FULL_REDUCTION', 100.00,  20.00,   1000, 150,  1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, '满500减50',    'FULL_REDUCTION', 500.00,  50.00,   500,  80,   1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(3, '满1000减120',  'FULL_REDUCTION', 1000.00, 120.00,  200,  30,   1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(4, '全场9折券',     'DISCOUNT',       NULL,    0.10,    500,  200,  1, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY)),
(5, '无门槛10元券',   'CASH',           NULL,    10.00,   300,  100,  1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

ALTER TABLE cm_coupon AUTO_INCREMENT = 100;

-- ============================================================
-- 用户领取的优惠券
-- ============================================================
INSERT INTO cm_user_coupon (id, user_id, coupon_id, status, claimed_time, order_id) VALUES
(1, 4, 1, 'UNUSED', NOW(), NULL),
(2, 4, 4, 'UNUSED', NOW(), NULL),
(3, 5, 1, 'UNUSED', NOW(), NULL),
(4, 5, 5, 'UNUSED', NOW(), NULL);

ALTER TABLE cm_user_coupon AUTO_INCREMENT = 100;

-- ============================================================
-- 订单
-- ============================================================
INSERT INTO cm_order (id, order_no, user_id, total_amount, status, address_snapshot, remark) VALUES
(1, 'ORD202507010001', 4, 11997.00, 'PAID',
    '{"consignee":"张三","phone":"13800000004","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园南区A栋1201"}',
    '请尽快发货'),
(2, 'ORD202507020002', 5, 658.00,  'SHIPPED',
    '{"consignee":"赵六","phone":"13800000005","province":"北京市","city":"北京市","district":"朝阳区","detail":"望京SOHO T3 2204"}',
    NULL),
(3, 'ORD202507030003', 4, 89.90,  'PENDING',
    '{"consignee":"张三","phone":"13800000004","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园南区A栋1201"}',
    NULL);

ALTER TABLE cm_order AUTO_INCREMENT = 1000;

-- ============================================================
-- 订单明细
-- ============================================================
INSERT INTO cm_order_item (id, order_id, goods_id, sku_id, goods_name, goods_image, price, quantity, subtotal) VALUES
-- 订单1：华为 Mate 70 Pro 曜金黑 512GB + Sony WH-1000XM6 黑色
(1, 1, 1, 2,  '华为 Mate 70 Pro（曜金黑 512GB）', NULL, 7999.00, 1, 7999.00),
(2, 1, 4, 9,  'Sony WH-1000XM6 降噪耳机（黑色）', NULL, 2999.00, 1, 2999.00),
(3, 1, 7, 16, '三只松鼠每日坚果礼盒',            NULL, 89.90,   1, 89.90),
-- 订单2：连衣裙 M码米白色 + 衬衫
(4, 2, 5, 13, '女士蕾丝刺绣连衣裙（M 米白色）',   NULL, 399.00,  1, 399.00),
(5, 2, 6, 15, '男士纯棉商务衬衫',                 NULL, 259.00,  1, 259.00),
-- 订单3：每日坚果（待付款）
(6, 3, 7, 16, '三只松鼠每日坚果礼盒',            NULL, 89.90,   1, 89.90);

ALTER TABLE cm_order_item AUTO_INCREMENT = 1000;

-- ============================================================
-- 购物车
-- ============================================================
INSERT INTO cm_cart (id, user_id, goods_id, sku_id, goods_name, goods_image, price, quantity, selected) VALUES
(1, 4, 2, 5,  'Apple iPhone 16 Pro Max（原色钛金属 256GB）', NULL, 9999.00, 1, 1),
(2, 4, 6, 15, '男士纯棉商务衬衫',                          NULL, 259.00,  2, 1),
(3, 5, 1, 1,  '华为 Mate 70 Pro（曜金黑 256GB）',           NULL, 6999.00, 1, 1),
(4, 5, 8, 17, '青岛啤酒经典1903 500ml*12',                  NULL, 69.90,   2, 0);

ALTER TABLE cm_cart AUTO_INCREMENT = 1000;

-- ============================================================
-- 秒杀活动
-- ============================================================
INSERT INTO cm_seckill_activity (id, name, start_time, end_time, status, create_user) VALUES
(1, '618数码狂欢节',            NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY),  'APPROVED', 2),
(2, '暑期清凉特卖',             DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 17 DAY), 'PENDING', 2),
(3, '618服饰专场（已结束）',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), 'FINISHED', 3);

ALTER TABLE cm_seckill_activity AUTO_INCREMENT = 100;

-- ============================================================
-- 秒杀商品
-- ============================================================
INSERT INTO cm_seckill_goods (id, activity_id, goods_id, seckill_price, stock, sold_count, audit_status) VALUES
(1, 1, 1, 5999.00,  10, 3, 'APPROVED'),
(2, 1, 4, 2499.00,  20, 5, 'APPROVED'),
(3, 1, 7, 69.90,    50, 12, 'APPROVED'),
(4, 3, 5, 299.00,   30, 28, 'APPROVED'),
(5, 3, 6, 199.00,   40, 35, 'APPROVED');

ALTER TABLE cm_seckill_goods AUTO_INCREMENT = 100;

-- ============================================================
-- AI 聊天会话（示例）
-- ============================================================
INSERT INTO cm_chat_session (id, session_id, user_id, title) VALUES
(1, UUID(), 4, '商品推荐咨询'),
(2, UUID(), 4, '手机选购建议'),
(3, UUID(), 5, '订单问题咨询');

ALTER TABLE cm_chat_session AUTO_INCREMENT = 1000;

drop table cm_goods