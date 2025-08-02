package com.daniel.wikimedia.consumer.metadataobject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WikimediaObject{
    @JsonProperty("$schema")
    public String $schema;
    @JsonProperty("meta")
    public Meta meta;
    @JsonProperty("id")
    public long id;
    @JsonProperty("type")
    public String type;
    @JsonProperty("namespace")
    public int namespace;
    @JsonProperty("title")
    public String title;
    @JsonProperty("title_url")
    public String titleUrl;
    @JsonProperty("comment")
    public String comment;
    @JsonProperty("timestamp")
    public int timestamp;
    @JsonProperty("user")
    public String user;
    @JsonProperty("bot")
    public boolean bot;
    @JsonProperty("notify_url")
    public String notifyUrl;
    @JsonProperty("minor")
    public boolean minor;
    @JsonProperty("patrolled")
    public boolean patrolled;
    @JsonProperty("length")
    public WikimediaLength length;
    @JsonProperty("revision")
    public WikimediaRevision revision;
    @JsonProperty("server_url")
    public String serverUrl;
    @JsonProperty("server_name")
    public String serverName;
    @JsonProperty("server_script_path")
    public String serverScriptPath;
    @JsonProperty("wiki")
    public String wiki;
    @JsonProperty("parsedcomment")
    public String parsedComment;
    @JsonProperty("log_id")
    public Long logId;
    @JsonProperty("log_type")
    public String logType;
    @JsonProperty("log_action")
    public String logAction;
    @JsonProperty("log_action_comment")
    public String logActionComment;
    @JsonProperty("log_params")
    public LogParams logParams;
}


