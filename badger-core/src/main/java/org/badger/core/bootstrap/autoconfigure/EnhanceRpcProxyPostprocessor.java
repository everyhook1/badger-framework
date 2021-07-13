/**
 * @(#)DoSomethingPostprocessor.java, 6月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.NettyClient;
import org.badger.core.bootstrap.entity.RpcProxy;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.util.SnowflakeIdWorker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author liubin01
 */
@Slf4j
@Component
public class EnhanceRpcProxyPostprocessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final NettyClient nettyClient = new NettyClient();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            final String packageSearchPath = "classpath*:org/**/*.class";

            final Resource[] resources =
                    applicationContext.getResources(packageSearchPath);
            final SimpleMetadataReaderFactory factory = new
                    SimpleMetadataReaderFactory(applicationContext);

            for (final Resource resource : resources) {
                final MetadataReader mdReader = factory.getMetadataReader(resource);

                final AnnotationMetadata am = mdReader.getAnnotationMetadata();
                if (!am.hasAnnotation(RpcProxy.class.getName())) {
                    continue;
                }
                beanFactory.registerSingleton(am.getClassName(), enhance(am.getClassName(),
                        am.getAnnotationAttributes(RpcProxy.class.getName())));

            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object enhance(String className, Map<String, Object> annotationAttributes) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setClzName(className);
                    request.setMethod(method.getName());
                    request.setQualifier((String) annotationAttributes.get("String"));
                    request.setArgs(args);
                    request.setArgTypes(method.getParameterTypes());
                    request.setSeqId(SnowflakeIdWorker.getId());

//                    Object result = nettyClient.send(request);
//                    Class<?> returnType = method.getReturnType();
//
//                    Response response = JSON.parseObject(result.toString(), Response.class);
//                    if (response.getCode() == 1) {
//                        throw new Exception(response.getError_msg());
//                    }
//                    if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)) {
//                        return response.getData();
//                    } else if (Collection.class.isAssignableFrom(returnType)) {
//                        return JSONArray.parseArray(response.getData().toString(), Object.class);
//                    } else if (Map.class.isAssignableFrom(returnType)) {
//                        return JSON.parseObject(response.getData().toString(), Map.class);
//                    } else {
//                        Object data = response.getData();
//                        return JSONObject.parseObject(data.toString(), returnType);
//                    }

                    return null;
                });
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
