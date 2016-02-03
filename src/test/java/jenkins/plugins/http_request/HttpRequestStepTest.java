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
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    private WebElement findConfigForm(WebDriver driver, String configName) {
        WebElement element = driver.findElement(By.name("jenkins-plugins-http_request-HttpRequestStep"));
        String id = element.getAttribute("id");
        System.out.println("Element id: "+id);
        List<WebElement> trs = driver.findElements(By.tagName("tr"));
        System.out.println("trs.size = "+trs.size());
        for (WebElement e: trs) {
            String attr = e.getAttribute("nameref");
            if (id.equals(attr)) {
                System.out.println("Found "+attr);
                List<WebElement> trs2 = e.findElements(By.className("setting-name"));
                if (trs2.size() == 1) {
                    String settingName = trs2.get(0).getText();
                    System.out.println("setting-name found with text: "+settingName);
                    if (settingName.equals(configName)) {
                        System.out.println("Found config: "+configName);
                        return e;
                    }
                }
            }
        }
        return null;
    }

    private void clickSubmitButton(WebDriver driver)
    throws RuntimeException
    {
        List<WebElement> submitElements = driver.findElements(By.name("Submit"));
        for (WebElement e: submitElements) {
            List<WebElement> button = e.findElements(By.tagName("button"));
            if (button.size() == 1) {
                button.get(0).click();
                return;
            }
        }
        throw new RuntimeException("Submit button not found.");
    }

    private void addBasicAuth(WebDriver driver, String keyName, String userName, String password)
    throws IOException
    {
        WebElement basicAuthConfig = findConfigForm(driver, "Basic/Digest Authentication");
        WebElement button = basicAuthConfig.findElement(By.tagName("button"));
        button.click();
        (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.name("basicDigestAuthentication.keyName")));
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.keyName")).sendKeys(keyName);
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.userName")).sendKeys(userName);
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.password")).sendKeys(password);
        clickSubmitButton(driver);
    }

    @Test
    public void canDoBasicDigestAuthentication() throws Exception {
        // Prepare the server
        final HttpHost target = start();
        final String baseURL = "http://localhost:" + target.getPort();

        // Create the pipeline project
        WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "proj");

        // Setup the global configuration authentication keys
        WebDriver driver = new FirefoxDriver();

        driver.get(j.getURL().toString()+"configure");
        //WebElement addBasicAuthButton = findAddButton(driver, "Basic/Digest Authentication");
        //assertNotNull(addBasicAuthButton);
        //addBasicAuthButton.click();
        addBasicAuth(driver, "keyname1","user1","password1");
        //System.out.println("============\n"+driver.getPageSource()+"\n===========");

        //HtmlPage configPage = j.createWebClient().goTo("configure");
        //HtmlForm form = configPage.getFormByName("config");
        //form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));

        //List<BasicDigestAuthentication> bda = new ArrayList<BasicDigestAuthentication>();
        //bda.add(new BasicDigestAuthentication("keyname1","username1","password1"));
        //bda.add(new BasicDigestAuthentication("keyname2","username2","password2"));

        // Configure the build
        proj.setDefinition(new CpsFlowDefinition(
            "def response = httpRequest url:'"+baseURL+"/basicAuth',\n" +
            "    authentication: 'keyname1'\n" +
            "println('Status: '+response.getStatus())\n" +
            "println('Response: '+response.getContent())\n",
            true));
        System.out.println("BEFORE BUILD EXEC");
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

        // Prepare the authentication
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("param1","value1"));
        params.add(new NameValuePair("param2","value2"));

        RequestAction action = new RequestAction(new URL(baseURL+"/reqAction"),HttpMode.GET,params);
        List<RequestAction> actions = new ArrayList<RequestAction>();
        actions.add(action);

        FormAuthentication formAuth = new FormAuthentication("keyname",actions);
        List<FormAuthentication> formAuthList = new ArrayList<FormAuthentication>();
        formAuthList.add(formAuth);

        // Prepare HttpRequest
        HttpRequest httpRequest = new HttpRequest(baseURL+"/formAuth");
        httpRequest.setHttpMode(HttpMode.GET);
        httpRequest.getDescriptor().setFormAuthentications(formAuthList);
        httpRequest.setAuthentication("keyname");

        // Run build
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(httpRequest);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        // Check expectations
        j.assertBuildStatus(Result.SUCCESS, build);
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
