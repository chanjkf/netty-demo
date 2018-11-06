package xyz.chanjkf.nettydemo.entity;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public class ChannelInfo {
    private ChannelId channelId;
    private Channel channel;
    private String name;
    private String userId;

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
