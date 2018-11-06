package xyz.chanjkf.nettydemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class ServiceInit {

    @PostConstruct
    public void init() {
        new Thread(new NettyService()).start();
        log.info(" WebSocketInit begin start ...");
    }

}
