/**
 * @(#)data.java, 8月 10, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.entity;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class CompensableIdentifier {
    private String serviceName;
    private Class<?> clz;
    private String identifier;
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
