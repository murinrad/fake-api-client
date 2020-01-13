package org.murinrad.fakeapi.client;

import java.util.concurrent.CompletableFuture;
import org.junit.Ignore;
import org.junit.Test;
import org.murinrad.fakeapi.client.datamodel.UserOverview;

@Ignore("Ignored so the server is not called during regular tests")
public class FakeAPIClientImplRealTest {

  @Test
  public void testMass() {
    int calls = 1000;
    try (FakeAPIClient client = FakeAPIClientImpl.Factory.create("http://jsonplaceholder.typicode.com/",
        100, 500)) {
      CompletableFuture<UserOverview>[] futures = new CompletableFuture[calls];
      long startTime = System.currentTimeMillis();
      int id = 1;
      for (int i = 0; i < calls; i++) {
        futures[i] = (CompletableFuture) client.retrieveUserOverviewAsync(id);
        id++;
        if (id == 11) {
          id = 1;
        }
      }
      CompletableFuture.allOf(futures).join();
      System.out.println("Done, took " + (System.currentTimeMillis() - startTime));
    }
  }

}
