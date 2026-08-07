package com.cloudmall.common.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;

/**
 * 断言工具类
 * 用于在业务逻辑中快速验证条件，当条件不满足时抛出对应的业务异常
 *
 * @author : Tomatos
 * @date : 2026/02/12
 */
@UtilityClass
public class Asserts {

    /**
     * 断言表达式为真，如果为假则抛出业务异常
     *
     * @param expression 待验证的表达式
     * @param errorCode 条件不满足时抛出的错误码
     * @throws BizException 当表达式为假时抛出
     */
    public static void isTrue(boolean expression, BizError errorCode) {
        if (!expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言表达式为假，如果为真则抛出业务异常
     *
     * @param expression 待验证的表达式
     * @param errorCode 条件不满足时抛出的错误码
     * @throws BizException 当表达式为真时抛出
     */
    public static void isFalse(boolean expression, BizError errorCode) {
        if (expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言对象不为空，如果为空则抛出业务异常
     *
     * @param obj 待验证的对象
     * @param errorCode 对象为空时抛出的错误码
     * @throws BizException 当对象为空时抛出
     */
    public static void notNull(Object obj, BizError errorCode) {
        if (obj == null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言对象不为空，如果为空则抛出默认业务异常（数据不存在）。
     *
     * @param obj 待验证的对象
     * @throws BizException 当对象为空时抛出
     */
    public static void notNull(Object obj) {
        notNull(obj, BizError.DATA_NOT_FOUND);
    }

    /**
     * 断言对象不为空并返回原值，为空则抛出业务异常。 <p>
     * 用于「查询结果 -> 非空校验 -> 直接使用」的常见链路，可把「声明变量 + 断言 + 使用/返回
     * 」收敛成一行。
     *
     * @param obj 待校验对象
     * @param errorCode 对象为空时抛出的错误码
     * @param <T> 对象类型
     * @return 非空的原对象
     * @throws BizException 当对象为空时抛出
     */
    public static <T> T requireNonNull(T obj, BizError errorCode) {
        if (obj == null) {
            throw new BizException(errorCode);
        }
        return obj;
    }

    /**
     * 断言对象不为空并返回原值，为空则抛出默认业务异常（数据不存在）。 <p>
     * 查询类场景无需手动指定错误码时使用，可进一步省略错误码参数。
     *
     * @param obj 待校验对象
     * @param <T> 对象类型
     * @return 非空的原对象
     * @throws BizException 当对象为空时抛出
     */
    public static <T> T requireNonNull(T obj) {
        return requireNonNull(obj, BizError.DATA_NOT_FOUND);
    }

    /**
     * 断言对象不为空，否则抛出业务异常。
     *
     * @param obj 待校验对象
     * @param exceptionSupplier 异常供应器
     */
    public static <X extends RuntimeException> void notNull(Object obj, Supplier<X> exceptionSupplier) {
        if (obj == null) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 断言对象为空，否则抛出业务异常。
     *
     * @param obj 待校验对象
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void isNull(Object obj, BizError errorCode) {
        if (obj != null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言对象为空，如果不为空则抛出业务异常
     *
     * @param obj 待验证的对象
     * @param errorCode 对象不为空时抛出的错误码
     * @throws BizException 当对象不为空时抛出
     */
    public static void assertNull(Object obj, BizError errorCode) {
        if (obj != null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言字符串不为空且不为空字符串，如果为空则抛出业务异常
     *
     * @param str 待验证的字符串
     * @param errorCode 字符串为空或空字符串时抛出的错误码
     * @throws BizException 当字符串为空或空字符串时抛出
     */
    public static void assertNotBlank(String str, BizError errorCode) {
        if (str == null || str.trim().isEmpty()) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言两个对象相等，如果不相等则抛出业务异常
     *
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @param errorCode 对象不相等时抛出的错误码
     * @throws BizException 当两个对象不相等时抛出
     */
    public static void isEqual(Object obj1, Object obj2, BizError errorCode) {
        if (!Objects.equals(obj1, obj2)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言两个对象不相等，如果相等则抛出业务异常
     *
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @param errorCode 对象相等时抛出的错误码
     * @throws BizException 当两个对象相等时抛出
     */
    public static void notEqual(Object obj1, Object obj2, BizError errorCode) {
        if (Objects.equals(obj1, obj2)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值为零，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void isZero(long value, BizError errorCode) {
        if (value != 0) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值不为零，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void isNotZero(long value, BizError errorCode) {
        if (value == 0) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值为正数，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void isPositive(long value, BizError errorCode) {
        if (value <= 0) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值大于下限，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param min 最小值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void greaterThan(long value, long min, BizError errorCode) {
        if (value <= min) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值不小于下限，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param min 最小值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void greaterOrEqual(long value, long min, BizError errorCode) {
        if (value < min) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值小于上限，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param max 最大值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void lessThan(long value, long max, BizError errorCode) {
        if (value >= max) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言数值位于指定范围内，否则抛出业务异常。
     *
     * @param value 待校验的值
     * @param min 最小值
     * @param max 最大值
     * @param errorCode 错误码
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void inRange(long value, long min, long max, BizError errorCode) {
        if (value < min || value > max) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言字符串不为空白，否则抛出业务异常。
     *
     * @param str 待校验字符串
     * @param errorCode 错误码
     */
    public static void notBlank(String str, BizError errorCode) {
        assertNotBlank(str, errorCode);
    }

    /**
     * 断言集合不为空且不为空集合，如果为空则抛出业务异常
     *
     * @param collection 待验证的集合
     * @param errorCode 集合为空或元素为空时抛出的错误码
     * @throws BizException 当集合为空或元素为空时抛出
     */
    public static void notEmpty(Collection<?> collection, BizError errorCode) {
        if (CollUtil.isEmpty(collection)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言至少一个表达式为真，否则抛出业务异常。
     *
     * @param errorCode 错误码
     * @param expressions 布尔表达式列表
     * @throws BizException 处理过程中可能抛出该异常
     */
    public static void anyTrue(BizError errorCode, boolean... expressions) {
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

    public static void isIn(String value, String[] allowedValues, BizError errorCode) {
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
     * @param element 待验证的元素
     * @param errorCode 条件不满足时抛出的错误码
     * @param <T> 集合元素类型
     */
    public static <T> void contains(Collection<T> collection, T element, BizError errorCode) {
        if (collection == null || !collection.contains(element)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言集合不包含指定元素，如果包含则抛出业务异常
     *
     * @param collection 待验证的集合
     * @param element 待验证的元素
     * @param errorCode 条件不满足时抛出的错误码
     * @param <T> 集合元素类型
     */
    public static <T> void notContains(Collection<T> collection, T element, BizError errorCode) {
        if (collection != null && collection.contains(element)) {
            throw new BizException(errorCode);
        }
    }
}
