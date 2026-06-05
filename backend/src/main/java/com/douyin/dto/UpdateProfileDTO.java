package com.douyin.dto;

import lombok.Data;

/**
 * 更新个人资料请求参数
 */
@Data
public class UpdateProfileDTO {

    /** 昵称 */
    private String nickname;

    /** 个人简介 */
    private String bio;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 生日 */
    private String birthday;
}
