package com.xtracr.realcamera.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xtracr.realcamera.config.BindTarget.BindConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BindConfigJsonAdapterTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BindConfig.class, new BindConfigJsonAdapter())
            .create();

    @Test
    void migratesLegacyRotationFlagToAllAxes() {
        BindConfig config = gson.fromJson(
                """
                {
                  "bindX": true,
                  "bindY": false,
                  "bindZ": true,
                  "bindRotation": true
                }
                """,
                BindConfig.class
        );

        assertEquals(new BindConfig(true, false, true, true, true, true), config);
    }

    @Test
    void readsIndependentRotationAxisFlags() {
        BindConfig expected = new BindConfig(false, true, false, true, false, true);

        BindConfig actual = gson.fromJson(gson.toJson(expected), BindConfig.class);

        assertEquals(expected, actual);
    }
}
