package com.example.edgecustomer.core.result;

/**
 * 系统级别的响应码枚举
 *
 * Created by Floki on 2017/9/28.
 */
public enum CommonCode implements Code {
    /** 服务器正常接收和处理请求 */
    SUCCESS(200, "操作成功")

    /** 服务器内部错误 */
    , SERVER_INERNAL_ERROR(500, "服务器内部错误，请联系管理员")

    /** Http请求错误 */
    , HTTP_METHOD_ERROR(415, "HTTP方法错误，请选择正确的方法")

    /** 参数错误 */
    , PARAMETER_ERROR(422, "参数错误")

    /** 空数据 */
    , NULL_ERROR(455, "空数据错误")

    /** 重定义 */
    , REDIF_ERROR(466, "重定义错误")

    /** 服务器遇到了一个未曾预料的状况，导致了它无法完成对请求的处理。一般来说，这个问题都会在服务器的程序代码出错时出现 */
    , SYSTEM_ERROR(1000, "系统错误")

    /**上传文件失败 */
    , UPLOAD_FILE_ERROR(10099, "上传文件失败：上传的文件过大")

    /** 由于临时的服务器维护或者过载，服务器当前无法处理请求。这个状况是临时的，并且将在一段时间以后恢复 */
    , SERVICE_UNAVAILABLE(10001, "服务暂停")

    /** 业务异常 */
    , SERVICE_ERROR(10002, "")

    /** 需要授权认证的api接口要求请求方提供授权认证的参数值，而请求方没有提供该参数 */
    , MISSING_PERMISSION_PARAMTER(10006, "缺少访问令牌参数")

    /** 但请求方向服务器获取服务器不支持的媒体类型时会出现该错误，比如系统只支持mp4格式，而请求却获取rm格式的数据 */
    , TOKEN_INVALID(10007, "访问令牌失效")

    /** 请求的api接口不存在 */
    , REQUEST_API_NOT_FOUND(10017, "接口不存在")

    , OVER_AMOUNT(19999,"库存数量不足，无法出库")

    , FILE_TYPE_WRONG(19998,"文件类型错误，请重新上传！")

    , FILE_FORMAT_WRONG(19997,"文件格式错误，请重新上传！")

    , FILE_NULL(19996,"文件为空，请重新上传！")

    , LOGIN_SUCCESS(18001,"登录成功！")

    , USER_NOTEXIT(18002,"用户不存在！请重新输入用户名！")

    , PASSWORD_WRONG(18003,"密码错误！请重新输入密码！")

    , ITEM_WRONG(19996,"该物料已存在，无法新增！")

    ;

    /**
     * 错误代码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    CommonCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) { this.message = message; }
}

