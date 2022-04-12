package com.tencent.tsf.femas.endpoint.log;

import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.endpoint.adaptor.AbstractBaseEndpoint;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.Record;
import com.tencent.tsf.femas.entity.log.LogModel;
import com.tencent.tsf.femas.enums.LogModuleEnum;
import com.tencent.tsf.femas.storage.DataOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Auther: yrz
 * @Date: 2021/05/25/15:57
 * @Descriptioin
 */
@RestController
@RequestMapping("/atom/v1/log")
@Api(tags = "日志模块")
public class LogEndpoint extends AbstractBaseEndpoint {

    private final DataOperation dataOperation;

    public LogEndpoint(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    @PostMapping("fetchLogs")
    @ApiOperation("查询操作日志")
    public Result<PageService<Record>> fetchLogs(@RequestBody LogModel logModel) {
        return executor.process(() -> {
            PageService<Record> res = dataOperation.fetchLogs(logModel);
            return Result.successData(res);
        });
    }

    @GetMapping("fetchModuleType")
    public Result<List<String>> fetchModuleType() {
        ArrayList<String> modules = new ArrayList<>();
        for (LogModuleEnum value : LogModuleEnum.values()) {
            modules.add(value.name());
        }
        return Result.successData(modules);
    }

}
