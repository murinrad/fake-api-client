package org.murinrad.fakeapi.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.validator.routines.UrlValidator;
import org.murinrad.fakeapi.client.datamodel.UserOverview;

final class FakeAPIClientImpl implements FakeAPIClient, AutoCloseable {

  private static final String USER_ENDPOINT = "/users/";
  private static final String POSTS_ENDPOINT = "/posts/";
  private static final String POSTS_BY_USER_ENDPOINT_TEMPLATE = "/posts?userId=%s";
  private final ObjectMapper objectMapper;
  private final OkHttpClient client;
  private final JavaType postListType;
  private final String apiHost;

  FakeAPIClientImpl(String apiBaseURL) {
    if (!UrlValidator.getInstance().isValid(apiBaseURL)) {
      throw new IllegalArgumentException("Base URL parameter is invalid");
    }
    if (apiBaseURL.endsWith("/")) {
      int lastSlash = apiBaseURL.lastIndexOf('/');
      apiBaseURL = apiBaseURL.substring(0, lastSlash);
    }
    this.apiHost = apiBaseURL;
    objectMapper = new ObjectMapper();
    client = new OkHttpClient();
    postListType = objectMapper.getTypeFactory().constructCollectionType(List.class, Post.class);
  }

  FakeAPIClientImpl(String apiBaseURL, int maxRequestsPerHost, int maxRequests) {
    this(apiBaseURL);
    Dispatcher customDispatcher = new Dispatcher();
    customDispatcher.setMaxRequestsPerHost(maxRequestsPerHost);
    customDispatcher.setMaxRequests(maxRequests);
    client.setDispatcher(customDispatcher);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserOverview retrieveUserOverview(long id) throws IOException, FakeApiClientException {
    User userData = retrieveUser(id);
    List<Post> userPosts = retrievePostsAssociatedWithUser(id);
    return mapToUserOverview(userData, userPosts);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Future<UserOverview> retrieveUserOverviewAsync(long id) {
    final CompletableFuture<User> userData = retrieveUserAsync(id);
    final CompletableFuture<List<Post>> userPosts = retrievePostsAssociatedWithUserAsync(id);
    final CompletableFuture<UserOverview> future = new CompletableFuture<>();
    userData.thenAcceptBoth(userPosts, (user, posts) ->
        future.complete(mapToUserOverview(user, posts)))
        .exceptionally(throwable -> {
          future.completeExceptionally(throwable);
          return null;
    });
    return future;

  }

  User retrieveUser(long id) throws IOException, FakeApiClientException {
    Request userRequest = new Request.Builder()
        .url(apiHost + USER_ENDPOINT + id).build();
    Response response = executeRequest(userRequest);
    return objectMapper.readValue(response.body().bytes(), User.class);
  }

  CompletableFuture<User> retrieveUserAsync(long id) {
    Request userRequest = new Request.Builder()
        .url(apiHost + USER_ENDPOINT + id).build();
    CompletableFuture<User> future = new CompletableFuture<>();
    executeRequestAsync(userRequest,
        new APIClientCallback<>(future, bytes -> objectMapper.readValue(bytes, User.class)));
    return future;
  }

  CompletableFuture<List<Post>> retrievePostsAssociatedWithUserAsync(long userID) {
    Request request = new Request.Builder().get()
        .url(apiHost + String.format(POSTS_BY_USER_ENDPOINT_TEMPLATE, userID)).build();
    CompletableFuture<List<Post>> future = new CompletableFuture<>();
    executeRequestAsync(request, new APIClientCallback<>(future,
        bytes -> objectMapper.readValue(bytes, postListType)));
    return future;
  }

  List<Post> retrievePostsAssociatedWithUser(long userID) throws IOException, FakeApiClientException {
    Request request = new Request.Builder().get()
        .url(apiHost + String.format(POSTS_BY_USER_ENDPOINT_TEMPLATE, userID)).build();
    Response response = executeRequest(request);
    return objectMapper.readValue(response.body().bytes(), postListType);
  }

  private Response executeRequest(Request req) throws IOException, FakeApiClientException {
    Call call = client.newCall(req);
    Response response = call.execute();
    checkResponseStatus(response);
    return response;
  }

  private void executeRequestAsync
      (Request req, APIClientCallback callback) {
    Call call = client.newCall(req);
    call.enqueue(callback);
  }

  private static void checkResponseStatus(Response response) throws FakeApiClientException {
    if (!response.isSuccessful()) {
      String message = response.message();
      if (message == null) {
        message = "<SERVER RETURNED NO MESSAGE>";
      }
      throw new FakeApiClientException(message, response.code());
    }
  }

  private static UserOverview mapToUserOverview(User userData, List<Post> userPosts) {
    return new UserOverview(
        userData.getName(), userData.getUsername(), userData.getEmail(), userPosts.stream().map(x ->
        new org.murinrad.fakeapi.client.datamodel.Post(x.getId(), x.getTitle())).collect(Collectors.toList())
    );
  }

  /**
   * Closes the okhttp dispatcher. Immediately releases executor threads.
   * Further calls will be rejected
   * The client threads will die eventually. But this is here if the resources need
   * to be released immediately. e.g.: a CLI invocation
   */
  @Override
  public void close() {
    if (!client.getDispatcher().getExecutorService().isShutdown()) {
      client.getDispatcher().getExecutorService().shutdown();
    }
  }

  private static class APIClientCallback<T> implements Callback {

    private final CompletableFuture<T> future;
    private final CheckedFunction<byte[], T> mappingFunction;

    private APIClientCallback(CompletableFuture<T> future, CheckedFunction<byte[], T> mappingFunction) {
      this.future = future;
      this.mappingFunction = mappingFunction;

    }

    @Override
    public void onFailure(Request request, IOException e) {
      parseResponse(null, e);
    }

    @Override
    public void onResponse(Response response) {
      parseResponse(response, null);
    }

    void parseResponse(Response res, Exception ex) {
      if (ex != null) {
        future.completeExceptionally(ex);
      }
      try {
        checkResponseStatus(res);
        future.complete(mappingFunction.apply(res.body().bytes()));
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    }
  }

  private interface CheckedFunction<T, R> {

    R apply(T object) throws IOException;

  }
}
