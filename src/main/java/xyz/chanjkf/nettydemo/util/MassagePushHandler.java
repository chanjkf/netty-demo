package xyz.chanjkf.nettydemo.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import xyz.chanjkf.nettydemo.entity.ChannelInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MassagePushHandler implements Runnable {
    NettyTask nettyTask = NettyTask.getInstance();

    public MassagePushHandler() {}
    @Override
    public void run() {

        while (true) {
            Queue<String> channelInfos = nettyTask.getChannelInfos();
            String info = channelInfos.poll();
            if (info == null) {
                System.out.println("当前无推送事件");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                ChannelManager channelManager = ChannelManager.getInstance();
                Map<ChannelId, ChannelInfo> channels = channelManager.getChannels();
                for (ChannelId channelId :channels.keySet()) {
                    ChannelInfo channelInfo = channels.get(channelId);
                    if (ChannelManager.isChannelOnline(channelId)) {
                        System.out.println("开始推送消息到：" + channelInfo.getUserId());
                        Channel channel = channelInfo.getChannel();
                        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame("userId: " + channelInfo.getUserId() + "你好当前时间是"+new Date().toString());
                        channel.writeAndFlush(textWebSocketFrame);
                        System.out.println("消息推送完成");

                    }

                }

            }
        }
    }
}
