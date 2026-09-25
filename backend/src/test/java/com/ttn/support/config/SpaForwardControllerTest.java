package com.ttn.support.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class SpaForwardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forwardsBrowserRouterDeepLinksToIndexWhenStaticShellExists() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());

        mockMvc.perform(get("/tickets")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));

        mockMvc.perform(get("/tickets/TKT-1001"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));

        mockMvc.perform(get("/ask")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void doesNotInterceptApiSwaggerOrOpenApiRoutes() throws Exception {
        mockMvc.perform(get("/api/tickets").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());

        mockMvc.perform(get("/swagger"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }
}
