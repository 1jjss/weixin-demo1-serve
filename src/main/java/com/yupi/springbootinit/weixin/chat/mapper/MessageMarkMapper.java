package com.yupi.springbootinit.weixin.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.weixin.domain.entity.MessageMark;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息标记表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-04-08
 */
@Mapper
public interface MessageMarkMapper extends BaseMapper<MessageMark> {

}
