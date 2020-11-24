package com.myorg;

import software.amazon.awscdk.core.App;

import java.util.Arrays;

public class AmbNetworkClientApp {
    public static void main(final String[] args) {
        App app = new App();

        new AmbNetworkClientStack(app, "AmbNetworkClientStack");

        app.synth();
    }
}
