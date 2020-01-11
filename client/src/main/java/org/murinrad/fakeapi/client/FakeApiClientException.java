package org.murinrad.fakeapi.client;

public class FakeApiClientException extends Exception {

  private int code;

  public FakeApiClientException(String responseMessage, int code) {
    super(responseMessage);
    this.code = code;
  }

  @Override
  public String toString() {
    return String.format("Response contained a failure status. Response status was : %s, message: %s", code, getMessage());
  }
}
