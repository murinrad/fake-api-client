package org.murinrad.fakeapi.client.datamodel;

public class Post {

  private long id;
  private String title;

  public Post(long id, String title) {
    this.id = id;
    this.title = title;
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append("id: ").append(getId()).append(", ")
        .append("title: ").append(getTitle())
        .append("}");
    return sb.toString();
  }
}
