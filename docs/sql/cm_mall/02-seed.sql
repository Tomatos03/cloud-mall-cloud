-- ============================================================
-- cm_mall — 种子数据（供开发 / 测试环境使用）
-- 执行前请先执行 01-schema.sql 初始化表结构
-- 与 01-schema.sql 当前表结构严格对齐（优惠券新模型 + 父子订单 + 商品多规格 + cm_shop
-- + 库存流水 + 消息 Outbox/消费幂等 + IM 会话消息）
-- ============================================================

SET NAMES utf8mb4;

USE cm_mall;

-- ============================================================
-- 用户模块 - 用户表
-- 密码统一为 "123456"（bcrypt cost=10）
-- 注：cm_user 不再持有 store_id，店铺归属统一走 cm_shop（cm_shop.user_id）
-- ============================================================
INSERT INTO cm_user (id, username, password, nickname, phone, email, bio, avatar_url, types) VALUES
(1, 'admin',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '系统管理员', '13800000001', 'admin@cloudmall.com', '系统管理员', NULL, 'ADMIN'),
(2, 'merchant1', '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '李明',       '13800000002', 'liming@shop.com',    '数码产品爱好者', NULL, 'MERCHANT'),
(3, 'merchant2', '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '王芳',       '13800000003', 'wangfang@shop.com',  '时尚穿搭达人',   NULL, 'MERCHANT'),
(4, 'user1',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '张三',       '13800000004', 'zhangsan@mail.com',  '普通用户一枚',   NULL, 'NORMAL'),
(5, 'user2',    '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '赵六',       '13800000005', 'zhaoliu@mail.com',   NULL,             NULL, 'NORMAL'),
(6, 'merchant3', '$2b$10$1dhvaWKeqZDhFP0N4wnphu71zmIgPQGDDWcYCMDE.wVKrzG0LubMO', '陈刚',      '13800000006', 'chengang@food.com',  '食品行业从业者', NULL, 'MERCHANT');

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
(5, 3),  -- user2 → user
(6, 2);  -- merchant3 → merchant

-- ============================================================
-- 资源权限（扩展已有数据；id=1,10~13,20~23,30~33,40,50,60 已存在）
-- 编码遵循全局重命名：store → shop、goods → product
-- ============================================================
INSERT INTO cm_resource (id, code, name, type, parent_id, sort, enabled) VALUES
(100, 'shop:view',        '店铺查看',      'api', NULL, 1, 1),
(101, 'product:audit',    '商品审核',      'api', NULL, 1, 1),
(102, 'comment:manage',   '评论管理',      'api', NULL, 1, 1),
(103, 'report:view',      '数据报表查看',   'api', NULL, 1, 1),
(104, 'system:config',    '系统配置',      'api', NULL, 1, 1),
(200, 'user:menu',        '用户管理菜单',   'menu', NULL, 10, 1),
(201, 'product:menu',     '商品管理菜单',   'menu', NULL, 20, 1),
(202, 'order:menu',       '订单管理菜单',   'menu', NULL, 30, 1),
(300, 'order:export',     '订单导出',      'button', NULL, 1, 1),
(301, 'product:batch-op', '商品批量操作',   'button', NULL, 1, 1);

ALTER TABLE cm_resource AUTO_INCREMENT = 1000;

-- 为 merchant 角色授权
INSERT INTO cm_role_resource (role_id, resource_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13),  -- product CRUD
(2, 20), (2, 21), (2, 22),            -- order 查看/创建/更新
(2, 40),                              -- shop:manage
(2, 100);                             -- shop:view

-- 为 user 角色授权
INSERT INTO cm_role_resource (role_id, resource_id) VALUES
(3, 10),  -- product:view
(3, 20);  -- order:view

-- ============================================================
-- 商家店铺（cm_shop 替代旧 cm_store，商品通过 shop_id 归属）
-- ============================================================
INSERT INTO cm_shop (id, user_id, name, logo, description, status) VALUES
(1, 2, '锐意数码旗舰店',  NULL, '专注正品数码产品，手机、电脑、智能设备一站式购齐', 1),
(2, 3, '芳华服饰精品店',  NULL, '时尚女装、潮流男装，品质生活从这里开始',             1),
(3, 6, '鲜享食品专营店',  NULL, '休闲零食、酒水饮料，产地直供新鲜到家',               1);

ALTER TABLE cm_shop AUTO_INCREMENT = 100;

-- ============================================================
-- 店铺统计（cm_shop_stat，按 shop_id 一行一店）
-- ============================================================
INSERT INTO cm_shop_stat (shop_id, product_count, total_review_count, positive_review_count, total_score, follow_count, update_time) VALUES
(1, 286,  1000, 980, 4900, 128000, NOW()),
(2, 320,  500,  470, 2450, 56000,  NOW()),
(3, 150,  300,  285, 1430, 21000,  NOW());

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
INSERT INTO cm_category (id, name, parent_id, level, icon, sort, status) VALUES
(1,  '数码电子', 0, 1, NULL, 1, 1),
(2,  '服饰鞋包', 0, 1, NULL, 2, 1),
(3,  '食品生鲜', 0, 1, NULL, 3, 1),
(4,  '家居生活', 0, 1, NULL, 4, 1);

-- 二级
INSERT INTO cm_category (id, name, parent_id, level, icon, sort, status) VALUES
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
INSERT INTO cm_category (id, name, parent_id, level, icon, sort, status) VALUES
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
-- 品牌（全局独立表，商品通过 brand_id 引用）
-- ============================================================
INSERT INTO cm_brand (id, name, name_en, logo, description, country, website, status, sort) VALUES
(1, '华为',     'HUAWEI',   NULL, '全球领先的ICT基础设施和智能终端提供商', '中国', 'https://www.huawei.com',   1, 1),
(2, 'Apple',    'Apple',    NULL, '全球科技公司，iPhone、Mac 等产品',       '美国', 'https://www.apple.com',    1, 2),
(3, '联想',     'Lenovo',   NULL, '全球个人电脑领导品牌',                  '中国', 'https://www.lenovo.com',   1, 3),
(4, 'Sony',     'SONY',     NULL, '音频、影像与游戏娱乐专家',              '日本', 'https://www.sony.com',     1, 4),
(5, '芳华',     'FANGHUA',  NULL, '原创设计女装/男装品牌',                 '中国', NULL,                        1, 5),
(6, '三只松鼠', 'Three Squirrels', NULL, '国民零食品牌',                  '中国', 'https://www.3songshu.com', 1, 6),
(7, '青岛啤酒', 'TSINGTAO', NULL, '百年经典啤酒品牌',                      '中国', 'https://www.tsingtao.com.cn', 1, 7);

ALTER TABLE cm_brand AUTO_INCREMENT = 100;

-- ============================================================
-- 商品图片（首张为主图 is_main=1，其余按 sort 轮播）
-- cm_product.image_id 指向各商品的主图
-- ============================================================
INSERT INTO cm_product_image (id, product_id, url, is_main, sort) VALUES
(1,  1, 'https://img.cloudmall.dev/product/mate70/main.jpg',    1, 0),
(2,  1, 'https://img.cloudmall.dev/product/mate70/2.jpg',       0, 1),
(3,  1, 'https://img.cloudmall.dev/product/mate70/3.jpg',       0, 2),
(4,  2, 'https://img.cloudmall.dev/product/iphone16/main.jpg',  1, 0),
(5,  2, 'https://img.cloudmall.dev/product/iphone16/2.jpg',     0, 1),
(6,  2, 'https://img.cloudmall.dev/product/iphone16/3.jpg',     0, 2),
(7,  3, 'https://img.cloudmall.dev/product/thinkpad/main.jpg',  1, 0),
(8,  3, 'https://img.cloudmall.dev/product/thinkpad/2.jpg',     0, 1),
(9,  4, 'https://img.cloudmall.dev/product/sony1000/main.jpg',  1, 0),
(10, 4, 'https://img.cloudmall.dev/product/sony1000/2.jpg',     0, 1),
(11, 5, 'https://img.cloudmall.dev/product/dress/main.jpg',     1, 0),
(12, 5, 'https://img.cloudmall.dev/product/dress/2.jpg',        0, 1),
(13, 5, 'https://img.cloudmall.dev/product/dress/3.jpg',        0, 2),
(14, 6, 'https://img.cloudmall.dev/product/shirt/main.jpg',     1, 0),
(15, 6, 'https://img.cloudmall.dev/product/shirt/2.jpg',        0, 1),
(16, 7, 'https://img.cloudmall.dev/product/nuts/main.jpg',      1, 0),
(17, 7, 'https://img.cloudmall.dev/product/nuts/2.jpg',         0, 1),
(18, 8, 'https://img.cloudmall.dev/product/beer/main.jpg',      1, 0),
(19, 8, 'https://img.cloudmall.dev/product/beer/2.jpg',         0, 1);

ALTER TABLE cm_product_image AUTO_INCREMENT = 1000;

-- ============================================================
-- 商品信息（SPU）
-- 列：id, name, spu_code, description, category_id, brand_id, image_id, shop_id, status
-- 价格/库存只存在于 SKU 表，SPU 不冗余
-- ============================================================
INSERT INTO cm_product (id, name, spu_code, description, category_id, brand_id, image_id, shop_id, status) VALUES
(1,  '华为 Mate 70 Pro',         'SPU20250001', '搭载麒麟芯片，超强影像系统，鸿蒙OS 5.0',             101, 1, 1,  1, 1),
(2,  'Apple iPhone 16 Pro Max', 'SPU20250002', 'A18 Pro 芯片，钛金属设计，4800万像素专业级拍摄',       101, 2, 4,  1, 1),
(3,  '联想 ThinkPad X1 Carbon', 'SPU20250003', '第14代酷睿Ultra，14英寸2.8K OLED屏，轻薄商务',         111, 3, 7,  1, 1),
(4,  'Sony WH-1000XM6 降噪耳机','SPU20250004', '旗舰级降噪，30小时续航，Hi-Res认证',                    121, 4, 9,  1, 1),
(5,  '女士蕾丝刺绣连衣裙',       'SPU20250005', '法式优雅蕾丝设计，修身显瘦，夏季新款',                  201, 5, 11, 2, 1),
(6,  '男士纯棉商务衬衫',         'SPU20250006', '免烫抗皱面料，经典修身版型，商务休闲两穿',              211, 5, 14, 2, 1),
(7,  '三只松鼠每日坚果礼盒',     'SPU20250007', '7种坚果混合，每日一包，营养均衡',                       301, 6, 16, 3, 1),
(8,  '青岛啤酒经典1903 500ml*12','SPU20250008', '百年经典工艺，麦香浓郁，纯正德国风味',                  31,  7, 18, 3, 1);

ALTER TABLE cm_product AUTO_INCREMENT = 1000;

-- ============================================================
-- 规格组（每商品独立维护）
-- ============================================================
INSERT INTO cm_product_spec_group (id, product_id, name, sort) VALUES
-- 华为 Mate 70 Pro（颜色 + 存储）
(1,  1, '颜色', 0),
(2,  1, '存储', 1),
-- iPhone 16 Pro Max（颜色 + 存储）
(3,  2, '颜色', 0),
(4,  2, '存储', 1),
-- ThinkPad X1 Carbon（单规格：1 组 × 1 值）
(5,  3, '配置', 0),
-- Sony 耳机（颜色）
(6,  4, '颜色', 0),
-- 连衣裙（尺码 + 颜色）
(7,  5, '尺码', 0),
(8,  5, '颜色', 1),
-- 衬衫（单规格）
(9,  6, '尺码', 0),
-- 每日坚果（单规格）
(10, 7, '规格', 0),
-- 青岛啤酒（单规格）
(11, 8, '规格', 0);

ALTER TABLE cm_product_spec_group AUTO_INCREMENT = 1000;

-- ============================================================
-- 规格值
-- ============================================================
INSERT INTO cm_product_spec_value (id, group_id, value, image, sort) VALUES
-- 华为 Mate 70 Pro：颜色
(1,  1, '曜金黑',   'https://img.cloudmall.dev/spec/color/gold-black.jpg', 0),
(2,  1, '雪域白',   'https://img.cloudmall.dev/spec/color/snow-white.jpg',  1),
(3,  1, '罗兰紫',   'https://img.cloudmall.dev/spec/color/purple.jpg',      2),
-- 华为 Mate 70 Pro：存储
(4,  2, '256GB', NULL, 0),
(5,  2, '512GB', NULL, 1),
(6,  2, '1TB',   NULL, 2),
-- iPhone 16 Pro Max：颜色
(7,  3, '原色钛金属', 'https://img.cloudmall.dev/spec/color/natural-ti.jpg', 0),
(8,  3, '白色钛金属', 'https://img.cloudmall.dev/spec/color/white-ti.jpg',   1),
-- iPhone 16 Pro Max：存储
(9,  4, '256GB', NULL, 0),
(10, 4, '512GB', NULL, 1),
(11, 4, '1TB',   NULL, 2),
-- ThinkPad X1 Carbon：配置
(12, 5, 'Ultra7 32G 1TB', NULL, 0),
-- Sony 耳机：颜色
(13, 6, '黑色',   'https://img.cloudmall.dev/spec/color/black.jpg',    0),
(14, 6, '铂金银', 'https://img.cloudmall.dev/spec/color/platinum.jpg', 1),
(15, 6, '午夜蓝', 'https://img.cloudmall.dev/spec/color/midnight.jpg', 2),
-- 连衣裙：尺码
(16, 7, 'S', NULL, 0),
(17, 7, 'M', NULL, 1),
(18, 7, 'L', NULL, 2),
-- 连衣裙：颜色
(19, 8, '米白色', 'https://img.cloudmall.dev/spec/color/rice-white.jpg', 0),
(20, 8, '樱花粉', 'https://img.cloudmall.dev/spec/color/sakura-pink.jpg', 1),
-- 衬衫：尺码（单规格）
(21, 9, 'M', NULL, 0),
-- 每日坚果：规格（单规格）
(22, 10, '750g/30包', NULL, 0),
-- 青岛啤酒：规格（单规格）
(23, 11, '500ml*12', NULL, 0);

ALTER TABLE cm_product_spec_value AUTO_INCREMENT = 1000;

-- ============================================================
-- 商品 SKU
-- spec_hash = 该 SKU 关联的规格值 ID 升序拼接（与 cm_product_sku_spec 严格一致）
-- sku_code / market_price / weight 为本模型新增字段
-- ============================================================
INSERT INTO cm_product_sku (id, product_id, sku_code, price, market_price, stock, weight, image, spec_hash) VALUES
-- 华为 Mate 70 Pro：曜金黑 256GB / 512GB / 1TB，雪域白 512GB
(1,  1, 'SKU1001', 6999.00,  7999.00,  30, 0.21, NULL, '1_4'),
(2,  1, 'SKU1002', 7999.00,  8999.00,  25, 0.21, NULL, '1_5'),
(3,  1, 'SKU1003', 9999.00,  10999.00, 10, 0.21, NULL, '1_6'),
(4,  1, 'SKU1004', 7999.00,  8999.00,  20, 0.21, NULL, '2_5'),
-- iPhone 16 Pro Max：原色钛金属 256GB / 512GB，白色钛金属 256GB
(5,  2, 'SKU1005', 9999.00,  10999.00, 25, 0.22, NULL, '7_9'),
(6,  2, 'SKU1006', 11999.00, 12999.00, 20, 0.22, NULL, '7_10'),
(7,  2, 'SKU1007', 9999.00,  10999.00, 15, 0.22, NULL, '8_9'),
-- ThinkPad X1 Carbon（单规格）
(8,  3, 'SKU1008', 12999.00, 14999.00, 50, 1.19, NULL, '12'),
-- Sony WH-1000XM6：黑色 / 铂金银 / 午夜蓝
(9,  4, 'SKU1009', 2999.00,  3499.00,  80, 0.25, NULL, '13'),
(10, 4, 'SKU1010', 2999.00,  3499.00,  60, 0.25, NULL, '14'),
(11, 4, 'SKU1011', 3099.00,  3599.00,  60, 0.25, NULL, '15'),
-- 连衣裙：S 米白 / M 米白 / M 樱花粉
(12, 5, 'SKU1012', 399.00,   599.00,   100, 0.40, NULL, '16_19'),
(13, 5, 'SKU1013', 399.00,   599.00,   100, 0.40, NULL, '17_19'),
(14, 5, 'SKU1014', 399.00,   599.00,   80,  0.40, NULL, '17_20'),
-- 衬衫（单规格）
(15, 6, 'SKU1015', 259.00,   359.00,   300, 0.30, NULL, '21'),
-- 每日坚果（单规格）
(16, 7, 'SKU1016', 89.90,    129.00,   600, 0.75, NULL, '22'),
-- 青岛啤酒（单规格）
(17, 8, 'SKU1017', 69.90,    89.90,    400, 7.20, NULL, '23');

ALTER TABLE cm_product_sku AUTO_INCREMENT = 1000;

-- ============================================================
-- SKU ↔ 规格值 关联（junction）
-- ============================================================
INSERT INTO cm_product_sku_spec (sku_id, spec_value_id) VALUES
(1, 1), (1, 4),
(2, 1), (2, 5),
(3, 1), (3, 6),
(4, 2), (4, 5),
(5, 7), (5, 9),
(6, 7), (6, 10),
(7, 8), (7, 9),
(8, 12),
(9, 13),
(10, 14),
(11, 15),
(12, 16), (12, 19),
(13, 17), (13, 19),
(14, 17), (14, 20),
(15, 21),
(16, 22),
(17, 23);

-- ============================================================
-- 首页轮播图（image 为 NOT NULL）
-- ============================================================
INSERT INTO cm_banner (id, title, image, link_url, sort, status) VALUES
(1, '华为 Mate 70 Pro 新品首发',   'https://img.cloudmall.dev/banner/1.jpg', '/product/1',  1, 1),
(2, '夏季清仓 全场低至5折',        'https://img.cloudmall.dev/banner/2.jpg', '/category/20', 2, 1),
(3, 'iPhone 16 Pro Max 限时特惠',  'https://img.cloudmall.dev/banner/3.jpg', '/product/2',  3, 1),
(4, '新用户注册即领100元优惠券',   'https://img.cloudmall.dev/banner/4.jpg', '/register', 4, 0);  -- 已下架

ALTER TABLE cm_banner AUTO_INCREMENT = 100;

-- ============================================================
-- 首页公告
-- ============================================================
INSERT INTO cm_notice (id, content, sort, status) VALUES
(1, '欢迎来到云商城，全场满 299 元包邮！', 1, 1),
(2, '新用户注册即领 100 元优惠券礼包',      2, 1),
(3, '每日 10 点整点秒杀，限量抢购先到先得', 3, 1),
(4, '平台维护公告（示例，已下线）',          4, 0);

ALTER TABLE cm_notice AUTO_INCREMENT = 100;

-- ============================================================
-- 用户收藏
-- ============================================================
INSERT INTO cm_favorite (id, user_id, product_id) VALUES
(1, 4, 1),
(2, 4, 4),
(3, 4, 7),
(4, 5, 2),
(5, 5, 6);

ALTER TABLE cm_favorite AUTO_INCREMENT = 100;

-- ============================================================
-- 商品评论
-- ============================================================
INSERT INTO cm_comment (id, product_id, user_id, order_id, order_item_id, shop_id, content, rating, images, useful_count, status, create_time, update_time) VALUES
(1, 1, 4, 1, 1, 1, '手机非常好用，拍照效果一流，鸿蒙系统超级流畅！', 5, NULL, 2, 1, NOW(), NOW()),
(2, 1, 5, 2, 6, 1, '用了两周，续航很给力，就是价格有点贵',        4, NULL, 1, 1, NOW(), NOW()),
(3, 4, 4, 1, 2, 1, '降噪效果惊艳，坐地铁完全听不到噪音，强烈推荐',  5, NULL, 3, 1, NOW(), NOW()),
(4, 5, 5, 2, 4, 2, '裙子很漂亮，材质舒适，尺码标准',              5, NULL, 0, 1, NOW(), NOW()),
(5, 7, 4, 1, 3, 3, '坚果很新鲜，每天一包很方便，会回购',          4, NULL, 1, 1, NOW(), NOW()),
(6, 6, 5, 2, 5, 2, '衬衫版型很好，免烫确实省心，值得购买',        5, NULL, 0, 1, NOW(), NOW());

ALTER TABLE cm_comment AUTO_INCREMENT = 1000;

-- ============================================================
-- 优惠券模板（新模型：平台券/店铺券双轨，发布后核心规则冻结）
-- ============================================================
-- 模板1-5 已发放；模板6 草稿；模板7 已暂停；模板8 已结束；模板9 可领取立减券
INSERT INTO cm_coupon (id, name, owner_type, shop_id, created_by, type, scope_type, threshold, discount_amount, discount_rate, total_count, claimed_count, issue_channel, validity_mode, issue_start_time, issue_end_time, fixed_start_time, fixed_expire_time, valid_days, status, first_issued_time) VALUES
(1, '新人注册礼（满100减20）',   'PLATFORM', NULL, 1, 'FULL_REDUCTION', 'ALL',      100.00,  20.00,  NULL,     NULL, 2, 'REGISTER_AUTO', 'RELATIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NULL, 30, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, '平台满500减50',            'PLATFORM', NULL, 1, 'FULL_REDUCTION', 'ALL',      500.00,  50.00,  NULL,     5000, 1, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'ACTIVE', NOW()),
(3, '数码分类9折券',            'PLATFORM', NULL, 1, 'DISCOUNT',       'CATEGORY', 0.00,    NULL,   0.100000, 1000, 1, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'ACTIVE', NOW()),
(4, '锐意数码满1000减120',      'SHOP',     1,   2, 'FULL_REDUCTION', 'ALL',      1000.00, 120.00, NULL,     200,  1, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'ACTIVE', NOW()),
(5, '芳华服饰满300减40',        'SHOP',     2,   3, 'FULL_REDUCTION', 'CATEGORY', 300.00,  40.00,  NULL,     1000, 2, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'ACTIVE', NOW()),
(6, '平台满200减30（草稿）',    'PLATFORM', NULL, 1, 'FULL_REDUCTION', 'ALL',      200.00,  30.00,  NULL,     1000, 0, 'PUBLIC_CLAIM', 'FIXED',    DATE_ADD(NOW(), INTERVAL 7 DAY),   DATE_ADD(NOW(), INTERVAL 37 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 97 DAY), NULL, 'DRAFT', NULL),
(7, '鲜享食品满50减10（已暂停）','SHOP',     3,   6, 'FULL_REDUCTION', 'ALL',      50.00,   10.00,  NULL,     500,  0, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'PAUSED', NULL),
(8, '平台满1000减100（已结束）', 'PLATFORM', NULL, 1, 'FULL_REDUCTION', 'PRODUCT',  1000.00, 100.00, NULL,     100,  0, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), NULL, 'ENDED', NULL),
(9, '平台无门槛立减5元',         'PLATFORM', NULL, 1, 'CASH',           'ALL',      0.00,    5.00,   NULL,     2000, 0, 'PUBLIC_CLAIM', 'FIXED',    DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, 'ACTIVE', NULL);

ALTER TABLE cm_coupon AUTO_INCREMENT = 100;

-- ============================================================
-- 优惠券范围关联（scope_type=ALL 不产生关联记录）
-- ============================================================
INSERT INTO cm_coupon_category (coupon_id, category_id) VALUES
(3, 1),   -- 数码9折券 → 数码电子（含全部后代分类）
(5, 20);  -- 芳华满300减40 → 服饰鞋包（含全部后代分类）

INSERT INTO cm_coupon_product (coupon_id, product_id) VALUES
(8, 1);   -- 已结束平台券 → 华为 Mate 70 Pro

-- ============================================================
-- 用户领取的优惠券（保存领取时核心快照）
-- ============================================================
INSERT INTO cm_user_coupon (id, user_id, coupon_id, owner_type, shop_id, name_snapshot, type_snapshot, scope_type_snapshot, threshold_snapshot, discount_amount_snapshot, discount_rate_snapshot, effective_time, expire_time, status, locked_order_no, lock_expire_time, state_time, issued_source, issued_event_id) VALUES
(1, 4, 1, 'PLATFORM', NULL, '新人注册礼（满100减20）',   'FULL_REDUCTION', 'ALL',      100.00,  20.00,  NULL,     DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 'AVAILABLE', NULL, NULL, NOW(), 'REGISTER_AUTO', 'EVT-UREG-000001'),
(2, 4, 2, 'PLATFORM', NULL, '平台满500减50',            'FULL_REDUCTION', 'ALL',      500.00,  50.00,  NULL,     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'USED',       NULL, NULL, NOW(), 'PUBLIC_CLAIM',  NULL),
(3, 4, 4, 'SHOP',     1,    '锐意数码满1000减120',      'FULL_REDUCTION', 'ALL',      1000.00, 120.00, NULL,     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'USED',       NULL, NULL, NOW(), 'PUBLIC_CLAIM',  NULL),
(4, 4, 5, 'SHOP',     2,    '芳华服饰满300减40',        'FULL_REDUCTION', 'CATEGORY', 300.00,  40.00,  NULL,     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'AVAILABLE', NULL, NULL, NOW(), 'PUBLIC_CLAIM',  NULL),
(5, 5, 1, 'PLATFORM', NULL, '新人注册礼（满100减20）',   'FULL_REDUCTION', 'ALL',      100.00,  20.00,  NULL,     DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_ADD(NOW(), INTERVAL 25 DAY), 'AVAILABLE', NULL, NULL, NOW(), 'REGISTER_AUTO', 'EVT-UREG-000002'),
(6, 5, 5, 'SHOP',     2,    '芳华服饰满300减40',        'FULL_REDUCTION', 'CATEGORY', 300.00,  40.00,  NULL,     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'USED',       NULL, NULL, NOW(), 'PUBLIC_CLAIM',  NULL),
(7, 5, 3, 'PLATFORM', NULL, '数码分类9折券',            'DISCOUNT',       'CATEGORY', 0.00,    NULL,   0.100000, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'AVAILABLE', NULL, NULL, NOW(), 'PUBLIC_CLAIM',  NULL);

ALTER TABLE cm_user_coupon AUTO_INCREMENT = 100;

-- ============================================================
-- 用户券状态流水（追加写入，event_id+operation_type 幂等）
-- ============================================================
INSERT INTO cm_user_coupon_log (id, user_coupon_id, coupon_id, user_id, order_no, event_id, operation_type, from_status, to_status, source, operator_id, reason) VALUES
(1,  1, 1, 4, NULL,              'EVT-UREG-000001',                'ISSUE',   NULL,       'AVAILABLE', 'SYSTEM',  NULL, '注册自动发放新人券'),
(2,  2, 2, 4, NULL,              NULL,                             'ISSUE',   NULL,       'AVAILABLE', 'USER',    4,    '公开领取'),
(3,  2, 2, 4, 'ORD202507010001', 'ORD202507010001-LOCK-000001',   'LOCK',    'AVAILABLE','LOCKED',    'ORDER',   NULL, '订单创建锁券'),
(4,  2, 2, 4, 'ORD202507010001', 'ORD202507010001-COMMIT-000001', 'COMMIT',  'LOCKED',   'USED',      'PAYMENT', NULL, '支付成功核销'),
(5,  3, 4, 4, NULL,              NULL,                             'ISSUE',   NULL,       'AVAILABLE', 'USER',    4,    '公开领取'),
(6,  3, 4, 4, 'ORD202507010001', 'ORD202507010001-LOCK-000002',   'LOCK',    'AVAILABLE','LOCKED',    'ORDER',   NULL, '订单创建锁券'),
(7,  3, 4, 4, 'ORD202507010001', 'ORD202507010001-COMMIT-000002', 'COMMIT',  'LOCKED',   'USED',      'PAYMENT', NULL, '支付成功核销'),
(8,  4, 5, 4, NULL,              NULL,                             'ISSUE',   NULL,       'AVAILABLE', 'USER',    4,    '公开领取'),
(9,  5, 1, 5, NULL,              'EVT-UREG-000002',                'ISSUE',   NULL,       'AVAILABLE', 'SYSTEM',  NULL, '注册自动发放新人券'),
(10, 6, 5, 5, NULL,              NULL,                             'ISSUE',   NULL,       'AVAILABLE', 'USER',    5,    '公开领取'),
(11, 6, 5, 5, 'ORD202507020002', 'ORD202507020002-LOCK-000001',   'LOCK',    'AVAILABLE','LOCKED',    'ORDER',   NULL, '订单创建锁券'),
(12, 6, 5, 5, 'ORD202507020002', 'ORD202507020002-COMMIT-000001', 'COMMIT',  'LOCKED',   'USED',      'PAYMENT', NULL, '支付成功核销'),
(13, 7, 3, 5, NULL,              NULL,                             'ISSUE',   NULL,       'AVAILABLE', 'USER',    5,    '公开领取');

ALTER TABLE cm_user_coupon_log AUTO_INCREMENT = 100;

-- ============================================================
-- 模板操作流水（追加写入）
-- ============================================================
INSERT INTO cm_coupon_operation_log (id, coupon_id, operator_id, operator_type, shop_id, from_status, to_status, action_type, change_summary, reason) VALUES
(1,  1, 1, 'OPERATOR', NULL, 'DRAFT',  'ACTIVE', 'PUBLISH', '新人券发布',        '运营发布平台新人券'),
(2,  2, 1, 'OPERATOR', NULL, 'DRAFT',  'ACTIVE', 'PUBLISH', '平台券发布',        '运营发布平台券'),
(3,  3, 1, 'OPERATOR', NULL, 'DRAFT',  'ACTIVE', 'PUBLISH', '平台券发布',        '运营发布数码分类折扣券'),
(4,  4, 2, 'MERCHANT', 1,    NULL,     'DRAFT',  'CREATE',  '创建店铺券草稿',    '商家创建店铺券'),
(5,  4, 2, 'MERCHANT', 1,    'DRAFT',  'ACTIVE', 'PUBLISH', '店铺券发布',        '商家发布店铺券'),
(6,  5, 3, 'MERCHANT', 2,    NULL,     'DRAFT',  'CREATE',  '创建店铺券草稿',    '商家创建店铺券'),
(7,  5, 3, 'MERCHANT', 2,    'DRAFT',  'ACTIVE', 'PUBLISH', '店铺券发布',        '商家发布店铺券'),
(8,  6, 1, 'OPERATOR', NULL, NULL,     'DRAFT',  'CREATE',  '创建平台券草稿',    '运营创建草稿模板'),
(9,  7, 6, 'MERCHANT', 3,    NULL,     'DRAFT',  'CREATE',  '创建店铺券草稿',    '商家创建店铺券'),
(10, 7, 6, 'MERCHANT', 3,    'DRAFT',  'ACTIVE', 'PUBLISH', '店铺券发布',        '商家发布店铺券'),
(11, 7, 6, 'MERCHANT', 3,    'ACTIVE', 'PAUSED', 'PAUSE',   '暂停发放',          '商家暂停发放'),
(12, 8, 1, 'OPERATOR', NULL, NULL,     'DRAFT',  'CREATE',  '创建平台券草稿',    '运营创建草稿模板'),
(13, 8, 1, 'OPERATOR', NULL, 'DRAFT',  'ACTIVE', 'PUBLISH', '平台券发布',        '运营发布平台券'),
(14, 8, 1, 'OPERATOR', NULL, 'ACTIVE', 'ENDED',  'END',     '结束发放',          '运营结束发放'),
(15, 9, 1, 'OPERATOR', NULL, 'DRAFT',  'ACTIVE', 'PUBLISH', '平台券发布',        '运营发布立减券');

ALTER TABLE cm_coupon_operation_log AUTO_INCREMENT = 100;

-- ============================================================
-- 父订单（金额为快照：原价总额/店铺券优惠/平台券优惠/商品应付/运费/最终应付）
-- 订单1 使用店铺券+平台券；订单2 仅店铺券；订单3 无券待支付
-- ============================================================
INSERT INTO cm_order (id, order_no, user_id, total_original_amount, total_shop_discount_amount, total_platform_discount_amount, goods_payable_amount, freight_amount, payable_amount, status, address_snapshot, remark) VALUES
(1, 'ORD202507010001', 4, 11087.90, 120.00, 50.00, 10917.90, 0.00, 10917.90, 'PAID',
    '{"consignee":"张三","phone":"13800000004","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园南区A栋1201"}',
    '请尽快发货'),
(2, 'ORD202507020002', 5, 658.00,   40.00,  0.00,  618.00,   0.00, 618.00,   'PAID',
    '{"consignee":"赵六","phone":"13800000005","province":"北京市","city":"北京市","district":"朝阳区","detail":"望京SOHO T3 2204"}',
    NULL),
(3, 'ORD202507030003', 4, 89.90,    0.00,   0.00,  89.90,    0.00, 89.90,    'PENDING_PAYMENT',
    '{"consignee":"张三","phone":"13800000004","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园南区A栋1201"}',
    NULL);

ALTER TABLE cm_order AUTO_INCREMENT = 1000;

-- ============================================================
-- 店铺子订单（每父订单每店铺一条，平台优惠按子订单分摊）
-- ============================================================
INSERT INTO cm_order_sub (id, sub_order_no, parent_order_id, shop_id, original_amount, shop_discount_amount, platform_discount_amount, payable_amount, merchant_settlement_amount, status) VALUES
(1, 'SUB202507010001', 1, 1, 10998.00, 120.00, 49.59, 10828.41, 10828.41, 'PENDING_SHIPMENT'),
(2, 'SUB202507010002', 1, 3, 89.90,    0.00,   0.41,  89.49,    89.49,    'PENDING_SHIPMENT'),
(3, 'SUB202507020001', 2, 2, 658.00,   40.00,  0.00,  618.00,   618.00,   'SHIPPED'),
(4, 'SUB202507030001', 3, 3, 89.90,    0.00,   0.00,  89.90,    89.90,    'PENDING_SHIPMENT');

ALTER TABLE cm_order_sub AUTO_INCREMENT = 1000;

-- ============================================================
-- 订单明细（归属于子订单，保存金额快照与优惠分摊）
-- ============================================================
INSERT INTO cm_order_item (id, sub_order_id, product_id, sku_id, shop_id, category_id, product_name, product_image, spec_snapshot, unit_price, quantity, original_amount, shop_discount_amount, platform_discount_amount, payable_amount) VALUES
-- 订单1 子订单1（锐意数码）：Mate 70 Pro 曜金黑512GB + Sony WH-1000XM6 黑色
(1, 1, 1, 2,  1, 101, '华为 Mate 70 Pro（曜金黑 512GB）', NULL, NULL, 7999.00, 1, 7999.00, 87.28, 36.07, 7875.65),
(2, 1, 4, 9,  1, 121, 'Sony WH-1000XM6 降噪耳机（黑色）', NULL, NULL, 2999.00, 1, 2999.00, 32.72, 13.52, 2952.76),
-- 订单1 子订单2（鲜享食品）：每日坚果
(3, 2, 7, 16, 3, 301, '三只松鼠每日坚果礼盒',            NULL, NULL, 89.90,   1, 89.90,   0.00,   0.41,  89.49),
-- 订单2 子订单3（芳华服饰）：连衣裙 M码米白色 + 衬衫
(4, 3, 5, 13, 2, 201, '女士蕾丝刺绣连衣裙（M 米白色）',   NULL, NULL, 399.00,  1, 399.00,  24.26,  0.00,  374.74),
(5, 3, 6, 15, 2, 211, '男士纯棉商务衬衫',                 NULL, NULL, 259.00,  1, 259.00,  15.74,  0.00,  243.26),
-- 订单3 子订单4（鲜享食品）：每日坚果（待支付）
(6, 4, 7, 16, 3, 301, '三只松鼠每日坚果礼盒',            NULL, NULL, 89.90,   1, 89.90,   0.00,   0.00,  89.90);

ALTER TABLE cm_order_item AUTO_INCREMENT = 1000;

-- ============================================================
-- 订单优惠关联（平台券挂父订单，店铺券挂子订单）
-- ============================================================
INSERT INTO cm_order_coupon (id, parent_order_id, sub_order_id, user_coupon_id, coupon_id, owner_type, shop_id, type_snapshot, threshold_snapshot, discount_amount_snapshot, discount_rate_snapshot, applicable_amount, discount_amount) VALUES
(1, 1, 1,    3, 4, 'SHOP',     1,    'FULL_REDUCTION', 1000.00, 120.00, NULL,     10998.00, 120.00),
(2, 1, NULL, 2, 2, 'PLATFORM', NULL, 'FULL_REDUCTION', 500.00,  50.00,  NULL,     10967.90, 50.00),
(3, 2, 3,    6, 5, 'SHOP',     2,    'FULL_REDUCTION', 300.00,  40.00,  NULL,     658.00,   40.00);

ALTER TABLE cm_order_coupon AUTO_INCREMENT = 1000;

-- ============================================================
-- 购物车（shop_id 冗余自商品所属店铺，便于按店聚合）
-- ============================================================
INSERT INTO cm_cart (id, user_id, shop_id, product_id, sku_id, product_name, product_image, price, quantity, selected) VALUES
(1, 4, 1, 2, 5,  'Apple iPhone 16 Pro Max（原色钛金属 256GB）', NULL, 9999.00, 1, 1),
(2, 4, 2, 6, 15, '男士纯棉商务衬衫',                          NULL, 259.00,  2, 1),
(3, 5, 1, 1, 1,  '华为 Mate 70 Pro（曜金黑 256GB）',           NULL, 6999.00, 1, 1),
(4, 5, 3, 8, 17, '青岛啤酒经典1903 500ml*12',                  NULL, 69.90,   2, 0);

ALTER TABLE cm_cart AUTO_INCREMENT = 1000;

-- 补充购物车数据（覆盖张三/赵六的多店铺混合场景，sku/价格与 cm_product_sku 严格一致）
INSERT INTO cm_cart (id, user_id, shop_id, product_id, sku_id, product_name, product_image, price, quantity, selected) VALUES
(5,  4, 1, 1, 1,  '华为 Mate 70 Pro（曜金黑 256GB）',           'https://img.cloudmall.dev/product/mate70/main.jpg',   6999.00,  1, 1),
(6,  4, 1, 3, 8,  '联想 ThinkPad X1 Carbon（Ultra7 32G 1TB）',  'https://img.cloudmall.dev/product/thinkpad/main.jpg', 12999.00, 1, 1),
(7,  4, 3, 7, 16, '三只松鼠每日坚果礼盒（750g/30包）',          'https://img.cloudmall.dev/product/nuts/main.jpg',     89.90,    4, 0),
(8,  5, 1, 2, 7,  'Apple iPhone 16 Pro Max（白色钛金属 256GB）', 'https://img.cloudmall.dev/product/iphone16/main.jpg', 9999.00,  2, 1),
(9,  5, 1, 4, 9,  'Sony WH-1000XM6 降噪耳机（黑色）',            'https://img.cloudmall.dev/product/sony1000/main.jpg', 2999.00,  1, 0),
(10, 5, 2, 5, 12, '女士蕾丝刺绣连衣裙（S 米白色）',              'https://img.cloudmall.dev/product/dress/main.jpg',    399.00,   1, 1);

-- ============================================================
-- 秒杀活动（时长固定1小时；DRAFT/PUBLISHED 满足开始时间晚于当前24小时）
-- ============================================================
INSERT INTO cm_seckill_activity (id, name, description, start_time, end_time, status, create_user_id, publish_user_id, publish_time) VALUES
(1, '618数码狂欢节',         '数码爆品限时秒杀，先到先得', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'RUNNING',   1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, '暑期清凉特卖',           '夏日服饰食品专场报名中',     DATE_ADD(NOW(), INTERVAL 2 DAY),     DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR, 'PUBLISHED', 1, 1, NOW()),
(3, '618服饰专场（已结束）', '春季服饰清仓',               DATE_SUB(NOW(), INTERVAL 30 DAY),    DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 1 HOUR, 'FINISHED',  1, 1, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(4, '中秋食品专场',           '运营排期中，尚未开放报名',   DATE_ADD(NOW(), INTERVAL 5 DAY),     DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR,  'DRAFT',     1, NULL, NULL);

ALTER TABLE cm_seckill_activity AUTO_INCREMENT = 100;

-- ============================================================
-- 秒杀商品（按 SKU 报名，冗余 product_id / shop_id；秒杀价低于 SKU 原价）
-- ============================================================
INSERT INTO cm_seckill_product (id, activity_id, product_id, sku_id, shop_id, seckill_price, stock, sold_count, audit_status) VALUES
-- 活动1（RUNNING）：数码专场
(1, 1, 1, 1,  1, 5999.00, 10, 3,  'APPROVED'),
(2, 1, 4, 9,  1, 2499.00, 20, 5,  'APPROVED'),
(3, 1, 7, 16, 3, 69.90,   50, 12, 'APPROVED'),
-- 活动2（PUBLISHED 报名期）：待审 + 已审 + 驳回各一条
(4, 2, 5, 13, 2, 299.00,  30, 0,  'PENDING'),
(5, 2, 6, 15, 2, 199.00,  40, 0,  'APPROVED'),
(6, 2, 8, 17, 3, 49.90,   60, 0,  'REJECTED'),
-- 活动3（FINISHED）：历史数据
(7, 3, 5, 12, 2, 299.00,  30, 28, 'APPROVED'),
(8, 3, 6, 15, 2, 199.00,  40, 35, 'APPROVED');

ALTER TABLE cm_seckill_product AUTO_INCREMENT = 100;

-- ============================================================
-- 通用审核单（当前仅秒杀商品审核场景）
-- ============================================================
INSERT INTO cm_audit_record (id, biz_type, biz_id, submitter_id, auditor_id, status, audit_remark, extra_info) VALUES
(1, 'SECKILL_PRODUCT', 4, 3, NULL, 'PENDING',  NULL,               '{"skuId":13,"seckillPrice":299.00,"stock":30}'),
(2, 'SECKILL_PRODUCT', 5, 3, 1,    'APPROVED', '价格与库存符合要求', '{"skuId":15,"seckillPrice":199.00,"stock":40}'),
(3, 'SECKILL_PRODUCT', 6, 6, 1,    'REJECTED', '秒杀力度不足，请调整价格', '{"skuId":17,"seckillPrice":49.90,"stock":60}');

ALTER TABLE cm_audit_record AUTO_INCREMENT = 100;

-- ============================================================
-- 秒杀购买记录（一人一单，order_no 为秒杀侧预生成的真实订单号）
-- ============================================================
INSERT INTO cm_seckill_order (id, activity_id, product_id, sku_id, user_id, quantity, seckill_price, order_no, status) VALUES
(1, 1, 1, 1,  4, 1, 5999.00, 'SK202507010000001', 'PAID'),
(2, 1, 4, 9,  5, 1, 2499.00, 'SK202507010000002', 'PENDING_PAYMENT'),
(3, 3, 5, 12, 4, 1, 299.00,  'SK202506010000003', 'CANCELLED');

-- ============================================================
-- AI 聊天会话（示例）
-- ============================================================
INSERT INTO cm_chat_session (id, session_id, user_id, title) VALUES
(1, UUID(), 4, '商品推荐咨询'),
(2, UUID(), 4, '手机选购建议'),
(3, UUID(), 5, '订单问题咨询');

ALTER TABLE cm_chat_session AUTO_INCREMENT = 1000;

-- ============================================================
-- 普通商品库存操作流水（订单创建预占、支付确认扣减；秒杀库存不接入本表）
-- before_stock / after_stock 按时间线连续推演：支付确认不改变可用库存，仅固化预占
-- （RELEASE=超时/取消释放、COMPENSATE=补偿任务释放，当前订单无超时/取消，故未出现）
-- ============================================================
INSERT INTO cm_stock_op_log (id, order_no, sku_id, product_id, shop_id, op_type, quantity, before_stock, after_stock, source, operator_id, reason, create_time) VALUES
-- 订单1（已支付）：Mate 70 Pro 曜金黑512GB（SKU2）+ Sony耳机黑色（SKU9）+ 每日坚果（SKU16）
(1,  'ORD202507010001', 2,  1, 1, 'RESERVE', 1, 25,  24,  'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2,  'ORD202507010001', 2,  1, 1, 'CONFIRM', 1, 24,  24,  'ORDER', NULL, '支付成功确认扣减',    DATE_SUB(NOW(), INTERVAL 29 DAY)),
(3,  'ORD202507010001', 9,  4, 1, 'RESERVE', 1, 80,  79,  'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4,  'ORD202507010001', 9,  4, 1, 'CONFIRM', 1, 79,  79,  'ORDER', NULL, '支付成功确认扣减',    DATE_SUB(NOW(), INTERVAL 29 DAY)),
(5,  'ORD202507010001', 16, 7, 3, 'RESERVE', 1, 600, 599, 'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 30 DAY)),
(6,  'ORD202507010001', 16, 7, 3, 'CONFIRM', 1, 599, 599, 'ORDER', NULL, '支付成功确认扣减',    DATE_SUB(NOW(), INTERVAL 29 DAY)),
-- 订单2（已支付）：连衣裙 M码米白（SKU13）+ 衬衫（SKU15）
(7,  'ORD202507020002', 13, 5, 2, 'RESERVE', 1, 100, 99,  'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 28 DAY)),
(8,  'ORD202507020002', 13, 5, 2, 'CONFIRM', 1, 99,  99,  'ORDER', NULL, '支付成功确认扣减',    DATE_SUB(NOW(), INTERVAL 27 DAY)),
(9,  'ORD202507020002', 15, 6, 2, 'RESERVE', 1, 300, 299, 'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 28 DAY)),
(10, 'ORD202507020002', 15, 6, 2, 'CONFIRM', 1, 299, 299, 'ORDER', NULL, '支付成功确认扣减',    DATE_SUB(NOW(), INTERVAL 27 DAY)),
-- 订单3（待支付）：每日坚果（SKU16）仅预占，支付后由 CONFIRM 固化
(11, 'ORD202507030003', 16, 7, 3, 'RESERVE', 1, 599, 598, 'ORDER', NULL, '订单创建预占库存',    DATE_SUB(NOW(), INTERVAL 1 DAY));

ALTER TABLE cm_stock_op_log AUTO_INCREMENT = 100;

-- ============================================================
-- 通用本地消息 Outbox（app_name 为来源服务，event_id 为全局幂等键）
-- 事件 ID 与 cm_user_coupon_log.issued_event_id 对齐；payload 结构参照各服务事件实体
-- ============================================================
INSERT INTO cm_message_outbox (id, app_name, event_id, topic, biz_type, biz_id, event_type, payload, publish_status, retry_count, next_retry_time, publish_time, last_error, create_time, update_time) VALUES
-- 用户注册事件（user-service → coupon-service 注册自动发券）
(1,  'user-service',    'EVT-UREG-000001', 'USER_REGISTERED', 'USER', '4', 'UserRegistered',
    '{"eventId":"EVT-UREG-000001","userId":4,"username":"user1"}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2,  'user-service',    'EVT-UREG-000002', 'USER_REGISTERED', 'USER', '5', 'UserRegistered',
    '{"eventId":"EVT-UREG-000002","userId":5,"username":"user2"}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY),  NULL, DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 订单支付事件（order-service → coupon-service 核销 / seckill-service 确认秒杀单）
(3,  'order-service',   'EVT-ORD-PAID-000001', 'ORDER_EVENTS', 'ORDER', 'ORD202507010001', 'OrderPaid',
    '{"eventId":"EVT-ORD-PAID-000001","orderNo":"ORD202507010001","userId":4}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 29 DAY), NULL, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
(4,  'order-service',   'EVT-ORD-PAID-000002', 'ORDER_EVENTS', 'ORDER', 'ORD202507020002', 'OrderPaid',
    '{"eventId":"EVT-ORD-PAID-000002","orderNo":"ORD202507020002","userId":5}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 27 DAY), NULL, DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY)),
-- 秒杀抢购事件（seckill-service → consumer-service 异步建真实订单）
(5,  'seckill-service', 'EVT-SK-ORDER-000001', 'SECKILL_ORDERS', 'SECKILL_ORDER', 'SK202507010000001', 'SeckillOrderCreate',
    '{"eventId":"EVT-SK-ORDER-000001","seckillOrderId":1,"orderNo":"SK202507010000001","userId":4,"skuId":1,"productId":1,"seckillPrice":5999.00,"quantity":1,"addressId":1}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(6,  'seckill-service', 'EVT-SK-ORDER-000002', 'SECKILL_ORDERS', 'SECKILL_ORDER', 'SK202507010000002', 'SeckillOrderCreate',
    '{"eventId":"EVT-SK-ORDER-000002","seckillOrderId":2,"orderNo":"SK202507010000002","userId":5,"skuId":9,"productId":4,"seckillPrice":2499.00,"quantity":1,"addressId":3}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(7,  'seckill-service', 'EVT-SK-ORDER-000003', 'SECKILL_ORDERS', 'SECKILL_ORDER', 'SK202506010000003', 'SeckillOrderCreate',
    '{"eventId":"EVT-SK-ORDER-000003","seckillOrderId":3,"orderNo":"SK202506010000003","userId":4,"skuId":12,"productId":5,"seckillPrice":299.00,"quantity":1,"addressId":1}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 60 DAY), NULL, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY)),
-- 商品变更事件（product-service → search-service 同步 ES）
(8,  'product-service', 'EVT-PROD-000001', 'PRODUCT_EVENTS', 'PRODUCT', '2', 'ProductUpdated',
    '{"eventId":"EVT-PROD-000001","productId":2,"spuCode":"SPU20250002"}', 'PUBLISHED', 0, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(9,  'product-service', 'EVT-PROD-000002', 'PRODUCT_EVENTS', 'PRODUCT', '5', 'ProductUpdated',
    '{"eventId":"EVT-PROD-000002","productId":5,"spuCode":"SPU20250005"}', 'PENDING', 3, DATE_ADD(NOW(), INTERVAL 5 MINUTE), NULL, 'MQ 连接超时，等待重试', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(10, 'product-service', 'EVT-PROD-000003', 'PRODUCT_EVENTS', 'PRODUCT', '7', 'ProductDeleted',
    '{"eventId":"EVT-PROD-000003","productId":7,"spuCode":"SPU20250007"}', 'DEAD', 5, NULL, NULL, '超过最大重试次数，转为死信', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

ALTER TABLE cm_message_outbox AUTO_INCREMENT = 100;

-- ============================================================
-- 消息消费幂等记录（event_id + consumer + operation_type 唯一约束）
-- consumer 取消费者组名；operation_type 词汇与 consumer-service OperationTypeEnum 对齐
-- ============================================================
INSERT INTO cm_message_consume_record (id, event_id, consumer, operation_type, status, retry_count, last_error, consume_time, create_time, update_time) VALUES
(1, 'EVT-UREG-000001',      'coupon-user-registered', 'REGISTER_AUTO_ISSUE', 'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 'EVT-UREG-000002',      'coupon-user-registered', 'REGISTER_AUTO_ISSUE', 'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'EVT-ORD-PAID-000001',  'coupon-order-events',    'ORDER_PAID',          'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),
(4, 'EVT-ORD-PAID-000002',  'coupon-order-events',    'ORDER_PAID',          'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY)),
(5, 'EVT-SK-ORDER-000001',  'seckill-order-events',   'SECKILL_ORDER_CREATE', 'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(6, 'EVT-SK-ORDER-000002',  'seckill-order-events',   'SECKILL_ORDER_CREATE', 'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(7, 'EVT-SK-ORDER-000003',  'seckill-order-events',   'SECKILL_ORDER_CREATE', 'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY)),
(8, 'EVT-PROD-000001',      'search-product-events',  'PRODUCT_UPSERT',      'CONSUMED', 0, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(9, 'EVT-PROD-000003',      'search-product-events',  'PRODUCT_DELETE',      'DEAD',     5, 'ES 索引删除失败：连接超时', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

ALTER TABLE cm_message_consume_record AUTO_INCREMENT = 100;

-- ============================================================
-- IM 聊天会话（买家 ↔ 商家，uk_buyer_merchant 唯一）
-- ============================================================
INSERT INTO cm_im_session (id, buyer_id, merchant_id, create_time, update_time) VALUES
(1, 4, 2, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),   -- 张三 ↔ 锐意数码（李明）
(2, 5, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),   -- 赵六 ↔ 锐意数码（李明）
(3, 4, 6, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));   -- 张三 ↔ 鲜享食品（陈刚）

ALTER TABLE cm_im_session AUTO_INCREMENT = 100;

-- ============================================================
-- IM 聊天消息（text / image 两类，is_read=0 表示对方未读）
-- ============================================================
INSERT INTO cm_im_message (id, session_id, sender_id, content, type, is_read, create_time) VALUES
-- 会话1：张三咨询 Mate 70 Pro 发货
(1,  1, 4, '你好，请问 Mate 70 Pro 什么时候能发货？',                'text',  1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2,  1, 2, '您好，您的订单已支付，今天下午统一发出',                  'text',  1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3,  1, 4, '好的，谢谢！',                                           'text',  1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4,  1, 2, '不客气，收到货后有任何问题随时联系～',                   'text',  0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 会话2：赵六咨询 Sony 耳机
(5,  2, 5, '索尼 WH-1000XM6 还有铂金银色的吗？',                     'text',  1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6,  2, 2, '有的，现货充足，下单后 24 小时内发出',                   'text',  1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7,  2, 5, '好的，那我去下单了',                                     'text',  1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 会话3：张三咨询坚果礼盒
(8,  3, 4, '每日坚果礼盒的保质期到什么时候？',                       'text',  1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9,  3, 6, '保质期到明年 6 月，日期很新鲜，放心购买',                'text',  1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 3, 6, 'https://img.cloudmall.dev/im/receipt.jpg',               'image', 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

ALTER TABLE cm_im_message AUTO_INCREMENT = 100;
