package com.yupi.springbootinit.weixin.rocketmq.consumer;


import com.yupi.springbootinit.weixin.chat.adapter.WSAdapter;
import com.yupi.springbootinit.weixin.chat.dao.ContactDao;
import com.yupi.springbootinit.weixin.chat.dao.MessageDao;
import com.yupi.springbootinit.weixin.chat.dao.RoomDao;
import com.yupi.springbootinit.weixin.chat.dao.RoomFriendDao;
import com.yupi.springbootinit.weixin.chat.enums.RoomTypeEnum;
import com.yupi.springbootinit.weixin.chat.service.ChatService;
import com.yupi.springbootinit.weixin.chat.service.PushService;
import com.yupi.springbootinit.weixin.chat.service.cache.GroupMemberCache;
import com.yupi.springbootinit.weixin.chat.service.cache.HotRoomCache;
import com.yupi.springbootinit.weixin.chat.service.cache.RoomCache;
import com.yupi.springbootinit.weixin.chat.service.cache.UserCache;
import com.yupi.springbootinit.weixin.common.contants.MQConstant;
import com.yupi.springbootinit.weixin.domain.dto.MsgSendMessageDTO;
import com.yupi.springbootinit.weixin.domain.entity.Message;
import com.yupi.springbootinit.weixin.domain.entity.Room;
import com.yupi.springbootinit.weixin.domain.entity.RoomFriend;
import com.yupi.springbootinit.weixin.domain.entity.msg.ChatMessageResp;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-12
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP, topic = MQConstant.SEND_MSG_TOPIC)
@Component
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageDao messageDao;

    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;

    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        Message message = messageDao.getById(dto.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        ChatMessageResp msgResp = chatService.getMsgResp(message, null);
        //所有房间更新房间最新消息
        roomDao.refreshActiveTime(room.getId(), message.getId(), message.getCreateTime());
        roomCache.delete(room.getId());
        if (room.isHotRoom()) {//热门群聊推送所有在线的人
            //更新热门群聊时间-redis
            hotRoomCache.refreshActiveTime(room.getId(), message.getCreateTime());
            //推送所有人
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp));
        } else {
            List<Long> memberUidList = new ArrayList<>();
            if (Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType())) {//普通群聊推送所有群成员
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            } else if (Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType())) {//单聊对象
                //对单人推送
                RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            //更新所有群成员的会话时间
            contactDao.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), message.getCreateTime());
            //推送房间成员
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp), memberUidList);
        }
    }


}
