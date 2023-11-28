package com.yupi.springbootinit.weixin.chat.service;


import com.yupi.springbootinit.weixin.domain.entity.Message;
import com.yupi.springbootinit.weixin.domain.entity.msg.ChatMessageResp;
import com.yupi.springbootinit.weixin.domain.vo.request.ChatMessageReq;
import com.yupi.springbootinit.weixin.domain.vo.response.ChatMemberStatisticResp;

/**
 * Description: 消息处理类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-26
 */
public interface ChatService {

    /**
     * 发送消息
     *
     * @param request
     */
    Long sendMsg(ChatMessageReq request, Long uid);
    /**
     * 根据消息获取消息前端展示的物料
     *
     * @param message
     * @param receiveUid 接受消息的uid，可null
     * @return
     */
    ChatMessageResp getMsgResp(Message message, Long receiveUid);
    ChatMemberStatisticResp getMemberStatistic();
}
