package jenkins.plugins.http_request;

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.jvnet.hudson.test.JenkinsRule;

import org.jenkinsci.plugins.workflow.steps.StepConfigTester;

public class HttpRequestStepTest {
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test public void configRoundTrip() throws Exception {
        HttpRequestStep step = new StepConfigTester(jenkinsRule).configRoundTrip(new HttpRequestStep("http://localhost:8080/"));
        assertEquals("http://localhost:8080", step.getUrl());
    }
}
