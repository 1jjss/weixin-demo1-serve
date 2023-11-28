package com.yupi.springbootinit.weixin.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.yupi.springbootinit.weixin.chat.adapter.MessageAdapter;
import com.yupi.springbootinit.weixin.chat.dao.GroupMemberDao;
import com.yupi.springbootinit.weixin.chat.dao.MessageMarkDao;
import com.yupi.springbootinit.weixin.chat.service.ChatService;
import com.yupi.springbootinit.weixin.chat.service.cache.RoomCache;
import com.yupi.springbootinit.weixin.chat.service.cache.RoomGroupCache;
import com.yupi.springbootinit.weixin.chat.service.cache.UserCache;
import com.yupi.springbootinit.weixin.common.event.MessageSendEvent;
import com.yupi.springbootinit.weixin.common.utils.AssertUtil;
import com.yupi.springbootinit.weixin.domain.entity.GroupMember;
import com.yupi.springbootinit.weixin.domain.entity.Message;
import com.yupi.springbootinit.weixin.domain.entity.MessageMark;
import com.yupi.springbootinit.weixin.domain.entity.Room;
import com.yupi.springbootinit.weixin.domain.entity.msg.ChatMessageResp;
import com.yupi.springbootinit.weixin.domain.entity.msg.RoomGroup;
import com.yupi.springbootinit.weixin.domain.vo.request.ChatMessageReq;
import com.yupi.springbootinit.weixin.domain.vo.response.ChatMemberStatisticResp;
import com.yupi.springbootinit.weixin.service.strategy.AbstractMsgHandler;
import com.yupi.springbootinit.weixin.service.strategy.MsgHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 消息处理类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-26
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    public static final long ROOM_GROUP_ID = 1L;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @Autowired
    private RoomCache roomCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private UserCache userCache;
    /**
     * 发送消息
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        check(request, uid);
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        System.out.println(Thread.currentThread().getName());
        Long onlineNum = userCache.getOnlineNum();
//        Long offlineNum = userCache.getOfflineNum();不展示总人数
        ChatMemberStatisticResp resp = new ChatMemberStatisticResp();
        resp.setOnlineNum(onlineNum);
//        resp.setTotalNum(onlineNum + offlineNum);
        return resp;
    }

    public List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long receiveUid) {
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        //查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, msgMark, receiveUid);
    }
    private void check(ChatMessageReq request, Long uid) {
        Room room = roomCache.get(request.getRoomId());
        if (room.isHotRoom()) {//全员群跳过校验
            return;
        }
        if (room.isRoomFriend()) {
//            RoomFriend roomFriend = roomFriendDao.getByRoomId(request.getRoomId());
//            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
//            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }

    }

}
