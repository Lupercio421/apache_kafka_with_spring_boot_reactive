package com.daniel.wikimedia.consumer.metadataobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WikimediaRevision {
    @JsonProperty("old")
    public long old;
    @JsonProperty("new")
    public long newRevision;
}
