package com.luo.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luo.yupao.common.ErrorCode;
import com.luo.yupao.exception.BusinessException;
import com.luo.yupao.model.domain.User;
import com.luo.yupao.model.vo.UserVo;
import com.luo.yupao.service.UserService;
import com.luo.yupao.mapper.UserMapper;
import com.luo.yupao.utils.AlgorithmUtils;
import io.swagger.models.auth.In;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.crypto.spec.OAEPParameterSpec;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.luo.yupao.contant.UserConstant.ADMIN_ROLE;
import static com.luo.yupao.contant.UserConstant.USER_LOGIN_STATE;


/**
* @author 13436
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-03-01 09:35:07
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT="luo";

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账户长度小于4位");
        }
        if (userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码小于8位");
        }
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号长度大于5位");
        }

        //账户不能包含特殊字符
        String validPattern="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
        }
        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"校验密码与密码不相同");
        }

        // 账户不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count=userMapper.selectCount(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }

        // 星球标号不能重复
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count=userMapper.selectCount(queryWrapper);
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号重复");
        }

        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setUsername(userAccount);
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SAVE_ERROR,"插入数据失败");
        }
        return user.getId();

    }

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        if (userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度小于8");
        }

        //账户不能包含特殊字符
        String validPattern="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户出现特殊字符");
        }

        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user=userMapper.selectOne(queryWrapper);

        //用户不存在
        if (user==null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在");
        }

        // 3. 用户脱敏
        User safetyUser=getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;

    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if (originUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户为空");
        }

        User safetyUser =new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;

    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户(内存查询)
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            //代替for循环，数组的每一项都会执行该逻辑，返回 false 就是被过滤掉， true 就是保留
            String tagsStr = user.getTags();
            //转换成列表类型   把 tagsStr 反序列化成对象。
            // Set 集合 的contains 效率  O（1） 要比 list 列表 的contains效率要高 O(n)
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
            //任何一个集合都要判空，  Optional可选类。  链式调用。用ofNullable去封装一个可能为空的对象，然后再用 orElse 给对象一个默认值。
            // 如果tempTagNameSet 为空的话， 那么我们取得的值是orElse里的值new HashSet<>()
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }



    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (user==loginUser){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request==null){
            return null;
        }
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user==null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) user;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可查询
        Object userObj=request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList=this.list(queryWrapper);
        String tags=loginUser.getTags();
        Gson gson=new Gson();
        List<String> tagList=gson.fromJson(tags, new TypeToken<List<String>>(){
        }.getType());
        //用户列表的下表 ==> 相似度
        List<Pair<User,Long>> list= new ArrayList<>();

        //依次计算所有用户和当前用户得相似度
        for (int i = 0; i < userList.size(); i++) {
            User user=userList.get(i);
            String userTags=user.getTags();
            // 无标签 或者 为 当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() .equals(loginUser.getId()) ) {
                continue;
        }
            List<String> userTagList = gson.fromJson(userTags,new TypeToken<List<String>>(){
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList,userTagList);
            list.add(new Pair<>(user,distance));
        }
        // 按编程距离 由小到大
        List<Pair<User,Long>> topUserPairList=list.stream()
                .sorted((a,b) -> (int) (a.getValue() -b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Long> userIdList=topUserPairList.stream().map(pair->pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper=new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        // 1, 3, 2
        // User1 ,User2 ,User3
        // 1 => User1 ,2 => User2 ,3 => User3
        Map<Long,List<User>> userIfUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user->getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList=new ArrayList<>();
        for (Long userId : userIdList){
            finalUserList.add(userIfUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户(SQL查询)
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1. 先查询所有用户
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
         //拼接 and 查询
         //like '%java%' and like '%Python%'
         for(String tagName : tagNameList){
            queryWrapper= queryWrapper.like("tags",tagName);
         }
          List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}




