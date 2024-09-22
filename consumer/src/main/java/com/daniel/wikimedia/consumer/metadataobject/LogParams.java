package com.daniel.wikimedia.consumer.metadataobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogParams {
    @JsonProperty("action")
    public String action;
    @JsonProperty("filter")
    public String filter;
    @JsonProperty("actions")
    public String actions;
    @JsonProperty("log")
    public Long log;
    @JsonProperty("img_sha1")
    public String imgSha1;
    @JsonProperty("img_timestamp")
    public String imgTimestamp;
    @JsonProperty("target")
    private String target;
    @JsonProperty("noredir")
    private String noredir;
    @JsonProperty("curid")
    private String curid;
    @JsonProperty("previd")
    private String previd;
    @JsonProperty("auto")
    private String auto;
}
