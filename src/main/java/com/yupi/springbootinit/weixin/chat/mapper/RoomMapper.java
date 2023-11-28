package com.yupi.springbootinit.weixin.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.weixin.domain.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 房间表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

}
