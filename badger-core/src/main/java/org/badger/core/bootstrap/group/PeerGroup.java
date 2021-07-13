/**
 * @(#)PeerGroup.java, 7月 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.badger.core.bootstrap.group;

import java.util.Set;

/**
 * @author liubin01
 */
public class PeerGroup {

    private Set<String> serviceName;

    private String registerServers;

    public PeerGroup(Set<String> serviceName, String registerServers) {
        this.serviceName = serviceName;
        this.registerServers = registerServers;
    }

    public void onChanged() {

    }
}
