package com.luo.yupao.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luo.yupao.mapper.UserTeamMapper;
import com.luo.yupao.model.domain.UserTeam;
import com.luo.yupao.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 13436
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-03-24 16:05:17
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




