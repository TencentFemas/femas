package com.tencent.tsf.femas.governance.circuitbreaker.core.mockservice;

import java.util.concurrent.CompletionStage;

public interface AsyncHelloWorldService {

    CompletionStage<String> returnHelloWorld();

    CompletionStage<String> returnHelloWorldWithName(String name);

    CompletionStage<Void> sayHelloWorld();

    CompletionStage<Void> sayHelloWorldWithName(String name);
}
