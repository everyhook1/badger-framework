package org.badger.core.bootstrap;

/**
 * @author liubin01
 */
public class EchoImpl implements Echo {

    @Override
    public int delta(int a, int b) {
        return a * b;
    }
}
