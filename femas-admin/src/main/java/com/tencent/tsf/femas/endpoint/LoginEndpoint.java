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

package com.tencent.tsf.femas.endpoint;

import static com.tencent.tsf.femas.constant.AdminConstants.PASSWORD;
import static com.tencent.tsf.femas.constant.AdminConstants.USERNAME;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.param.User;
import com.tencent.tsf.femas.util.AESUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/5/10 14:57
 */

@RestController
@RequestMapping("/atom/v1/auth")
public class LoginEndpoint extends AbstractBaseEndpoint {


    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (USERNAME.equalsIgnoreCase(user.getUsername()) && PASSWORD.equalsIgnoreCase(user.getPassword())) {
            return Result.successData(AESUtils.encrypt(user.getUsername() + "-" + user.getPassword()));
        }
        return Result.errorMessage("登陆失败");
    }


}