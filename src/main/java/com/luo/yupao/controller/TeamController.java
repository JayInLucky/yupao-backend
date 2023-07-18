package com.luo.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.yupao.common.BaseResponse;
import com.luo.yupao.common.DeleteRequest;
import com.luo.yupao.common.ErrorCode;
import com.luo.yupao.common.ResultUtils;
import com.luo.yupao.exception.BusinessException;
import com.luo.yupao.model.domain.Team;
import com.luo.yupao.model.domain.User;
import com.luo.yupao.model.domain.UserTeam;
import com.luo.yupao.model.dto.TeamQuery;
import com.luo.yupao.model.request.TeamAddRequest;
import com.luo.yupao.model.request.TeamJoinRequest;
import com.luo.yupao.model.request.TeamQuitRequest;
import com.luo.yupao.model.request.TeamUpdateRequest;
import com.luo.yupao.model.vo.TeamUserVo;
import com.luo.yupao.service.TeamService;
import com.luo.yupao.service.UserService;
import com.luo.yupao.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = {"http://localhost:3000"})
@CrossOrigin(origins = {"http://127.0.0.1:3000"})
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser= userService.getLoginUser(request);
        boolean save = teamService.deleteTeam(id,loginUser);
        if(!save){
            throw new   BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean save = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!save){
            throw new   BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }


    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        //查询队伍列表
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        //判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        try {
            User loginUser= userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId()).in("teamId",teamIdList);
//            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList=userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){}
        //3 查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍 id => 加入这个队伍的用户列表
        Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> resultPage=teamService.page(page,queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result=teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest   request){
        if (teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser=userService.getLoginUser(request);
        boolean result=teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser=userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser=userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList=userTeamService.list(queryWrapper);
        //取出不重复的队伍 id
        // teamId userId
        //  1   ,   2
        //  1   ,   3
        //  2   ,   3
        //   result
        //  1  ==> 2 , 3
        //  2  ==> 3

        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList=new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }


}
