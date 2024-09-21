package com.daniel.wikimedia.consumer.MetaDataObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogParams {
    @JsonProperty("action")
    public String action;
    @JsonProperty("filter")
    private String filter;
    @JsonProperty("actions")
    private String actions;
    @JsonProperty("log")
    private Long log;
}
