package org.murinrad.fakeapi.client;

import com.squareup.okhttp.Dispatcher;
import java.io.IOException;
import java.util.concurrent.Future;
import org.murinrad.fakeapi.client.datamodel.UserOverview;

/**
 * @see Factory class for instantiation
 * Api client for the Fake online REST API for developers site.
 */
public interface FakeAPIClient extends AutoCloseable {

  /**
   * Retrieves a User overview
   *
   * @param id the id of the user
   * @return UserOverview, a sum of user info and posts associated to the user
   * @throws IOException if a transport level error occurs
   * @throws FakeApiClientException if the server returns an unexpected status
   */
  UserOverview retrieveUserOverview(long id) throws IOException, FakeApiClientException;

  /**
   * Retrieves a User overview asynchronously
   * Calling apis asynchronously will spawn multiple daemon threads.
   * Should you desire to close these threads consider using the client in a
   * try-with-resources block.
   *
   * @param id the id of the user
   * @return a future of UserOverview, a sum of user info and posts associated to the user
   */
  Future<UserOverview> retrieveUserOverviewAsync(long id);

  /**
   * Closes the connection
   * Normally clients do not need to call this method.
   * Use only if you need to release the used resources immediately.
   */
  @Override
  void close();

  class Factory {

    /**
     * Creates a default FakeAPI Client, for transport details see OKHttp documentation.
     *
     * @param apiBaseURL the base URL of the API eg: http://foobar.com
     * @return The API client
     * @throws IllegalArgumentException when the URL provided is invalid or empty
     */
    public static FakeAPIClient create(String apiBaseURL) {
      return new FakeAPIClientImpl(apiBaseURL);
    }

    /**
     * Creates a FakeAPI Client with customizable concurrency
     * for transport details see OKHttp documentation.
     *
     * @param apiBaseURL the base URL of the API eg: http://foobar.com
     * @param maxRequest The maximum amount of concurrent requests
     * @param maxRequestsPerHost The maximum amount of concurrent requests per host
     * @see Dispatcher
     * @return The API client
     * @throws IllegalArgumentException when the URL provided is invalid or empty
     */
    public static FakeAPIClient create(String apiBaseURL, int maxRequest, int maxRequestsPerHost) {
      return new FakeAPIClientImpl(apiBaseURL, maxRequestsPerHost , maxRequest);
    }

  }
}
