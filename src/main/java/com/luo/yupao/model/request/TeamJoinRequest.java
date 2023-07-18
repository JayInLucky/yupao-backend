package com.luo.yupao.model.request;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户加入队伍请求体
 *
 */
@Data
public class TeamJoinRequest implements Serializable {
    /**
     * id
     */
    private Long teamId;
    /**
     * 密码
     */
    private String password;

    private static final long serialVersionUID = 1L;
}