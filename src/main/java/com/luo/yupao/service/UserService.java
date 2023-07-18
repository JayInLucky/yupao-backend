package com.luo.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luo.yupao.model.domain.User;
import com.luo.yupao.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 13436
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-03-01 09:35:07
*/
public interface UserService extends IService<User> {



    /**
     * 用户注册
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return id 新用户
     */
    long userRegister(String userAccount,String userPassword ,String checkPassword,String planetCode);


    /**
     *  用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request
     * @return  脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);


    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);


    /**
     * 根据标签搜索用户
     * @param tagNameList 用户拥有的标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);


    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
