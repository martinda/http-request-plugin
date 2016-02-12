package jenkins.plugins.http_request;

import hudson.model.Result;

import org.apache.http.HttpHost;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import org.junit.Test;

public class HttpRequestStepSeleniumTest extends HttpRequestSeleniumTestBase {

    @Test
    public void canDoBasicDigestAuthentication() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Create the pipeline project
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");

        driver.get(j.getURL().toString()+"configure");
        addBasicAuth("keyname1","username1","password1");

        // Configure the build
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/basicAuth',\n" +
            "    authentication: 'keyname1'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.SUCCESS, run);
    }

/*
    @Test
    public void canDoFormAuthentication() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Create the pipeline project
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");

        driver.get(j.getURL().toString()+"configure");

        // Prepare the authentication
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("param1","value1"));
        params.add(new NameValuePair("param2","value2"));
        addFormAuth(driver, "keyname1", baseUrl+"/reqAction", HttpMode.GET, params);

        // Configure the build
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/formAuth',\n" +
            "    authentication: 'keyname1'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));

        // Execute the build
        WorkflowRun run = proj.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void rejectedFormCredentialsFailTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Prepare the authentication
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("param1","value1"));
        params.add(new NameValuePair("param2","value2"));

        RequestAction action = new RequestAction(new URL(baseURL+"/formAuthBad"),HttpMode.GET,params);
        List<RequestAction> actions = new ArrayList<RequestAction>();
        actions.add(action);

        FormAuthentication formAuth = new FormAuthentication("keyname",actions);
        List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
        formAuthList.add(formAuth);

        // Prepare HttpRequest (this request won't be sent)
        HttpRequest httpRequest = new HttpRequest(baseURL+"/formAuthBad");
        httpRequest.setHttpMode(HttpMode.GET);
        httpRequest.setConsoleLogResponseBody(true);
        httpRequest.getDescriptor().setFormAuthentications(formAuthList);
        httpRequest.setAuthentication("keyname");

        // Run build
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(httpRequest);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, build);
        j.assertLogContains("Error doing authentication",build);
    }

    @Test
    public void invalidKeyFormAuthenticationFailsTheBuild() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Prepare the authentication
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("param1","value1"));
        params.add(new NameValuePair("param2","value2"));

        // The request action won't be sent but we need to prepare it
        RequestAction action = new RequestAction(new URL(baseURL+"/non-existent"),HttpMode.GET,params);
        List<RequestAction> actions = new ArrayList<RequestAction>();
        actions.add(action);

        FormAuthentication formAuth = new FormAuthentication("keyname",actions);
        List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
        formAuthList.add(formAuth);

        // Prepare HttpRequest - the actual request won't be sent
        HttpRequest httpRequest = new HttpRequest(baseURL+"/non-existent");
        httpRequest.setHttpMode(HttpMode.GET);
        httpRequest.setConsoleLogResponseBody(true);
        httpRequest.getDescriptor().setFormAuthentications(formAuthList);

        // Select a non-existent form authentication, this will error the build before any request is made
        httpRequest.setAuthentication("non-existent");

        // Run build
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(httpRequest);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.FAILURE, build);
        j.assertLogContains("Authentication 'non-existent' doesn't exist anymore",build);
    }
*/
}
