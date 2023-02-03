/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.protocol;

import com.sparrow.protocol.constant.Constant;
import java.util.List;

/**
 * 可用于协议 规范服务端返回格式 <p>
 *
 * <p> BusinessException KEY
 * <p>
 * ErrorSupport SPARROW_ERROR name+suffix=key suffix 与界面name 对应 <p>
 * <p> 为什么用该类型？
 * <p>
 * 与异常相比 考虑继承的问题 枚举不可以继承
 * <p>
 * 考虑该类要求稳定不经常修改 不要影响数据协议 考虑第三调用的泛型
 * <p>
 * json 反序列化，set get 方法一定要存在
 */

public class Result<T> implements VO {

    private Result() {
        this.code = Constant.RESULT_OK_CODE;
    }

    public Result(T data, String key) {
        this.code = Constant.RESULT_OK_CODE;
        this.data = data;
        this.key = key;
    }

    public Result(T data) {
        if (data instanceof ErrorSupport) {
            ErrorSupport error = (ErrorSupport) data;
            this.code = error.getCode();
            this.message = error.getMessage();
        } else {
            this.code = Constant.RESULT_OK_CODE;
            this.data = data;
        }
    }

    private Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 错误编码
     */
    private String code;
    /**
     * 错误文本 需要业务自定义获取错误信息获取器
     */
    private String message;
    /**
     * 错误 key= key.suffix
     * <p>
     * <p>
     * suffix==input name
     */
    private String key;
    /**
     * 返回值
     */
    private T data;

    private List<Object> parameters;

    public static Result success() {
        return new Result();
    }

    public static Result success(String message) {
        Result result = new Result();
        result.setMessage(message);
        return result;
    }

    public static Result fail(ErrorSupport errorSupport) {
        return new Result(errorSupport.getCode(), errorSupport.getMessage());
    }

    public static Result fail(BusinessException business) {
        Result result = new Result(business.getCode(), business.getMessage());
        result.key = business.getKey();
        result.parameters = business.getParameters();
        return result;
    }

    public static Result fail() {
        String code = "-1";
        String msg = "system error";
        return new Result(code, msg);
    }

    public T getData() {
        return this.data;
    }

    public boolean isSuccess() {
        return this.code.equals(Constant.RESULT_OK_CODE);
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getKey() {
        return key;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
}
