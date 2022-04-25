package com.tencent.tsf.femas.endpoint.index;

import com.tencent.tsf.femas.util.EnvUtil;
import com.tencent.tsf.femas.util.ResourceFileReadUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cody
 * @date 2021 2021/9/23 2:23 下午
 */
@RestController
public class IndexController {

    @RequestMapping(value = "/index")
    public String index() {
        String FEMAS_BASE_PATH = EnvUtil.getFemasPrefix();
        String template = ResourceFileReadUtil.getResourceAsString("index/index.html");
        return template.replace("${FEMAS_BASE_PATH}", FEMAS_BASE_PATH);
    }
}
