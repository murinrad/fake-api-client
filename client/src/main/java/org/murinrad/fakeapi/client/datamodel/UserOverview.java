package org.murinrad.fakeapi.client.datamodel;

import java.util.ArrayList;
import java.util.List;

public class UserOverview {

  private final String name;
  private final String username;
  private final String email;
  private final List<Post> posts;

  public UserOverview(String name, String username, String email, List<Post> posts) {
    this.posts = new ArrayList<>(posts);
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
    String sb = "{" + "name: " + getName() + ", "
        + "username: " + getUsername() + ", "
        + "email: " + getEmail() + ", "
        + "posts: " + getPosts().toString()
        + "}";
    return sb;
  }
}
