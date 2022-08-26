package com.lyd.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lyd.common.ResultCode;

/**
 * @author 天狗
 * @desc 接口统一返回包装类
 */
@Data
@NoArgsConstructor
//@AllArgsConstructor
public class Result {

    private static final long serialVersionUID = 1725159680599612404L;

    private String code;
    private String msg;
    private Object data;
    private String total;
//    private String jwtToken;

    public Result(String code, String msg, Object data, String total) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.total = total;
//        this.jwtToken = null;
    }
    public Result(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.total = null;
//        this.jwtToken = null;
    }

    public static Result success(){
        return new Result(Constants.CODE_200,"",null,null);
    }

    public static Result reportSuccess() {
        return new Result(Constants.CODE_200,"您已成功举报",null,null);
    }

    public static Result banUserSuccess() {
        return new Result(Constants.CODE_200,"您已成功封禁该用户",null,null);
    }

    public static Result success(Object data){
        return new Result(Constants.CODE_200,"",data,null);
    }
    public static Result success(Object data,Long total) {
        return new Result(Constants.CODE_200,null,data,total.toString());
    }
//    public static Result success(Object data,String jwtToken) {
//        return new Result(Constants.CODE_200,null,data,null,jwtToken);
//    }

    public static Result error(String code,String msg){
        return new Result(code,msg,null,null);
    }

    public static Result error(String code401, ResultCode userNotLogin){
        return new Result(Constants.CODE_500,"系统错误",null,null);
    }



}
