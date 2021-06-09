package org.badger.common; /**
 * @(#)KryoNetty.java, 6月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author liubin01
 */
public class KryoNetty {
    boolean useLogging;
    boolean useExecution;
    int executionThreadSize;

    HashMap<Integer, Class<?>> classesToRegister;
    int inputBufferSize;
    int outputBufferSize;
    int maxOutputBufferSize;

    public KryoNetty() {
        this.useLogging = false;
        this.useExecution = false;
        this.executionThreadSize = 1;

        this.classesToRegister = new HashMap<>();
        this.inputBufferSize = -1;
        this.outputBufferSize = -1;
        this.maxOutputBufferSize = -1;
    }

    public KryoNetty useLogging() {
        this.useLogging = true;
        return this;
    }

    public KryoNetty useExecution() {
        this.useExecution = true;
        return this;
    }

    public KryoNetty threadSize(int size) {
        this.executionThreadSize = size;
        return this;
    }

    public KryoNetty register(int index, Class<?> clazz) {
        this.classesToRegister.put(index, clazz);
        return this;
    }

    public KryoNetty register(Class<?> clazz) {
        this.classesToRegister.put(this.classesToRegister.size() + 1, clazz);
        return this;
    }

    public KryoNetty register(Class<?>... clazzez) {
        if (clazzez.length != 0)
            Arrays.stream(clazzez).forEach(clazz -> this.classesToRegister.put(this.classesToRegister.size() + 1, clazz));
        return this;
    }

    public KryoNetty inputSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
        return this;
    }

    public KryoNetty outputSize(int outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
        return this;
    }

    public KryoNetty maxOutputSize(int maxOutputBufferSize) {
        this.maxOutputBufferSize = maxOutputBufferSize;
        return this;
    }

    public boolean isUseLogging() {
        return useLogging;
    }

    public boolean isUseExecution() {
        return useExecution;
    }

    public int getExecutionThreadSize() {
        return executionThreadSize;
    }

    protected HashMap<Integer, Class<?>> getClassesToRegister() {
        return classesToRegister;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }

    protected int getMaxOutputBufferSize() {
        return maxOutputBufferSize;
    }
}
