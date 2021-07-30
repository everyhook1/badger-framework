/**
 * @(#)EchoImpl.java, 7月 31, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
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
