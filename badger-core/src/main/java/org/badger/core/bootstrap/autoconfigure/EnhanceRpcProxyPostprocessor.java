package org.badger.core.bootstrap.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.badger.core.bootstrap.NettyClient;
import org.badger.core.bootstrap.entity.RpcProxy;
import org.badger.core.bootstrap.entity.RpcRequest;
import org.badger.core.bootstrap.entity.RpcResponse;
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

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            final String packageSearchPath = "classpath*:org/**/*.class";

            final Resource[] resources =
                    applicationContext.getResources(packageSearchPath);
            final SimpleMetadataReaderFactory factory = new
                    SimpleMetadataReaderFactory(applicationContext);

            Set<String> serviceNameSet = new HashSet<>();
            for (final Resource resource : resources) {
                final MetadataReader mdReader = factory.getMetadataReader(resource);

                final AnnotationMetadata am = mdReader.getAnnotationMetadata();
                if (!am.hasAnnotation(RpcProxy.class.getName())) {
                    continue;
                }
                Map<String, Object> attrs = am.getAnnotationAttributes(RpcProxy.class.getName());
                String key = "serviceName";
                if (attrs != null && attrs.containsKey(key)) {
                    String serviceName = (String) attrs.get(key);
                    serviceNameSet.add(serviceName);
                    beanFactory.registerSingleton(am.getClassName(), enhance(am.getClassName(), attrs, serviceName));
                }
            }
            nettyClient.setServiceNameSet(serviceNameSet);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object enhance(String className, Map<String, Object> attrs, String serviceName) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    if (Arrays.stream(clazz.getDeclaredMethods()).noneMatch(m -> m.getName().equals(method.getName()))) {
                        return method.invoke(clazz.newInstance(), args);
                    }
                    RpcRequest request = new RpcRequest();
                    request.setClzName(clazz.getSimpleName());
                    request.setMethod(method.getName());
                    request.setQualifier((String) attrs.get("qualifier"));
                    request.setServiceName(serviceName);
                    request.setArgs(args);
                    request.setArgTypes(method.getParameterTypes());
                    request.setSeqId(SnowflakeIdWorker.getId());

                    Object result = nettyClient.send(request);
                    Class<?> returnType = method.getReturnType();

                    RpcResponse response = (RpcResponse) result;
                    if (response.getCode() == 1) {
                        throw new Exception(response.getErrMsg());
                    }
                    if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)) {
                        return response.getBody();
                    } else if (Collection.class.isAssignableFrom(returnType)) {
                        return JSONArray.parseArray(response.getBody().toString(), Object.class);
                    } else if (Map.class.isAssignableFrom(returnType)) {
                        return JSON.parseObject(response.getBody().toString(), Map.class);
                    } else {
                        Object data = response.getBody();
                        return JSONObject.parseObject(data.toString(), returnType);
                    }
                });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
