package com.luo.yupao.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类（脱敏）
 */
@Data
public class TeamUserVo implements Serializable {

    private static final long serialVersionUID = 2606242941233427192L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */

    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;



    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户列表
     */
    UserVo createUser;

    /**
     * 已加入的用户数
     */
    private Integer hasJoinNum;

    /**
     *
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

}
