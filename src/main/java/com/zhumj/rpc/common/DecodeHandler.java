/*
 * Copyright (c) 2001-2020 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.zhumj.rpc.common;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码，netty很好的帮我们解决了拆包的问题，只有生成具体的Object（完整的一个包），才会激活下一个pipeline
 * 未读完的byteBuf会等待下个包的到来，一块进行处理
 * @author 朱梦杰
 * @version V1.0
 * @since 2020-12-17 14:41
 */
public class DecodeHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list)
        throws Exception {

        // 只有拿到完整的头部信息，才进行解码，否则，等待数据到达再进行下一步的处理
        while (byteBuf.readableBytes() >= 104) {
            byte[] headerBytes = new byte[104];
            // 不移动读取的指针，防止读body内容不够的时候，会break，等待下个包的到达后读取，如果指针移动了，将无法获取正确的header了
            byteBuf.getBytes(byteBuf.readerIndex(), headerBytes);

            Header header = SerializeUtil.deserialize(headerBytes, Header.class);

            // 包剩下的内容不够数据的长度的话，不进行处理
            if (byteBuf.readableBytes() < header.getDataLength() + 104) {
                break;
            }
            // body数据够的话，将读取的指针移动到body开始的位置
            byteBuf.readBytes(104); // 此方法会将readerIndex移动到104，然后返回当前readIndex到104的新的buffer
            byte[] bodyBytes = new byte[(int)header.getDataLength()];

            byteBuf.readBytes(bodyBytes);
            // 响应，消费者解码
            if (header.getFlag() == 0x14141424) {
                ResponseBody body = SerializeUtil.deserialize(bodyBytes, ResponseBody.class);
                PackageMessage pkg = new PackageMessage();
                pkg.setHeader(header);
                pkg.setContent(body);
                // 每一个pkg都会触发下一个pipeline
                list.add(pkg);
            }
            // 接收，服务端解码
            if (header.getFlag() == 0x14141414) {
                RequestBody body = SerializeUtil.deserialize(bodyBytes, RequestBody.class);
                PackageMessage pkg = new PackageMessage();
                pkg.setHeader(header);
                pkg.setContent(body);
                // 每一个pkg都会触发下一个pipeline
                list.add(pkg);
            }

        }


    }
}
