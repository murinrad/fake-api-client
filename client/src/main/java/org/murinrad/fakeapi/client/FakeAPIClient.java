package org.murinrad.fakeapi.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.validator.routines.UrlValidator;
import org.murinrad.fakeapi.client.datamodel.UserOverview;

/**
 * Api client for the Fake online REST API for developers site
 * See inner static Factory class for instantiation
 */
public class FakeAPIClient {

  private static final String USER_ENDPOINT = "/users/";
  private static final String POSTS_ENDPOINT = "/posts/";
  private static final String POSTS_BY_USER_ENDPOINT_TEMPLATE = "/posts?userId=%s";
  private final ObjectMapper objectMapper;
  private final OkHttpClient client;
  private final JavaType postListType;
  private final String apiHost;

  private FakeAPIClient(String apiBaseURL) {
    if (!UrlValidator.getInstance().isValid(apiBaseURL)) {
      throw new IllegalArgumentException("Base URL parameter is invalid");
    }
    if (apiBaseURL.endsWith("/")) {
      int lastSlash = apiBaseURL.lastIndexOf('/');
      apiBaseURL = apiBaseURL.substring(0, lastSlash);
    }
    this.apiHost = apiBaseURL;
    objectMapper = new ObjectMapper();
    client =  new OkHttpClient();
    postListType = objectMapper.getTypeFactory().constructCollectionType(List.class, Post.class);
  }

  /**
   * Retrieves a User overview
   * @param id the id of the user
   * @return UserOverview, a sum of user info and posts associated to the user
   * @throws IOException if a transport level error occurs
   * @throws FakeApiClientException if the server returns an unexpected status
   */
  public UserOverview retrieveUserOverview(long id) throws IOException, FakeApiClientException {
    User userData = retrieveUser(id);
    List<Post> userPosts = retrievePostsAssociatedWithUser(id);
    UserOverview userOverview = new UserOverview(
        userData.getName(), userData.getUsername(), userData.getEmail(), userPosts.stream().map(x ->
        new org.murinrad.fakeapi.client.datamodel.Post(x.getId(), x.getTitle())).collect(Collectors.toList())
    );
    return userOverview;
  }

  User retrieveUser(long id) throws IOException, FakeApiClientException {
    Request userRequest = new Request.Builder()
        .url(apiHost + USER_ENDPOINT + id).build();
    Response response = executeRequest(userRequest);
    return objectMapper.readValue(response.body().bytes(), User.class);
  }

  List<Post> retrievePostsAssociatedWithUser(long userID) throws IOException, FakeApiClientException {
    Request userRequest = new Request.Builder().get()
        .url(apiHost + String.format(POSTS_BY_USER_ENDPOINT_TEMPLATE, userID)).build();
    Response response = executeRequest(userRequest);
    return objectMapper.readValue(response.body().bytes(), postListType);
  }

  private Response executeRequest(Request req) throws IOException, FakeApiClientException {
    Call call = client.newCall(req);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      String message = response.message();
      if (message == null) {
        message = "<SERVER RETURNED NO MESSAGE>";
      }
      throw new FakeApiClientException(message, response.code());
    }
    return response;
  }

  public static class Factory {

    /**
     * Creates a default FakeAPI Client, for transport details see OKHttp documentation.
     * @param apiBaseURL the base URL of the API eg: http://foobar.com
     * @throws IllegalArgumentException when the URL provided is invalid or empty
     * @return The API client
     */
    public static FakeAPIClient create(String apiBaseURL) {
      return new FakeAPIClient(apiBaseURL);
    }

  }
}
