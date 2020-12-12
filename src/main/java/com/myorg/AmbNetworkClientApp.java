package com.myorg;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

import java.util.Arrays;

public class AmbNetworkClientApp {

    // static Environment makeEnv(String account, String region) {
    //     return Environment.builder()
    //             .account(account)
    //             .region(region)
    //             .build();
    // }
    
    public static void main(final String[] args) {
        App app = new App();
        
        // Environment envUS = makeEnv("036842120113", "us-east-1");
        // new AmbNetworkClientStack(app, "AmbNetworkClientStack", StackProps.builder()
        // .env(envUS).build());

        new AmbNetworkClientStack(app, "AmbNetworkClientStack");
        app.synth();
    }
}
