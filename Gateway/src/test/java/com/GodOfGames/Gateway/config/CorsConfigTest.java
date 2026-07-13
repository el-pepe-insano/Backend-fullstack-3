package com.GodOfGames.Gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    void corsWebFilter_seCreaCorrectamente() {
        CorsConfig config = new CorsConfig();
        CorsWebFilter filter = config.corsWebFilter();
        assertNotNull(filter);
    }
}