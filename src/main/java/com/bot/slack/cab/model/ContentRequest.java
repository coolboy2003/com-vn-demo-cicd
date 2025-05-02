package com.bot.slack.cab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ContentRequest {
    public List<Content> contents;

    public ContentRequest(List<Content> contents) {
        this.contents = contents;
    }

    public static class Content {
        public List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
