package com.luo.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luo.yupao.model.domain.Team;
import com.luo.yupao.model.domain.User;
import com.luo.yupao.model.dto.TeamQuery;
import com.luo.yupao.model.request.TeamJoinRequest;
import com.luo.yupao.model.request.TeamQuitRequest;
import com.luo.yupao.model.request.TeamUpdateRequest;
import com.luo.yupao.model.vo.TeamUserVo;

import java.util.List;


/**
* @author 13436
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-03-24 16:04:20
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);


    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加u人队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除（解散）队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id,User loginUser);
}
