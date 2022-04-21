package com.tencent.tsf.femas.example.agent.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "femas-springcloud-agent-provider")
//@FeignClient(name = "femas-springcloud-provider", url = "127.0.0.1:19001")
public interface ProviderService {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    String hello();

    @RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
    String echo(@PathVariable("str") String str);

    @RequestMapping(value = "/echo/error/{str}", method = RequestMethod.GET)
    String echoError(@PathVariable("str") String str);

    @RequestMapping(value = "/echo/slow/{str}", method = RequestMethod.GET)
    String echoSlow(@PathVariable("str") String str);
}