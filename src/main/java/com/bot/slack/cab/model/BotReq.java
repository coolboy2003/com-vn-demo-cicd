package com.bot.slack.cab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
public class BotReq {
    private String summary;
    private List<ServiceDeployment> deploymentServices;
    private String ticket;
}
