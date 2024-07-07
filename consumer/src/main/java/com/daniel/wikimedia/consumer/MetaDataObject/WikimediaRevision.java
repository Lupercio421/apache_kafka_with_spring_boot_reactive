package com.daniel.wikimedia.consumer.MetaDataObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class WikimediaRevision {
    @JsonProperty("old")
    public long old;
    @JsonProperty("new")
    public long newRevision;
}
