package org.badger.core.bootstrap.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.badger.common.api.RpcProxy;
import org.badger.common.api.RpcRequest;
import org.badger.common.api.RpcResponse;
import org.badger.common.api.SpanContext;
import org.badger.core.bootstrap.NettyClient;
import org.badger.core.bootstrap.util.SnowflakeIdWorker;
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

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
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

    private String getBasePackage() {
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object v = entry.getValue();
            String n = v.getClass().getPackage().getName();
            return n.replace(".", "/") + "/";
        }
        return "org/badger/";
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            String packageSearchPath = "classpath*:" + getBasePackage() + "**/*.class";
            Resource[] resources =
                    applicationContext.getResources(packageSearchPath);
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
                    if (fan != null && !serviceNameSet.contains(fan.serviceName())) {
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
