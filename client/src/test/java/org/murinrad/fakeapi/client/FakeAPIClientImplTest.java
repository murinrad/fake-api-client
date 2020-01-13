package org.murinrad.fakeapi.client;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.murinrad.fakeapi.client.FakeAPIClient.Factory;
import org.murinrad.fakeapi.client.datamodel.UserOverview;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FakeAPIClientImpl.class, OkHttpClient.class})
public class FakeAPIClientImplTest {

  private FakeAPIClientImpl tested;
  private OkHttpClient fakeClient;

  @Before
  public void before() throws Exception {
    fakeClient = mock(OkHttpClient.class);
    Call userCall = mock(Call.class);
    Call postsCall = mock(Call.class);
    Call errorCall = mock(Call.class);
    Call transportErrorCall = mock(Call.class);
    byte[] userData = IOUtils.toString(this.getClass().getResourceAsStream("user_data_1.json"), "UTF-8").getBytes();
    byte[] postData = IOUtils.toString(this.getClass().getResourceAsStream("posts_data_1.json"), "UTF-8").getBytes();
    PowerMockito.whenNew(OkHttpClient.class).withAnyArguments().thenReturn(fakeClient);
    tested = (FakeAPIClientImpl) Factory.create("http://doesNotMatter.com/");
    matchCallToURL(userCall, "/users/1");
    matchCallToURL(postsCall, "/posts?userId=1");
    matchCallToURL(errorCall, "/posts?userId=123");
    matchCallToURL(errorCall, "/users/123");
    matchCallToURL(transportErrorCall, "/users/321");
    matchCallToURL(transportErrorCall, "/posts?userId=321");
    Response userResponse = constructResponse(200, true,
        userData);
    Response postsResponse = constructResponse(200, true,
        postData);
    Response errResponse = constructResponse(404, false, "{}".getBytes());
    when(userCall.execute()).thenReturn(userResponse);
    when(postsCall.execute()).thenReturn(postsResponse);
    when(errorCall.execute()).thenReturn(errResponse);
    prepareCallbackHandle(userCall, userResponse);
    prepareCallbackHandle(postsCall, postsResponse);
    prepareCallbackHandle(errorCall, errResponse);
    doAnswer(invocationOnMock -> {
      Callback callback = invocationOnMock.getArgument(0);
      callback.onFailure(null, new IOException("Mock IOException"));
      return null;
    }).when(transportErrorCall).enqueue(Mockito.any(Callback.class));

  }

  private void matchCallToURL(Call userCall, String urlEnd) {
    when(fakeClient.newCall(Mockito.argThat((x -> x != null && x.urlString() != null && x.urlString().endsWith(urlEnd)))))
        .thenReturn(userCall);
  }

  private void prepareCallbackHandle(Call call, Response response) {
    doAnswer(invocationOnMock -> {
      Callback callback = invocationOnMock.getArgument(0);
      callback.onResponse(response);
      return null;
    }).when(call).enqueue(Mockito.any(Callback.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidURL() {
    Factory.create("foobar");
  }


  @Test
  public void testRetrieveUserOverview() throws IOException, FakeApiClientException {
    UserOverview retVal = tested.retrieveUserOverview(1L);
    Assert.assertEquals("Leanne Graham", retVal.getName());
    Assert.assertEquals("Collection should have 10 items", 10, retVal.getPosts().size());
  }

  @Test
  public void testRetrieveUserOverviewAsync() throws ExecutionException, InterruptedException {
    Future<UserOverview> future = tested.retrieveUserOverviewAsync(1L);
    UserOverview retVal = future.get();
    Assert.assertEquals("Leanne Graham", retVal.getName());
    Assert.assertEquals("Collection should have 10 items", 10, retVal.getPosts().size());
  }

  @Test(expected = FakeApiClientException.class)
  public void testErrorCallAsync() throws Throwable {
    try {
      tested.retrieveUserOverviewAsync(123L).get();
    } catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testErrorCallAsyncIOProblem() throws Throwable {
    try {
      tested.retrieveUserOverviewAsync(321L).get();
    } catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = FakeApiClientException.class)
  public void testErrorCall() throws IOException, FakeApiClientException {
    tested.retrieveUserOverview(123L);
  }

  @Test
  public void testRetrieveUser() throws IOException, FakeApiClientException {
    User retVal = tested.retrieveUser(1L);
    Assert.assertEquals("Leanne Graham", retVal.getName());
    Assert.assertEquals(1L, retVal.getId());
  }

  @Test
  public void testRetrievePostByUser() throws IOException, FakeApiClientException {
    List<Post> retVal = tested.retrievePostsAssociatedWithUser(1L);
    Assert.assertEquals("Collection should have 10 items", 10, retVal.size());
    Assert.assertNotNull(retVal.get(0).getTitle());
  }

  @Test
  public void testRetrievePostByUserAsync() throws ExecutionException, InterruptedException {
    Future<List<Post>> future = tested.retrievePostsAssociatedWithUserAsync(1L);
    List<Post> retVal = future.get();
    Assert.assertEquals("Collection should have 10 items", 10, retVal.size());
    Assert.assertNotNull(retVal.get(0).getTitle());
  }

  @Test
  public void testRetrieveUserAsync() throws ExecutionException, InterruptedException {
    Future<User> future = tested.retrieveUserAsync(1L);
    User retVal = future.get();
    Assert.assertEquals("Leanne Graham", retVal.getName());
    Assert.assertEquals(1L, retVal.getId());
  }

  private Response constructResponse(int code, boolean isSuccessful, byte[] body) throws IOException {
    Response response = mock(Response.class);
    ResponseBody responseBody = mock(ResponseBody.class);
    when(response.isSuccessful()).thenReturn(isSuccessful);
    when(response.code()).thenReturn(code);
    when(response.body()).thenReturn(responseBody);
    when(responseBody.bytes()).thenReturn(body);
    when(responseBody.byteStream()).thenReturn(new ByteArrayInputStream(body));
    return response;
  }


}