package xyz.chanjkf.nettydemo.util;

import io.netty.channel.ChannelId;
import xyz.chanjkf.nettydemo.entity.ChannelInfo;

import java.util.HashMap;
import java.util.Map;

public class ChannelManager {
    private static Map<ChannelId, ChannelInfo> map = new HashMap<>();
    private ChannelManager(){}
    public static ChannelManager getInstance() {
        return new ChannelManager();
    }
    public synchronized Map<ChannelId, ChannelInfo> getChannels() {
        return map;
    }

    public synchronized int getChannelSize() {
        return map.size();
    }

    public synchronized void addChannel(ChannelId channelId, ChannelInfo channelInfo) {
        map.put(channelId, channelInfo);
    }

    public synchronized void removeChannel(ChannelId channelId) {
        map.remove(channelId);
    }

    public static synchronized boolean isChannelOnline(ChannelId channelId) {
        return map.containsKey(channelId);
    }
}
