/**
 * @(#)AssistProxy.java, 7月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author liubin01
 */
public class AssistProxy<T> {

    private final Class<T> t;//接口

    public AssistProxy(Class<T> t) {
        this.t = t;
    }

    private static final String PROXY_PREFIX = "$Proxy";//生成的代理对象名称前缀
    private static final String PROXY_SUFFIX = "Impl";//生成的代理对象名称前缀

    //生成代理对象
    public T getProxyObject() {
        T proxyObject = null;
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.makeClass(getPackageName() + getProxyObjectName()); //创建代理类对象

            //设置代理类的接口
            CtClass inter = pool.getCtClass(getPackageName() + "." + t.getSimpleName()); //获取代理对象的接口类
            CtClass[] interfaces = new CtClass[]{inter};
            ctClass.setInterfaces(interfaces);

            CtMethod[] methods = inter.getDeclaredMethods(); //代理类的所有方法
            CtField[] fields = inter.getDeclaredFields();//代理类的所有属性
            for (CtMethod method : methods) {
                String methodName = method.getName();
                CtMethod cm = new CtMethod(method.getReturnType(), methodName, method.getParameterTypes(), ctClass);
                cm.setBody("System.out.println(\"hand up my homework from proxy Object\");");
                ctClass.addMethod(cm);
            }
            Class<?> aClass = ctClass.toClass();
            proxyObject = (T) aClass.newInstance();
        } catch (NotFoundException | CannotCompileException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return proxyObject;
    }

    //获取包名
    public String getPackageName() {
        Package aPackage = t.getPackage();
        return aPackage.getName();
    }

    //获取代理对象的名称
    public String getProxyObjectName() {
        return PROXY_PREFIX + t.getSimpleName() + PROXY_SUFFIX;
    }

}
