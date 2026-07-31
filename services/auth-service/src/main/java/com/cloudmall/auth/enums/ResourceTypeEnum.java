package com.cloudmall.auth.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 资源类型枚举
 * <p>
 * 定义系统中所有合法的资源（权限）类型。
 * 资源同时承担前端菜单和后端权限标识双重职责：
 * <ul>
 *   <li>{@link #MENU} — 菜单项，对应具体的功能页面，通常绑定权限编码</li>
 *   <li>{@link #CATALOG} — 目录，作为菜单分组容器，不直接绑定权限</li>
 *   <li>{@link #LAYOUT} — 布局页，承载子页面，不直接绑定权限</li>
 *   <li>{@link #BUTTON} — 按钮级权限，仅作为权限标识，不参与菜单树</li>
 * </ul>
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
public enum ResourceTypeEnum {

    /** 菜单项 — 功能页面，通常绑定权限编码 */
    MENU("menu"),

    /** 目录 — 菜单分组容器 */
    CATALOG("catalog"),

    /** 布局页 — 承载子页面 */
    LAYOUT("layout"),

    /** 按钮级权限 — 仅作为权限标识，不参与菜单树 */
    BUTTON("button");

    private final String code;

    ResourceTypeEnum(String code) {
        this.code = code;
    }

    /**
     * 获取资源类型编码（数据库存储值）
     *
     * @return 编码字符串，如 "menu"、"catalog"
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取参与菜单树构建的 UI 资源类型
     * <p>
     * 仅包含 {@link #MENU}、{@link #CATALOG}、{@link #LAYOUT}，
     * 排除 {@link #BUTTON}。用于构建前端可导航的菜单树。
     *
     * @return UI 资源类型列表（不可变）
     */
    public static List<ResourceTypeEnum> uiTypes() {
        return List.of(MENU, CATALOG, LAYOUT);
    }

    /**
     * 获取 UI 资源类型的编码字符串列表
     * <p>
     * 便捷方法，直接返回 code 值列表，用于 MyBatis-Plus IN 查询。
     *
     * @return UI 资源类型编码列表，如 ["menu", "catalog", "layout"]
     */
    public static List<String> uiTypeCodes() {
        return List.of(MENU.code, CATALOG.code, LAYOUT.code);
    }

    /**
     * 获取所有合法的资源类型
     *
     * @return 全部资源类型列表（不可变）
     */
    public static List<ResourceTypeEnum> allTypes() {
        return List.of(values());
    }

    /**
     * 获取所有合法资源类型的编码字符串列表
     *
     * @return 全部资源类型编码列表，如 ["menu", "catalog", "layout", "button"]
     */
    public static List<String> allTypeCodes() {
        return Arrays.stream(values())
                .map(ResourceTypeEnum::getCode)
                .toList();
    }

    /**
     * 根据编码查找资源类型
     *
     * @param code 编码字符串，如 "menu"
     * @return 匹配的 ResourceTypeEnum
     * @throws IllegalArgumentException 无匹配的类型时抛出
     */
    public static ResourceTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid resource type: '" + code + "', valid values: " + allTypeCodes()
                ));
    }

    /**
     * 判断指定编码是否为合法的资源类型
     *
     * @param code 编码字符串
     * @return true 为合法类型
     */
    public static boolean isValid(String code) {
        return Arrays.stream(values()).anyMatch(t -> t.code.equals(code));
    }

    /**
     * 判断指定编码是否为 UI 资源类型（参与菜单树）
     *
     * @param code 编码字符串
     * @return true 为 UI 资源类型
     */
    public static boolean isUiType(String code) {
        return uiTypeCodes().contains(code);
    }

    /**
     * 转换对象数据。
     *
     * @return 字符串结果
     */
    @Override
    public String toString() {
        return code;
    }
}
