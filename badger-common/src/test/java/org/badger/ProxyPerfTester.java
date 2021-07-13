package org.badger;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProxyPerfTester {


    public static void main(String[] args) {
        //创建测试对象；
        Test nativeTest = new TestImpl();
        Test decorator = new DecoratorTest(nativeTest);
        Test dynamicProxy = DynamicProxyTest.newProxyInstance(nativeTest);
        Test cglibProxy = CglibProxyTest.newProxyInstance(TestImpl.class);

        //预热一下；
        int preRunCount = 10000;
        runWithoutMonitor(nativeTest, preRunCount);
        runWithoutMonitor(decorator, preRunCount);
        runWithoutMonitor(cglibProxy, preRunCount);
        runWithoutMonitor(dynamicProxy, preRunCount);

        //执行测试；
        Map<String, Test> tests = new LinkedHashMap<>();
        tests.put("Native   ", nativeTest);
        tests.put("Decorator", decorator);
        tests.put("Dynamic  ", dynamicProxy);
        tests.put("Cglib    ", cglibProxy);
        int repeatCount = 3;
        int runCount = 1000000;
        runTest(repeatCount, runCount, tests);
        runCount = 500000000;
        runTest(repeatCount, runCount, tests);
    }

    private static void runTest(int repeatCount, int runCount, Map<String, Test> tests) {
        System.out.printf("\n==================== run test : [repeatCount=%s] [runCount=%s] [java.version=%s] ====================%n", repeatCount, runCount, System.getProperty("java.version"));
        for (int i = 0; i < repeatCount; i++) {
            System.out.printf("\n--------- test : [%s] ---------%n", (i + 1));
            for (String key : tests.keySet()) {
                runWithMonitor(tests.get(key), runCount, key);
            }
        }
    }

    private static void runWithoutMonitor(Test test, int runCount) {
        for (int i = 0; i < runCount; i++) {
            test.test(i);
        }
    }

    private static void runWithMonitor(Test test, int runCount, String tag) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            test.test(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("[" + tag + "] Elapsed Time:" + (end - start) + "ms");
    }
}