package com.daniel.wikimedia.consumer.MetaDataObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Meta{
    @JsonProperty("uri")
    public String uri;
    @JsonProperty("request_id")
    public String request_id;
    @JsonProperty("id")
    public String id;
    @JsonProperty("dt")
    public Date dt;
    @JsonProperty("domain")
    public String domain;
    @JsonProperty("stream")
    public String stream;
    @JsonProperty("topic")
    public String topic;
    @JsonProperty("partition")
    public int partition;
    @JsonProperty("offset")
    public long offset;
}
