package org.badger.core.bootstrap.entity;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author liubin01
 */
@Data
@Slf4j
public class Peer {

    private final String serviceName;
    private final Bootstrap bootstrap;
    private List<End> ends = new CopyOnWriteArrayList<>();
    private Map<Pair<String, Integer>, Channel> channelMap = new ConcurrentHashMap<>();

    public void addEnd(String host, int port) {
        Pair<String, Integer> pair = ImmutablePair.of(host, port);
        if (channelMap.containsKey(pair)) {
            return;
        }
        try {
            Channel channel = this.bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
            ends.add(new End(host, port, channel));
            channelMap.put(pair, channel);
        } catch (InterruptedException e) {
            log.error("connect ", e);
        }
    }

    private boolean isConnected(Channel channel) {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public void removeEnd(String host, int port) {
        Pair<String, Integer> pair = ImmutablePair.of(host, port);
        if (!channelMap.containsKey(pair)) {
            return;
        }
        Channel channel = channelMap.get(pair);
        closeChannel(channel);
        channelMap.remove(pair);
        ends.removeIf(end -> end.host.equals(host) && end.port == port);
    }

    private void closeChannel(Channel channel) {
        if (isConnected(channel)) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                log.error("closeChannel error", e);
            }
        }
    }

    public void destroy() {
        channelMap.forEach((k, v) -> closeChannel(v));
        channelMap.clear();
        ends.clear();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class End {
        private String host;
        private int port;
        private Channel channel;
    }
}
