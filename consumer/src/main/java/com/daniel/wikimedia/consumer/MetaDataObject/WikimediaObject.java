package com.daniel.wikimedia.consumer.MetaDataObject;
import com.fasterxml.jackson.annotation.JsonProperty;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */

import lombok.Data;

@Data
public class WikimediaObject{
    @JsonProperty("$schema")
    public String $schema;
    @JsonProperty("meta")
    public Meta meta;
    @JsonProperty("id")
    public int id;
    @JsonProperty("type")
    public String type;
    @JsonProperty("namespace")
    public int namespace;
    @JsonProperty("title")
    public String title;
    @JsonProperty("title_url")
    public String title_url;
    @JsonProperty("comment")
    public String comment;
    @JsonProperty("timestamp")
    public int timestamp;
    @JsonProperty("user")
    public String user;
    @JsonProperty("bot")
    public boolean bot;
    @JsonProperty("notify_url")
    public String notify_url;
    @JsonProperty("server_url")
    public String server_url;
    @JsonProperty("server_name")
    public String server_name;
    @JsonProperty("server_script_path")
    public String server_script_path;
    @JsonProperty("wiki")
    public String wiki;
    @JsonProperty("parsedcomment")
    public String parsedcomment;
}


