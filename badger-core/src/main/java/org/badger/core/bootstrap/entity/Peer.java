/**
 * @(#)Peer.java, 6月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.entity;

import lombok.Data;

import java.util.Set;

/**
 * @author liubin01
 */
@Data
public class Peer {

    private String serviceName;
    private Set<String> ends;

    @Data
    static class End {
        private String ip;
        private String port;
    }
}
