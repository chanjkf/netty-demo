package xyz.chanjkf.nettydemo.util;

import org.springframework.stereotype.Component;
import xyz.chanjkf.nettydemo.entity.ChannelInfo;

import javax.annotation.PostConstruct;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class NettyTask {

    private NettyTask(){}

    private static NettyTask nettyTask = new NettyTask();
    public static NettyTask getInstance() {
        return nettyTask;
    }

    private static ExecutorService executor;

    public Queue<String> channelInfos = new ConcurrentLinkedQueue<>();

    public void pushQueueu(String info) {
        channelInfos.add(info);
    }

    public Queue<String> getChannelInfos() {
        return channelInfos;
    }

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(10);
        executor.execute(new MassagePushHandler());
    }
}
