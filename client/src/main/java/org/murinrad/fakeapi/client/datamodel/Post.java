package org.murinrad.fakeapi.client.datamodel;

public class Post {

  private final long id;
  private final String title;

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
    String sb = "{" + "id: " + getId() + ", "
        + "title: " + getTitle()
        + "}";
    return sb;
  }
}
