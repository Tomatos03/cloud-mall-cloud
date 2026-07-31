-- ============================================================
-- cm_mall — 种子数据（供开发 / 测试环境使用）
-- 执行前请先执行 cm_mall.sql 初始化表结构
-- 与 cm_mall.sql 当前表结构严格对齐（cm_product_* 多规格模型 + cm_shop + cm_brand）
-- ============================================================

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
-- 品牌（全局独立表，商品通过 brand_id 引用）
-- ============================================================
INSERT INTO cm_brand (id, name, name_en, logo, description, country, website, status, sort_order) VALUES
(1, '华为',     'HUAWEI',   NULL, '全球领先的ICT基础设施和智能终端提供商', '中国', 'https://www.huawei.com',   1, 1),
(2, 'Apple',    'Apple',    NULL, '全球科技公司，iPhone、Mac 等产品',       '美国', 'https://www.apple.com',    1, 2),
(3, '联想',     'Lenovo',   NULL, '全球个人电脑领导品牌',                  '中国', 'https://www.lenovo.com',   1, 3),
(4, 'Sony',     'SONY',     NULL, '音频、影像与游戏娱乐专家',              '日本', 'https://www.sony.com',     1, 4),
(5, '芳华',     'FANGHUA',  NULL, '原创设计女装/男装品牌',                 '中国', NULL,                        1, 5),
(6, '三只松鼠', 'Three Squirrels', NULL, '国民零食品牌',                  '中国', 'https://www.3songshu.com', 1, 6),
(7, '青岛啤酒', 'TSINGTAO', NULL, '百年经典啤酒品牌',                      '中国', 'https://www.tsingtao.com.cn', 1, 7);

ALTER TABLE cm_brand AUTO_INCREMENT = 100;

-- ============================================================
-- 商品图片（首张为主图 is_main=1，其余按 sort_order 轮播）
-- cm_product.image_id 指向各商品的主图
-- ============================================================
INSERT INTO cm_product_image (id, product_id, url, is_main, sort_order) VALUES
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
INSERT INTO cm_product_spec_group (id, product_id, name, sort_order) VALUES
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
INSERT INTO cm_product_spec_value (id, group_id, value, image, sort_order) VALUES
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
INSERT INTO cm_banner (id, title, image, link_url, sort_order, status) VALUES
(1, '华为 Mate 70 Pro 新品首发',   'https://img.cloudmall.dev/banner/1.jpg', '/product/1',  1, 1),
(2, '夏季清仓 全场低至5折',        'https://img.cloudmall.dev/banner/2.jpg', '/category/20', 2, 1),
(3, 'iPhone 16 Pro Max 限时特惠',  'https://img.cloudmall.dev/banner/3.jpg', '/product/2',  3, 1),
(4, '新用户注册即领100元优惠券',   'https://img.cloudmall.dev/banner/4.jpg', '/register', 4, 0);  -- 已下架

ALTER TABLE cm_banner AUTO_INCREMENT = 100;

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
INSERT INTO cm_comment (id, product_id, user_id, order_id, content, rating, images, status) VALUES
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
-- 订单（total_amount 与明细小计一致）
-- ============================================================
INSERT INTO cm_order (id, order_no, user_id, total_amount, status, address_snapshot, remark) VALUES
(1, 'ORD202507010001', 4, 11087.90, 'PAID',
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
-- 订单明细（product_name / product_image 为快照列）
-- ============================================================
INSERT INTO cm_order_item (id, order_id, product_id, sku_id, product_name, product_image, price, quantity, subtotal) VALUES
-- 订单1：华为 Mate 70 Pro 曜金黑 512GB + Sony WH-1000XM6 黑色 + 每日坚果
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
-- 购物车（shop_id 冗余自商品所属店铺，便于按店聚合）
-- ============================================================
INSERT INTO cm_cart (id, user_id, shop_id, product_id, sku_id, product_name, product_image, price, quantity, selected) VALUES
(1, 4, 1, 2, 5,  'Apple iPhone 16 Pro Max（原色钛金属 256GB）', NULL, 9999.00, 1, 1),
(2, 4, 2, 6, 15, '男士纯棉商务衬衫',                          NULL, 259.00,  2, 1),
(3, 5, 1, 1, 1,  '华为 Mate 70 Pro（曜金黑 256GB）',           NULL, 6999.00, 1, 1),
(4, 5, 3, 8, 17, '青岛啤酒经典1903 500ml*12',                  NULL, 69.90,   2, 0);

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
-- 秒杀商品（表名与 schema 对齐：cm_seckill_product）
-- ============================================================
INSERT INTO cm_seckill_product (id, activity_id, product_id, seckill_price, stock, sold_count, audit_status) VALUES
(1, 1, 1, 5999.00,  10, 3, 'APPROVED'),
(2, 1, 4, 2499.00,  20, 5, 'APPROVED'),
(3, 1, 7, 69.90,    50, 12, 'APPROVED'),
(4, 3, 5, 299.00,   30, 28, 'APPROVED'),
(5, 3, 6, 199.00,   40, 35, 'APPROVED');

ALTER TABLE cm_seckill_product AUTO_INCREMENT = 100;

-- ============================================================
-- AI 聊天会话（示例）
-- ============================================================
INSERT INTO cm_chat_session (id, session_id, user_id, title) VALUES
(1, UUID(), 4, '商品推荐咨询'),
(2, UUID(), 4, '手机选购建议'),
(3, UUID(), 5, '订单问题咨询');

ALTER TABLE cm_chat_session AUTO_INCREMENT = 1000;
