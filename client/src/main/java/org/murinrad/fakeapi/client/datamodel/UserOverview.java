package org.murinrad.fakeapi.client.datamodel;

import java.util.ArrayList;
import java.util.List;

public class UserOverview {

  private String name;
  private String username;
  private String email;
  private List<Post> posts;

  public UserOverview(String name, String username, String email, List<Post> posts) {
    this.posts = new ArrayList<Post>(posts);
    this.name = name;
    this.username = username;
    this.email = email;
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

  public List<Post> getPosts() {
    return posts;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append("name: ").append(getName()).append(", ")
        .append("username: ").append(getUsername()).append(", ")
        .append("email: ").append(getEmail()).append(", ")
        .append("posts: ").append(getPosts().toString())
        .append("}");
    return sb.toString();
  }
}
