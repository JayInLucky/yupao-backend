package com.luo.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author 13436
 */
@Data
public class TeamQuitRequest implements Serializable {
    /**
     * id
     */
    private Long teamId;

    private static final long serialVersionUID = 1L;
}