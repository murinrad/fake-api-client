package org.murinrad.fakeapi.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.murinrad.fakeapi.client.FakeAPIClient.Factory;
import org.murinrad.fakeapi.client.datamodel.UserOverview;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FakeAPIClient.class, OkHttpClient.class})
public class FakeAPIClientTest {

  private FakeAPIClient tested;
  private OkHttpClient fakeClient;

  @Before
  public void before() throws Exception {
    fakeClient = mock(OkHttpClient.class);
    Call userCall = mock(Call.class);
    Call postsCall = mock(Call.class);
    Call errorCall = mock(Call.class);
    PowerMockito.whenNew(OkHttpClient.class).withAnyArguments().thenReturn(fakeClient);
    tested = Factory.create("http://jsonplaceholder.typicode.com/");
    when(fakeClient.newCall(Mockito.argThat((x-> x != null && x.urlString() != null && x.urlString().endsWith("/users/1"))))).thenReturn(userCall);
    when(fakeClient.newCall(Mockito.argThat((x-> x != null && x.urlString() != null && x.urlString().endsWith("/posts?userId=1"))))).thenReturn(postsCall);
    when(fakeClient.newCall(Mockito.argThat((x-> x != null && x.urlString() != null && x.urlString().endsWith("/users/123"))))).thenReturn(errorCall);
    Response userResponse = constructResponse(200, true,
        IOUtils.toString(this.getClass().getResourceAsStream("user_data_1.json"), "UTF-8").getBytes());
    Response postsResponse = constructResponse(200, true,
        IOUtils.toString(this.getClass().getResourceAsStream("posts_data_1.json"), "UTF-8").getBytes());
    Response errResponse = constructResponse(404, false, "{}".getBytes());
    when(userCall.execute()).thenReturn(userResponse);
    when(postsCall.execute()).thenReturn(postsResponse);
    when(errorCall.execute()).thenReturn(errResponse);
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