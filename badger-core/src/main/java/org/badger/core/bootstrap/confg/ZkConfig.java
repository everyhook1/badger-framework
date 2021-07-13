/**
 * @(#)ZkConfig.java, 6月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.confg;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class ZkConfig {
    private String address;
    private String path;
    private int sleepMsBetweenRetries = 100;
    private int maxRetries = 3;
}
