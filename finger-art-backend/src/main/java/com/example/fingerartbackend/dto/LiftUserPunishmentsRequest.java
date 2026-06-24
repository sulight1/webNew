package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * 解除用户处罚请求 DTO。
 * 管理员按类型撤销用户的一项或多项处罚。
 */
@Data
public class LiftUserPunishmentsRequest {
    /** 要解除的处罚类型列表 */
    private List<String> types;
}
