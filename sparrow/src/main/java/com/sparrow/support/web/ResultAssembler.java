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
package com.sparrow.support.web;

import com.sparrow.constant.Config;
import com.sparrow.protocol.BusinessException;
import com.sparrow.protocol.Result;
import com.sparrow.utility.CollectionsUtility;
import com.sparrow.utility.ConfigUtility;
import com.sparrow.utility.StringUtility;

public class ResultAssembler {
    public static Result assemble(BusinessException exception, String language) {
        Result result = Result.fail(exception);

        String error = ConfigUtility.getLanguageValue(result.getKey(), language, result.getMessage());
        if (!CollectionsUtility.isNullOrEmpty(exception.getParameters())) {
            error = String.format(error, exception.getParameters().toArray());
        }
        if (exception.getKey().contains(".")) {
            String ctrlName = exception.getKey().split("\\.")[1];
            HttpContext.getContext().put(ctrlName, error);
        }
        result.setMessage(error);
        return result;
    }

    public static Result assemble(Result result) {
        return assemble(result, ConfigUtility.getValue(Config.LANGUAGE));
    }

    public static Result assemble(Result result, String language) {
        String message = ConfigUtility.getLanguageValue(result.getKey(), language, result.getMessage());
        if (StringUtility.isNullOrEmpty(message)) {
            message = result.getMessage();
        }
        result.setMessage(message);
        return result;
    }
}
