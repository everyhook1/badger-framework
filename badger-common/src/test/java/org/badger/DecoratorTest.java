package org.badger;

/**
 * @author liubin01
 */
public class DecoratorTest implements Test {
    private final Test target;

    public DecoratorTest(Test target) {
        this.target = target;
    }

    public int test(int i) {
        return target.test(i);
    }
}