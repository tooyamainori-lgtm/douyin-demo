package com.douyin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求参数
 */
@Data
public class UpdateProfileDTO {

    /** 昵称 */
    @Size(min = 1, max = 30, message = "昵称长度必须为 1-30 个字符")
    private String nickname;

    /** 个人简介 */
    @Size(max = 200, message = "个人简介不能超过 200 个字符")
    private String bio;

    /** 性别：0-未知 1-男 2-女 */
    @Min(value = 0, message = "性别参数不正确")
    @Max(value = 2, message = "性别参数不正确")
    private Integer gender;

    /** 生日 */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式必须为 yyyy-MM-dd")
    private String birthday;
}
