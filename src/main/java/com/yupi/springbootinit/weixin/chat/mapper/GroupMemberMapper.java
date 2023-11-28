package com.yupi.springbootinit.weixin.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.weixin.domain.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 群成员表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

}
