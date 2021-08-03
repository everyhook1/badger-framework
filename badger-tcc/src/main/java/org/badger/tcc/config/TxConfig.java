/**
 * @(#)TxConfig.java, 8月 02, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.tcc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TxConfig {
    private boolean tcc = false;
    //tc 地址
    private String remote;
}
