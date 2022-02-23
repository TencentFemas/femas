/*
 * Copyright (c) 2021 www.tencent.com.
 * All Rights Reserved.
 * This program is the confidential and proprietary information of
 * www.tencent.com ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with www.tencent.com.
 */   
package com.tencent.tsf.femas.extension.springcloud.ilford.common.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
* <pre>  
* 文件名称：FemasContextConfiguration.java  
* 创建时间：Dec 16, 2021 11:38:10 AM   
* @author juanyinyang  
* 类说明：  
*/
@Configuration
public class FemasContextConfiguration {
    
    private static final Logger LOG = LoggerFactory.getLogger(FemasContextConfiguration.class);
    
    static {
        // 升级到SpringBoot 2.3.x以后，抛异常时返回的message默认被置为了空串，得设置server.error.include-message=always才能显示出message
        // 服务治理功能需要用到message，如果用户没设置server.error.include-message，我们默认将异常message显示出来
        if(StringUtils.isEmpty(System.getProperty("server.error.include-message"))) {
            LOG.info("femas init server.error.include-message=always");
            System.setProperty("server.error.include-message", "always");
        }
    }

}
  