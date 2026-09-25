package com.ttn.support.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ttn.support.config.KnowledgeRetryProperties;
import com.ttn.support.rag.RagProperties;

@Configuration
@EnableConfigurationProperties({RagProperties.class, KnowledgeRetryProperties.class})
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:" + serverPort, "http://127.0.0.1:" + serverPort)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger", "/swagger-ui/index.html");
    }
}
