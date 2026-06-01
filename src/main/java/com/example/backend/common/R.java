package com.example.backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 所有后端接口的返回值都使用此类包装，方便前端统一处理
 * 格式：{ code: 200, msg: "success", data: {...} }
 *
 * 使用示例：
 *   R.ok(data)           → 成功返回，code=200
 *   R.fail("错误信息")    → 失败返回，code=500
 *   R.fail(400, "参数错误") → 失败返回，自定义code
 *
 * @param <T> data 的泛型类型
 */
@Data
public class R<T> implements Serializable {

    /** 业务状态码：200=成功，其他=失败 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /** 私有构造器，通过静态工厂方法创建实例 */
    private R() {}

    /**
     * 创建成功响应（无数据）
     * @return R 实例，code=200, msg="success"
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 创建成功响应（带数据）
     * @param data 响应数据
     * @return R 实例，code=200, msg="success"
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /**
     * 创建失败响应（默认错误码500）
     * @param msg 错误信息
     * @return R 实例，code=500
     */
    public static <T> R<T> fail(String msg) {
        return fail(500, msg);
    }

    /**
     * 创建失败响应（自定义错误码）
     * @param code 错误状态码
     * @param msg 错误信息
     * @return R 实例
     */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
