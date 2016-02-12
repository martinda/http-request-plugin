package jenkins.plugins.http_request;

import hudson.model.Result;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;

import java.lang.RuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class HttpRequestStepTest extends HttpRequestTestBase {

    @Test
    public void simpleGetTest() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest '"+baseURL+"/doGET'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Status: 200",run);
        j.assertLogContains("Response: "+allIsWellMessage,run);
    }

    @Test
    public void canDetectActualContent() throws Exception {
        // Setup the expected pattern
        String findMe = allIsWellMessage;

        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Prepare HttpRequest
        HttpRequest httpRequest = new HttpRequest(baseURL+"/doGET");
        httpRequest.setConsoleLogResponseBody(true);
        httpRequest.setValidResponseContent(findMe);

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/doGET',\n" +
            "    consoleLogResponseBody: true\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Status: 200",run);
        j.assertLogContains("Response: "+allIsWellMessage,run);
        j.assertLogContains(findMe,run);
    }

    @Test
    public void badContentFailsTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/doGET',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    validResponseContent: 'bad content'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, run);
        String s = FileUtils.readFileToString(run.getLogFile());
        Pattern p = Pattern.compile("Fail: Response with length \\d+ doesn't contain 'bad content'");
        Matcher m = p.matcher(s);
        assertTrue(m.find());
    }

    @Test
    public void responseMatchAcceptedMimeType() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/doGET',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    acceptType: 'TEXT_PLAIN'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains(allIsWellMessage,run);
    }

    @Test
    public void responseDoesNotMatchAcceptedMimeTypeDoesNotFailTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/doGET',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    acceptType: 'TEXT_HTML'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains(allIsWellMessage,run);
    }

    @Test
    public void doAllRequestTypes() throws Exception {
        for (HttpMode mode: HttpMode.values()) {
            doRequest(mode);
        }
    }

    public void doRequest(final HttpMode mode) throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj"+mode.toString());
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/do"+mode.toString()+"',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    httpMode: '"+mode.toString()+"'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);

        if (mode == HttpMode.HEAD) return;

        j.assertLogContains(allIsWellMessage,run);
    }

    @Test
    public void invalidResponseCodeFailsTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/invalidStatusCode',\n" +
            "    consoleLogResponseBody: true\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains("Throwing status 400 for test",run);
    }

    @Test
    public void invalidResponseCodeIsAccepted() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/invalidStatusCode',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    validResponseCodes: '100:599'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains("Throwing status 400 for test",run);
    }

    @Test
    public void sendAllContentTypes() throws Exception {
        for (MimeType mimeType : MimeType.values()) {
            sendContentType(mimeType);
        }
    }

    public void sendContentType(final MimeType mimeType) throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj"+mimeType.toString());
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/incoming_"+mimeType.toString()+"',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    contentType: '"+mimeType.toString()+"'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains(allIsWellMessage,run);
    }

    @Test
    public void sendAllAcceptTypes() throws Exception {
        for (MimeType mimeType : MimeType.values()) {
            sendAcceptType(mimeType);
        }
    }

    public void sendAcceptType(final MimeType mimeType) throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj"+mimeType.toString());
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/accept_"+mimeType.toString()+"',\n" +
            "    consoleLogResponseBody: true,\n" +
            "    acceptType: '"+mimeType.toString()+"'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatusSuccess(run);
        j.assertLogContains(allIsWellMessage,run);
    }

    @Test
    public void timeoutFailsTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/timeout',\n" +
            "    timeout: 2\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void canDoCustomHeaders() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/customHeaders',\n" +
            "    customHeaders: [[name: 'customHeader', value: 'value1'],[name: 'customHeader', value: 'value2']]\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void nonExistentBasicAuthFailsTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Configure the build
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/basicAuth',\n" +
            "    authentication: 'invalid'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, run);
    }

}
