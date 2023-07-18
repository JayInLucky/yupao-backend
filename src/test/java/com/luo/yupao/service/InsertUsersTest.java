package com.luo.yupao.service;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.luo.yupao.mapper.UserMapper;
import com.luo.yupao.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

     //CPU 密集型： 分配的核心线程数 = CPU -1
     // IO 密集型： 分配的核心线程数可以大于 CPU 核数
     private ExecutorService executorService=new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    /**
     * 批量插入用户
     */
    // @Scheduled(initialDelay = 5000,fixedDelay = Long.MAX_VALUE)
    public void doInsertUser1(){
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
        // 3秒多
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }


    /**
     * 批量插入用户
     */
    public void doInsertUsers2(){
        //spring 提供的倒计时工具
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("洛克李");
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
            userList.add(user);
        }
        //    2秒
        userService.saveBatch(userList,100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers(){
        //spring 提供的倒计时工具
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("洛克李");
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
            userList.add(user);
        }
        //  14秒 10 万条
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers(){
        //spring 提供的倒计时工具
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        //分十组
        int batchSize=2500;
        int j = 0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("加斯科");
                user.setUserAccount("faeJ");
                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);;
                user.setTags("[]");
                user.setUserRole(0);
                user.setPlanetCode("111");
                userList.add(user);
                if (j % batchSize == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
                System.out.println(" threadName :"+Thread.currentThread().getName());
               userService.saveBatch(userList,batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        // 6.365 s
        // 5.902 s  int batchSize=5000;
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
