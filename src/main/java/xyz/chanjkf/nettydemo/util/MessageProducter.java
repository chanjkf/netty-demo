package xyz.chanjkf.nettydemo.util;

import io.netty.channel.ChannelId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.chanjkf.nettydemo.entity.ChannelInfo;

import java.util.Map;


@Component
public class MessageProducter {

    @Scheduled(cron = "0/2 * * * * ?")
    public void task() {
        ChannelManager channelManager = ChannelManager.getInstance();
        Map<ChannelId, ChannelInfo> channels = channelManager.getChannels();

        NettyTask nettyTask = NettyTask.getInstance();

        nettyTask.pushQueueu(Math.random()*100+"");

    }
}
