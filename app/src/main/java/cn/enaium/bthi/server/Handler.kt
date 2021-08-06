package cn.enaium.bthi.server

import cn.enaium.bthi.Config
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import kotlin.Throws
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import java.lang.Exception
import java.nio.charset.Charset

/**
 * @author Enaium
 */
class Handler(val config: Config) : ChannelInboundHandlerAdapter() {
    private var host: String? = null
    private var port = 0
    private var channelFuture: ChannelFuture? = null
    private var clientRequestUUID: String? = null

    @Throws(Exception::class)
    override fun channelRead(requestContext: ChannelHandlerContext, requestInstance: Any) {
        if (requestInstance is FullHttpRequest) {

            val s = requestInstance.headers()["host"]
            val temp = s.split(":").toTypedArray()
            host = temp[0]
            port = if (temp.size == 3) {
                temp[1].toInt()
            } else if ("CONNECT".equals(requestInstance.method().name(), ignoreCase = true)) {
                443
            } else {
                80
            }

            if (requestInstance.uri().endsWith("app/v2/time/heartbeat")) {
                val content = requestInstance.content().toString(Charset.forName("UTF-8"))

                for (each in content.split("&")) {
                    if ("client_request_uuid" == each.split("=")[0]) {
                        clientRequestUUID = each.split("=")[1]
                    }
                }

                //清除数据让请求失败
                requestInstance.headers().clear()
                requestInstance.content().clear()
            }

            val bootstrap = Bootstrap()
                .group(requestContext.channel().eventLoop())
                .channel(requestContext.channel().javaClass)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast("codec", HttpClientCodec())
                        pipeline.addLast("aggregator", HttpObjectAggregator(1048576))
                        pipeline.addLast("response", object : ChannelInboundHandlerAdapter() {
                            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                var response = msg as FullHttpResponse
                                //不用返回数据也行 写这个只是为了和通常返回的数据一致
                                if (requestInstance.uri().endsWith("app/v2/time/heartbeat")) {
                                    if (clientRequestUUID != null) {
                                        val content = """
                                            {
                                                "code": 0,
                                                "client_request_uuid": "$clientRequestUUID",
                                                "data": "{"user_info":{"adult_status":1,"tour_mark":0}}",
                                                "ts": 4
                                            }
                                        """.trimIndent()
                                        response = DefaultFullHttpResponse(
                                            response.protocolVersion(),
                                            response.status(),
                                            Unpooled.wrappedBuffer(content.toByteArray())
                                        )
                                        clientRequestUUID = null
                                        config.send("防沉迷时间拦截成功!")
                                    }
                                }
                                requestContext.channel().writeAndFlush(response)
                            }
                        })
                    }
                })

            if ("CONNECT".equals(requestInstance.method().name(), ignoreCase = true)) {
                requestContext.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
                requestContext.pipeline().remove("httpCodec")
                return
            }

            bootstrap.connect(host, port).addListener(ChannelFutureListener { future: ChannelFuture ->
                if (future.isSuccess) {
                    future.channel().writeAndFlush(requestInstance)
                } else {
                    requestContext.channel().close()
                }
            })
        } else {
            //HTTPS
            if (channelFuture == null) {
                val bootstrap = Bootstrap()
                bootstrap.group(requestContext.channel().eventLoop())
                    .channel(requestContext.channel().javaClass)
                    .handler(object : ChannelInitializer<Channel>() {
                        @Throws(Exception::class)
                        override fun initChannel(ch: Channel) {
                            ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                                override fun channelRead(ctx0: ChannelHandlerContext, msg: Any) {
                                    requestContext.channel().writeAndFlush(msg)
                                }
                            })
                        }
                    })
                channelFuture = bootstrap.connect(host, port)
                channelFuture!!.addListener(ChannelFutureListener { future: ChannelFuture ->
                    if (future.isSuccess) {
                        future.channel().writeAndFlush(requestInstance)
                    } else {
                        requestContext.channel().close()
                    }
                })
            } else {
                channelFuture!!.channel().writeAndFlush(requestInstance)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        config.send(cause.toString())
        cause.printStackTrace()
    }
}