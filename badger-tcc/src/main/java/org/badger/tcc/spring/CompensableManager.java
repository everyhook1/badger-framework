
package org.badger.tcc.spring;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.badger.tcc.entity.Compensable;
import org.badger.tcc.entity.CompensableIdentifier;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liubin01
 */
@Slf4j
public class CompensableManager implements InstantiationAwareBeanPostProcessor {

    private final Map<String, CompensableIdentifier> compensableIdentifierMap = new ConcurrentHashMap<>();

    public CompensableManager() {

    }

    public static boolean equals(Class<?>[] a, Class<?>[] a2) {
        if (a == a2)
            return true;
        if (a == null || a2 == null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            Class<?> o1 = a[i];
            Class<?> o2 = a2[i];
            if (!(Objects.equals(o1, o2)))
                return false;
        }
        return true;
    }

    public CompensableIdentifier getIdentifier(String identifier) {
        if (!compensableIdentifierMap.containsKey(identifier)) {
            throw new IllegalStateException(String.format("no identifier %s exist", identifier));
        }
        CompensableIdentifier compensableIdentifier = new CompensableIdentifier();
        BeanUtils.copyProperties(compensableIdentifierMap.get(identifier), compensableIdentifier);
        return compensableIdentifier;
    }

    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        Map<String, List<Pair<Compensable, Method>>> methodMap = new HashMap<>();
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            if (method.isAnnotationPresent(Compensable.class)) {
                Compensable compensable = method.getAnnotation(Compensable.class);
                String identifier = compensable.identifier();
                methodMap.computeIfAbsent(identifier, k -> new ArrayList<>());
                methodMap.get(identifier).add(new ImmutablePair<>(compensable, method));
            }
        });
        methodMap.forEach((identifier, list) -> {
            Compensable c0 = list.get(0).getKey();
            Class<?>[] p0 = list.get(0).getValue().getParameterTypes();
            for (int i = 1; i < 3; i++) {
                Compensable ci = list.get(i).getKey();
                Class<?>[] pi = list.get(i).getValue().getParameterTypes();
                boolean res = c0.tryMethod().equals(ci.tryMethod())
                        && c0.confirmMethod().equals(ci.confirmMethod())
                        && c0.cancelMethod().equals(ci.cancelMethod())
                        && equals(p0, pi);
                if (!res) {
                    log.error("Compensable {} is not completeness", identifier);
                    return;
                }
            }
            Set<String> nameSet = new HashSet<>();
            nameSet.add(c0.tryMethod());
            nameSet.add(c0.confirmMethod());
            nameSet.add(c0.cancelMethod());
            if (nameSet.size() != 3) {
                log.error("Compensable {} is not completeness", identifier);
                return;
            }
            for (Pair<Compensable, Method> pair : list) {
                Method method = pair.getValue();
                if (!nameSet.contains(method.getName())) {
                    log.error("Compensable {} is not completeness", identifier);
                    return;
                }
            }
            CompensableIdentifier compensableIdentifier = new CompensableIdentifier();
            compensableIdentifier.setIdentifier(identifier);
            compensableIdentifier.setClzName(bean.getClass().getName());
            compensableIdentifier.setBeanName(beanName);
            compensableIdentifier.setTryMethod(c0.tryMethod());
            compensableIdentifier.setConfirmMethod(c0.confirmMethod());
            compensableIdentifier.setCancelMethod(c0.cancelMethod());
            compensableIdentifier.setParameterTypes(p0);
            compensableIdentifierMap.put(identifier, compensableIdentifier);
        });

        return true;
    }
}
