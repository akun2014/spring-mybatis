package com.ownerkaka.springmybatis.support;

/**
 * @author akun
 * @since 2019-08-13
 */
public class OwnerkakaOptions {
    private Integer id;
    private String fieldName;
    private String fieldType;
    private String fieldValue;

    @Override
    public String toString() {
        return "OwnerkakaOptions{" +
                "id=" + id +
                ", fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                '}';
    }
}