package net.minelink.bungeeproxy;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class BungeeProxy extends Plugin {
    @Override
    public void onEnable() {
        try {
            Field remoteAddressField = AbstractChannel.class.getDeclaredField("remoteAddress");
            remoteAddressField.setAccessible(true);

            Field serverChild = PipelineUtils.class.getField("SERVER_CHILD");
            serverChild.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(serverChild, serverChild.getModifiers() & ~Modifier.FINAL);

            ChannelInitializer<Channel> bungeeChannelInitializer = PipelineUtils.SERVER_CHILD;

            Method initChannelMethod = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannelMethod.setAccessible(true);

            Field handlerField = HandlerBoss.class.getDeclaredField("handler");
            handlerField.setAccessible(true);

            serverChild.set(null, new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    initChannelMethod.invoke(bungeeChannelInitializer, channel);
                    channel.pipeline().addAfter(PipelineUtils.TIMEOUT_HANDLER, "haproxy-decoder", new HAProxyMessageDecoder());

                    HandlerBoss handlerBoss = new HandlerBoss() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (msg instanceof HAProxyMessage) {
                                HAProxyMessage message = (HAProxyMessage) msg;
                                remoteAddressField.set(channel, new InetSocketAddress(message.sourceAddress(), message.sourcePort()));
                            } else {
                                super.channelRead(ctx, msg);
                            }
                        }
                    };

                    handlerBoss.setHandler((PacketHandler) handlerField.get(channel.pipeline().get(HandlerBoss.class)));
                    channel.pipeline().replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, handlerBoss);
                }
            });
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            getProxy().stop();
        }
    }
}
