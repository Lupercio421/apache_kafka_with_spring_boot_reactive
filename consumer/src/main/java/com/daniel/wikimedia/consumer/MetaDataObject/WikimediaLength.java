package com.daniel.wikimedia.consumer.MetaDataObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WikimediaLength {
    @JsonProperty("old")
    public int old;
    @JsonProperty("new")
    public int newLength;
}
