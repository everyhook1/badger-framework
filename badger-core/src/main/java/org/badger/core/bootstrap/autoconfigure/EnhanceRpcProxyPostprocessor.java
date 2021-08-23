package org.badger.core.bootstrap.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProxy;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.common.api.SpanContext;
import org.badger.core.bootstrap.NettyClient;
import org.badger.common.api.util.SnowflakeIdWorker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liubin01
 */
@Slf4j
@Component
public class EnhanceRpcProxyPostprocessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public static final NettyClient nettyClient = NettyClient.getInstance();

    private static final String BADGER_BASE_PACKAGE = "org/badger/";

    private String getBasePackage() {
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object v = entry.getValue();
            String n = v.getClass().getPackage().getName();
            return n.replace(".", "/") + "/";
        }
        return BADGER_BASE_PACKAGE;
    }

    private Set<Resource> scanPackage(String... packages) throws IOException {
        Set<String> packageSet = new HashSet<>();
        Collections.addAll(packageSet, packages);
        Set<Resource> resourceSet = new HashSet<>();
        for (String s : packageSet) {
            String packageSearchPath = "classpath*:" + s + "**/*.class";
            Resource[] resources = applicationContext.getResources(packageSearchPath);
            Collections.addAll(resourceSet, resources);
        }
        return resourceSet;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            Set<Resource> resources = scanPackage(getBasePackage(), BADGER_BASE_PACKAGE);
            SimpleMetadataReaderFactory factory = new
                    SimpleMetadataReaderFactory(applicationContext);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Set<String> serviceNameSet = new HashSet<>();
            for (Resource resource : resources) {
                MetadataReader mdReader = factory.getMetadataReader(resource);
                Class<?> aClazz = loader.loadClass(mdReader.getClassMetadata().getClassName());
                Field[] fields = aClazz.getDeclaredFields();
                for (Field field : fields) {
                    RpcProxy fan = field.getAnnotation(RpcProxy.class);
                    if (fan != null) {
                        serviceNameSet.add(fan.serviceName());
                        String clzName = field.getType().getName();
                        beanFactory.registerSingleton(clzName, enhance(clzName, fan.qualifier(), fan.serviceName(), fan.timeout()));
                    }
                }
            }
            nettyClient.setServiceNameSet(serviceNameSet);
        } catch (final Exception e) {
            log.error("error", e);
            throw new RuntimeException(e);
        }
    }


    private Object enhance(String className, String qualifier, String serviceName, long timeout) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    if (Arrays.stream(clazz.getDeclaredMethods()).noneMatch(m -> m.getName().equals(method.getName()))) {
                        return null;
                    }
                    RpcRequest request = new RpcRequest();
                    request.setClzName(clazz.getSimpleName());
                    request.setMethod(method.getName());
                    request.setQualifier(qualifier);
                    request.setServiceName(serviceName);
                    request.setTimeout(timeout);
                    request.setArgs(args);
                    request.setArgTypes(method.getParameterTypes());
                    request.setSeqId(SnowflakeIdWorker.getId());
                    request.setParentRpc(SpanContext.getCurRequest());
                    request.setTransactionContext(SpanContext.getTransactionContext());
                    RpcResponse response = (RpcResponse) nettyClient.send(request);
                    if (response.getCode() == 500) {
                        throw new Exception(response.getErrMsg());
                    }
                    return response.getBody();
                });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
