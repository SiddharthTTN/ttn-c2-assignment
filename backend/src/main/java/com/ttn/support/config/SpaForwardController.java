package com.ttn.support.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Forwards browser-router deep links to the packaged React shell when {@code static/index.html}
 * is present. API, Swagger, and static asset paths are intentionally not mapped here.
 */
@Controller
public class SpaForwardController {

    private final boolean spaAvailable;

    public SpaForwardController(ResourceLoader resourceLoader) {
        Resource index = resourceLoader.getResource("classpath:/static/index.html");
        spaAvailable = index.exists();
    }

    @GetMapping(path = {"/", "/tickets", "/tickets/**", "/ask"})
    public String forwardSpaRoutes() {
        if (!spaAvailable) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "forward:/index.html";
    }
}
