package com.tencent.tsf.femas.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/16 17:55
 * @Version 1.0
 */
public class FemasException extends RuntimeException {

    /**
     * 异常编码
     */
    private String errorCode;
    /**
     * 异常信息
     */
    private String errorMessage;

    public FemasException() {
    }

    public FemasException(String errorCode) {
        this(errorCode, (String) null);
    }

    public FemasException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, (Throwable) null);
    }

    public FemasException(String errorCode, Throwable cause) {
        this(errorCode, (String) null, cause);
    }

    public FemasException(String errorCode, String errorMessage, Throwable cause) {
        super(String.format("%s: %s", errorCode, String.format(errorMessage)), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 异常枚举转换为code
     *
     * @param error 异常枚举
     * @return 大驼峰编码
     */
    public static String errorToCode(Enum<?> error) {
        String errorName = error.name().toLowerCase();
        String[] sp = errorName.split("_");
        StringBuffer code = new StringBuffer();
        for (String s : sp) {
            code.append(StringUtils.capitalize(s));
        }
        return code.toString();
    }

    /**
     * 获取原始异常编码
     *
     * @return 异常编码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取原始异常消息
     *
     * @return 异常消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}