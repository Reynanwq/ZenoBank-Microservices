package com.zenobank.u7;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "greeting")
public interface GreetingConfig {

    //Serve para ler configurações do application.properties de forma tipada.

    @WithName("message")
    String message();

}