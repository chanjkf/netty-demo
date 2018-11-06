package xyz.chanjkf.nettydemo.service;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import xyz.chanjkf.nettydemo.entity.ChannelInfo;
import xyz.chanjkf.nettydemo.entity.ParamRequest;
import xyz.chanjkf.nettydemo.util.ChannelManager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static xyz.chanjkf.nettydemo.service.NettyService.WEBSOCKET_BASE_URL;

public class NettyService implements Runnable {

    private static final int port = 8081;
    public static final String WEBSOCKET_BASE_URL="ws://" + "127.0.0.1" + ":" + port + "/" + "websocket";

    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChildHandler());
        System.out.println(" WebSocketServer is running ... ");

        try {
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        NettyService nettyService = new NettyService();
        new Thread(nettyService).start();
    }



}
class ChildHandler extends ChannelInitializer<SocketChannel> {
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(64 * 1024));
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        pipeline.addLast("protocol", new WebSocketServerProtocolHandler("/app/websocket"));
        pipeline.addLast("handler", new WebSocketServerHandler());
        socketChannel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("关闭channel："+future.channel().id());
                ChannelManager.getInstance().removeChannel(future.channel().id());
            }
        });
    }


}
class WebSocketServerHandler extends SimpleChannelInboundHandler {
    private WebSocketServerHandshaker handshaker;
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage) {
            System.out.println("发送的是Http请求");
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
        } else if (frame instanceof PingWebSocketFrame) {
            System.out.println("ping:" + (PingWebSocketFrame)frame);
            ctx.channel().write(new PingWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof TextWebSocketFrame) {
            //返回应答
            String request = ((TextWebSocketFrame) frame).text();
            ParamRequest paramRequest = JSON.parseObject(request, ParamRequest.class);
            addChannelInfoByParam(ctx, paramRequest);
            System.out.println("received: "+paramRequest);
            String responseMsg = "欢迎用户编号："+paramRequest.getParameters().getUserId()+"使用websocket服务，现在是北京时间：" + new Date().toString();
            ctx.channel().write(new TextWebSocketFrame(responseMsg));
            return;
        }
        throw new UnsupportedOperationException();
    }

    private void addChannelInfoByParam(ChannelHandlerContext ctx, ParamRequest paramRequest) {
        Long userId = paramRequest.getParameters().getUserId();
        ChannelManager channelManager = ChannelManager.getInstance();
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(userId+"");
        channelInfo.setChannel(ctx.channel());
        channelInfo.setChannelId(ctx.channel().id());
        channelManager.addChannel(channelInfo.getChannelId(), channelInfo);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (request.getDecoderResult().isFailure() ||
                !"websocket".equalsIgnoreCase(request.headers().get("Upgrade"))) {
            System.out.println("该Http请求不是WebSocket注册是发送的请求");
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        addChannelInfo(ctx, request);
        System.out.println("构造握手成功返回信息");
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(WEBSOCKET_BASE_URL, null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    private void addChannelInfo(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> parameters = decoder.parameters();
        String userId = parameters.get("userId").get(0);
        System.out.println("用户id为：" + userId);
        ChannelManager channelManager = ChannelManager.getInstance();
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(userId);
        channelInfo.setChannel(ctx.channel());
        channelInfo.setChannelId(ctx.channel().id());
        channelManager.addChannel(channelInfo.getChannelId(), channelInfo);

    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request,
                                  DefaultFullHttpResponse response) {
        if (response.getStatus().code() != 200) {
            ByteBuf buff = Unpooled.copiedBuffer(response.getStatus().toString(),
                    CharsetUtil.UTF_8);
            response.content().writeBytes(buff);
            buff.release();
        }

        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        boolean isKeepAlive = HttpHeaders.isKeepAlive(request);
        if (!isKeepAlive || response.getStatus().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    /**
     * 通道关闭处理方法
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
