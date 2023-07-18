package com.luo.yupao.once;
import java.util.Date;

import com.luo.yupao.mapper.UserMapper;
import com.luo.yupao.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    // @Scheduled(initialDelay = 5000,fixedDelay = Long.MAX_VALUE)
    public void doInsertUsers(){
        //spring 提供的倒计时工具
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假杰");
            user.setUserAccount("fakeJ");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);;
            user.setTags("[]");
            user.setUserRole(0);
            user.setPlanetCode("111");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

    // public static void main(String[] args) {
    //     new InsertUsers().doInsertUsers();
    // }


}
