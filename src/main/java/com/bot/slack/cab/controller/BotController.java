package com.bot.slack.cab.controller;

import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.service.BotService;
import com.bot.slack.cab.service.GerminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot")
public class BotController {

    @Autowired
    private BotService botService;

    @Autowired
    GerminiService germiniService;

    @PostMapping(value = "/generate", consumes = "application/json")
    public String GenerateCAB(@RequestBody BotReq req){
        return botService.format(req);
    }

    @GetMapping("/ask")
    public String askGemini(@RequestParam String question) {
        return germiniService.callGemini(question);
    }
}
