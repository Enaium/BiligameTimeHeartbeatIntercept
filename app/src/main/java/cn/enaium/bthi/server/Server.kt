package cn.enaium.bthi.server

import cn.enaium.bthi.Config
import cn.enaium.bthi.LogType
import kotlin.Throws
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.socket.SocketChannel
import java.lang.Exception
import java.net.InetAddress

/**
 * @author Enaium
 */
object Server {
    @Throws(InterruptedException::class)
    fun start(config: Config) {
        Thread {
            val bossGroup: EventLoopGroup = NioEventLoopGroup()
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            try {
                ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        override fun initChannel(ch: SocketChannel) {
                            val pipeline = ch.pipeline()
                            pipeline.addLast("httpCodec", HttpServerCodec())
                            pipeline.addLast("httpObject", HttpObjectAggregator(1048576))
                            pipeline.addLast("request", Handler(config))
                        }
                    }).bind(config.host, config.port).sync().addListener {
                        config.send("启动成功 Host:${config.host} Port:${config.port}")
                    }.channel().closeFuture().sync()
            } catch (e: InterruptedException) {
                config.send(e.message.toString(), LogType.ERROR)
            } finally {
                bossGroup.shutdownGracefully().sync()
                workerGroup.shutdownGracefully().sync()
            }
        }.start()
    }
}