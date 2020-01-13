package org.murinrad.fakeapi.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class Post {

  @JsonProperty("id")
  private long id;
  @JsonProperty("title")
  private String title;

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    String sb = "{" + "id: " + getId() + ", "
        + "title: " + getTitle()
        + "}";
    return sb;
  }
}
