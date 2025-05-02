package com.bot.slack.cab.service;

import com.bot.slack.cab.model.BotReq;
import org.springframework.stereotype.Service;

public interface BotService {
    String format(BotReq req);
}
