package com.cloudmall.common.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;

/**
 * 断言工具类
 * 用于在业务逻辑中快速验证条件，当条件不满足时抛出对应的业务异常
 *
 * @author : Tomatos
 * @date : 2026/02/12
 */
public class AssertUtils {

    private AssertUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 断言表达式为真，如果为假则抛出业务异常
     *
     * @param expression 待验证的表达式
     * @param errorCode  条件不满足时抛出的错误码
     * @throws BizException 当表达式为假时抛出
     */
    public static void isTrue(boolean expression, BizErrorCode errorCode) {
        if (!expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言表达式为假，如果为真则抛出业务异常
     *
     * @param expression 待验证的表达式
     * @param errorCode  条件不满足时抛出的错误码
     * @throws BizException 当表达式为真时抛出
     */
    public static void isFalse(boolean expression, BizErrorCode errorCode) {
        if (expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言对象不为空，如果为空则抛出业务异常
     *
     * @param obj       待验证的对象
     * @param errorCode 对象为空时抛出的错误码
     * @throws BizException 当对象为空时抛出
     */
    public static void notNull(Object obj, BizErrorCode errorCode) {
        if (obj == null) {
            throw new BizException(errorCode);
        }
    }

    public static <X extends RuntimeException> void notNull(Object obj, Supplier<X> exceptionSupplier) {
        if (obj == null) {
            throw exceptionSupplier.get();
        }
    }

    public static void isNull(Object obj, BizErrorCode errorCode) {
        if (obj != null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言对象为空，如果不为空则抛出业务异常
     *
     * @param obj       待验证的对象
     * @param errorCode 对象不为空时抛出的错误码
     * @throws BizException 当对象不为空时抛出
     */
    public static void assertNull(Object obj, BizErrorCode errorCode) {
        if (obj != null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言字符串不为空且不为空字符串，如果为空则抛出业务异常
     *
     * @param str       待验证的字符串
     * @param errorCode 字符串为空或空字符串时抛出的错误码
     * @throws BizException 当字符串为空或空字符串时抛出
     */
    public static void assertNotBlank(String str, BizErrorCode errorCode) {
        if (str == null || str.trim()
                              .isEmpty()) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言两个对象相等，如果不相等则抛出业务异常
     *
     * @param obj1      第一个对象
     * @param obj2      第二个对象
     * @param errorCode 对象不相等时抛出的错误码
     * @throws BizException 当两个对象不相等时抛出
     */
    public static void isEqual(Object obj1, Object obj2, BizErrorCode errorCode) {
        if (!Objects.equals(obj1, obj2)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言两个对象不相等，如果相等则抛出业务异常
     *
     * @param obj1      第一个对象
     * @param obj2      第二个对象
     * @param errorCode 对象相等时抛出的错误码
     * @throws BizException 当两个对象相等时抛出
     */
    public static void assertNotEqual(Object obj1, Object obj2, BizErrorCode errorCode) {
        if (Objects.equals(obj1, obj2)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言集合不为空且不为空集合，如果为空则抛出业务异常
     *
     * @param collection 待验证的集合
     * @param errorCode  集合为空或元素为空时抛出的错误码
     * @throws BizException 当集合为空或元素为空时抛出
     */
    public static void notEmpty(Collection<?> collection, BizErrorCode errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw new BizException(errorCode);
        }
    }

    public static void anyTrue(BizErrorCode errorCode, boolean... expressions) {
        if (expressions == null || expressions.length == 0) {
            throw new BizException(errorCode);
        }
        for (boolean expression : expressions) {
            if (expression) {
                return;
            }
        }
        throw new BizException(errorCode);
    }

    public static void isIn(String value, String[] allowedValues, BizErrorCode errorCode) {
        if (value == null) {
            throw new BizException(errorCode);
        }
        for (String allowed : allowedValues) {
            if (value.equals(allowed)) {
                return;
            }
        }
        throw new BizException(errorCode);
    }

    /**
     * 断言集合包含指定元素，如果不包含则抛出业务异常
     *
     * @param collection 待验证的集合
     * @param element    待验证的元素
     * @param errorCode  条件不满足时抛出的错误码
     * @param <T>        集合元素类型
     */
    public static <T> void contains(Collection<T> collection, T element, BizErrorCode errorCode) {
        if (collection == null || !collection.contains(element)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言集合不包含指定元素，如果包含则抛出业务异常
     *
     * @param collection 待验证的集合
     * @param element    待验证的元素
     * @param errorCode  条件不满足时抛出的错误码
     * @param <T>        集合元素类型
     */
    public static <T> void notContains(Collection<T> collection, T element, BizErrorCode errorCode) {
        if (collection != null && collection.contains(element)) {
            throw new BizException(errorCode);
        }
    }
}
