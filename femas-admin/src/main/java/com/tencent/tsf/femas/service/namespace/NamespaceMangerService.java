package com.tencent.tsf.femas.service.namespace;

import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.PageService;
import com.tencent.tsf.femas.entity.namespace.*;
import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.service.ServiceExecutor;
import com.tencent.tsf.femas.service.registry.OpenApiFactory;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import com.tencent.tsf.femas.storage.DataOperation;
import com.tencent.tsf.femas.util.ResultCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;


/**
 * @Auther: yrz
 * @Date: 2021/05/07/15:40
 * @Descriptioin
 */
@Service
public class NamespaceMangerService implements ServiceExecutor {

    private final static Logger log = LoggerFactory.getLogger(NamespaceMangerService.class);

    private final DataOperation dataOperation;

    public static final String DEFAULT_NAME = "default-name";

    public static final String DEFAULT_DESC = "default init";

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private RegistryManagerService registryManagerService;

    @Autowired
    private OpenApiFactory factory;

    public NamespaceMangerService(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public Result modifyNamespace(Namespace namespace) {
        int res = dataOperation.modifyNamespace(namespace);
        if(ResultCheck.checkCount(res)){
            return Result.successMessage("编辑成功");
        }
        return Result.errorMessage("编辑失败");
    }

    public Result createNamespace(Namespace namespace) {
        int res = dataOperation.createNamespace(namespace);
        if(ResultCheck.checkCount(res)){
            return Result.successMessage("创建成功");
        }
        return Result.errorMessage("创建失败");
    }

    public Result<PageService<NamespaceVo>> fetchNamespaces(NamespacePageModel namespaceModel){
        PageService<Namespace> namespacePages = dataOperation.fetchNamespaces(namespaceModel);
        if(!CollectionUtil.isEmpty(namespacePages.getData())){
            final List<FutureTask<Void>> tasks = new ArrayList<>();
            for(Namespace ns : namespacePages.getData()){
                FutureTask<Void> task = new FutureTask<Void>(() -> {
                    RegistryPageService registryPageService = (RegistryPageService)registryManagerService.describeRegisterService(ns.getNamespaceId(),null, 1, Integer.MAX_VALUE, null).getData();
                    ns.setServiceCount((registryPageService == null || registryPageService.getCount() == null) ? 0 : registryPageService.getCount());
                    return null;
                });
                executorService.submit(task);
                tasks.add(task);
            }
            tasks.stream().forEach(t -> {
                try {
                    t.get();
                } catch (InterruptedException e) {
                    log.error("queryService  failed  ", e);
                } catch (ExecutionException e) {
                    log.error("queryService  failed  ", e);
                }
            });
        }
        PageService<NamespaceVo> namespaceVoPageService = namespaceToVo(namespacePages);
        return Result.successData(namespaceVoPageService);
    }

    public Result deleteNamespace(String namespaceId){
        int res = dataOperation.deleteNamespaceById(namespaceId);
        if(ResultCheck.checkCount(res)){
            return Result.successMessage("删除成功");
        }
        return Result.errorMessage("删除失败");
    }

    public Result<Namespace> fetchNamespaceById(String namespaceId){
        Namespace namespace = dataOperation.fetchNamespaceById(namespaceId);
        return Result.successData(namespace);
    }

    public void initNamespace(String registryAddress, String namespaceId){
        dataOperation.initNamespace(registryAddress, namespaceId);
    }

    public PageService<NamespaceVo> namespaceToVo(PageService<Namespace> namespacePages){
        PageService<NamespaceVo> namespaceVoPage = new PageService<>();
        namespaceVoPage.setCount(namespacePages.getCount());
        if(namespacePages.getCount() == 0){
            return namespaceVoPage;
        }
        ArrayList<NamespaceVo> res = new ArrayList<>();
        for(Namespace ns : namespacePages.getData()){
            List<RegistryConfig> registryConfigs = new ArrayList<>();
            NamespaceVo vo = NamespaceVo.build(ns, registryConfigs);;
            if(!CollectionUtil.isEmpty(ns.getRegistryId())){
                for(String registryId : ns.getRegistryId()){
                    RegistryConfig config = registryManagerService.getSafetyConfigById(registryId);
                    if(config != null){
                        registryConfigs.add(config);
                    }
                }
            }
            res.add(vo);
        }
        namespaceVoPage.setData(res);
        return namespaceVoPage;
    }
}
