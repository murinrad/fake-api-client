package org.murinrad.fakeapi.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class User {

  @JsonProperty("id")
  private long id;
  @JsonProperty("name")
  private String name;
  @JsonProperty("username")
  private String username;
  @JsonProperty("email")
  private String email;

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append("name: ").append(getName()).append(", ")
        .append("username: ").append(getUsername()).append(", ")
        .append("email: ").append(getEmail())
        .append("}");
    return sb.toString();
  }
}
