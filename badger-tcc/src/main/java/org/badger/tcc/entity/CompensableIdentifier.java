package org.badger.tcc.entity;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class CompensableIdentifier {

    private String clzName;
    private String beanName;
    private String identifier;
    private String tryMethod;
    private String cancelMethod;
    private String confirmMethod;
    private Class<?>[] parameterTypes;
    private Object[] args;

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
