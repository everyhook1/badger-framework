/**
 * @(#)data.java, 8月 10, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.entity;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author liubin01
 */
@Data
public class CompensableIdentifier {
    private String serviceName;
    private String clzName;
    private Object bean;
    private String beanName;
    private String identifier;
    private Method tryM;
    private Method cancelM;
    private Method confirmM;
    private String tryMethod;
    private String cancelMethod;
    private String confirmMethod;
    private Class<?>[] parameterTypes;

    public CompensableEnum getCompensableEnum(String methodName) {
        if (tryMethod.equals(methodName)) {
            return CompensableEnum.TRY;
        }
        if (confirmMethod.equals(methodName)) {
            return CompensableEnum.CONFIRM;
        }
        if (cancelMethod.equals(methodName)) {
            return CompensableEnum.CANCEL;
        }
        return CompensableEnum.UNKNOWN;
    }

}
