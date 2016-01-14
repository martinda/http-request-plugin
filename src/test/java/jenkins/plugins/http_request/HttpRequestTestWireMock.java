package jenkins.plugins.http_request;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.Rule;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import org.jvnet.hudson.test.JenkinsRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpRequestTestWireMock {

    // The class under test
    HttpRequest httpRequest;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public static WireMockServer wireMockServer;

    public static final Logger logger = LoggerFactory.getLogger(HttpRequestTest.class);
    public static String urlForTest;

    @BeforeClass
    public static void setup() throws IOException {
        int portNum;
        for (portNum = 49152; portNum <= 65535; portNum++) {
            wireMockServer = new WireMockServer(wireMockConfig().port(portNum));
            try {
                wireMockServer.start();
            } catch (FatalStartupException exception) {
                continue;
            }
            logger.debug("Started WireMockServer on port "+portNum);
            break;
        }
        if (portNum > 65535) {
          throw new IOException("Unable to allocation a port number to run the WireMock server.");
        }
        urlForTest = "http://localhost:"+portNum+"/";
    }

    @AfterClass
    public static void cleanup() {
        wireMockServer.stop();
    }

   @Test
   public void testWithMock() throws Exception {

       // WireMock verbosity
       wireMockConfig().notifier(new ConsoleNotifier(true));

       // Prepare a response
       wireMockServer.stubFor(get(urlEqualTo("/path"))
           .willReturn(aResponse()
               .withHeader("Content-Type", "text/plain")
               .withBody("Hello World")));

       // Create the Jenkins project
       FreeStyleProject project = j.createFreeStyleProject();

       // Prepare the GET request, but do not issue it yet
       project.getBuildersList().add(new HttpRequest(urlForTest+"path", HttpMode.GET, "",
           MimeType.NOT_SET, MimeType.NOT_SET,
           "", null, true, false, null, 0, "", ""));

       // Run the Jenkins job, this issues the request
       FreeStyleBuild build = project.scheduleBuild2(0).get();

       // We expect this Jenkins job to succeed
       j.assertBuildStatusSuccess(build);

       // More verification and debug
       String s = FileUtils.readFileToString(build.getLogFile());
       System.out.println(s);
       Pattern p = Pattern.compile("HttpMode: GET");
       Matcher m = p.matcher(s);
       assertTrue(m.find());
   }

   @Test
   public void testWithoutMock() throws Exception {
       FreeStyleProject project = j.createFreeStyleProject();
       project.getBuildersList().add(new HttpRequest(j.getURL().toString()+"api/json", HttpMode.GET, "",
           MimeType.NOT_SET, MimeType.NOT_SET,
           "", null, true, false, null, 0, "", ""));
       FreeStyleBuild build = project.scheduleBuild2(0).get();
       j.assertBuildStatusSuccess(build);
       String s = FileUtils.readFileToString(build.getLogFile());
       Pattern p = Pattern.compile("HttpMode: GET");
       Matcher m = p.matcher(s);
       assertTrue(m.find());
   }
}
