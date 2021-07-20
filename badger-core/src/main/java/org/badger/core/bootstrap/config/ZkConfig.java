package org.badger.core.bootstrap.config;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class ZkConfig {
    private String address;
    private int sleepMsBetweenRetries = 100;
    private int maxRetries = 3;
}
