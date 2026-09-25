package com.ttn.support.web;

import com.ttn.support.service.AskService;
import com.ttn.support.web.dto.AskRequest;
import com.ttn.support.web.dto.AskResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AskService askService;

    public AiController(AskService askService) {
        this.askService = askService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return askService.ask(request);
    }
}
