package org.badger.core.bootstrap.config;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class NettyServerConfig {


    public static final int DEFAULT_PORT = 11311;

    private int port = DEFAULT_PORT;

    private String serviceName;
}
