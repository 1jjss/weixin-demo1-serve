package com.yupi.springbootinit.weixin.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.weixin.domain.entity.RoomFriend;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 单聊房间表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-22
 */
@Mapper
public interface RoomFriendMapper extends BaseMapper<RoomFriend> {

}
