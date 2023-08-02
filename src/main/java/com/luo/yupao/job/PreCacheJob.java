package com.luo.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luo.yupao.model.domain.User;
import com.luo.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户 （缓存 用户id为1 的）， 这里不要写死， 动态读取，
    private List<Long> mainUserList= Arrays.asList(1L);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 4 0 * * *")   // 每一小时缓存一次
    public void doCacheRecommendUser(){

        RLock lock=redissonClient.getLock("yupao:precachejob:docache:lock");

        try {
            //只有一个线程能获取到锁   timeout:锁一定要加过期时间
            // waitTime一定是 0 ：每天只能允许一个人，执行一次 ， 只要第一次没获取到，就放弃。
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getlock:"+Thread.currentThread().getId());
                for (Long userId : mainUserList){
                    QueryWrapper<User> queryWrapper=new QueryWrapper<>();
                    // 假设缓存，一页，20条数据
                    Page<User> userPage=userService.page(new Page<>(1,20),queryWrapper);
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();

                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }
                // 不要放在try里， 代码中途报错的话，锁无法释放。
//                if (lock.isHeldByCurrentThread()) {
//                    System.out.println("unlock:"+Thread.currentThread().getId());
//                    //用完锁要释放
//                    lock.unlock();
//                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        }finally {
            // 只能释放自己的锁。 判断当前的锁是不是这个线程加的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock:"+Thread.currentThread().getId());
                //用完锁要释放
                lock.unlock();
            }
        }
    }
}
