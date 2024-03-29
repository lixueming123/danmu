
package com.lxm.danmu.netty.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BootstrapConfig {

    @Value("${tcp.backlog}")
    private int backlog;

    @Value("${tcp.keepalive}")
    private boolean keepalive;

    @Value("${tcp.noDelay}")
    private boolean noDelay;

    @Autowired
    private PipeLineConfig pipeLineConfig;

    @Bean
    public ServerBootstrap getBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(NioServerSocketChannel.class)
                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, backlog)
                //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, keepalive)
                //将小的数据包包装成更大的帧进行传送，提高网络的负载
                .childOption(ChannelOption.TCP_NODELAY, noDelay)
                .childHandler(pipeLineConfig);
        log.info("Bootstrap inited.");
        return bootstrap;
    }

}
