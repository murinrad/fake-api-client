package org.murinrad.fakeapi.cli;

import java.io.IOException;
import org.murinrad.fakeapi.client.FakeAPIClient;
import org.murinrad.fakeapi.client.FakeApiClientException;
import org.murinrad.fakeapi.client.datamodel.UserOverview;

public class Main {

  public static void main(String[] args) throws IOException, FakeApiClientException {
    if (args.length != 2) {
      printHelp();
      return;
    }
    String hostURL = args[0];
    try {
      long userID = Long.parseLong(args[1]);
      UserOverview userOverview = FakeAPIClient.Factory.create(hostURL).retrieveUserOverview(userID);
      System.out.println(userOverview.toString());
    } catch (NumberFormatException ex) {
      printHelp();
    }


  }

  private static void printHelp() {
    System.out.println("Incorrect input.");
    System.out.println("Please use the following input format foo.jar <HOST_URL> <USER_ID>");
  }

}
