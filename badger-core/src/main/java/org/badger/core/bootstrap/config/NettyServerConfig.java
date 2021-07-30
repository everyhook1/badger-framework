package org.badger.core.bootstrap.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NettyServerConfig {


    public static final int DEFAULT_PORT = 11311;

    private int port = DEFAULT_PORT;

    private String serviceName;
}
