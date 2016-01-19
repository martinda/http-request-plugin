package jenkins.plugins.http_request;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.inject.Inject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import jenkins.plugins.http_request.HttpRequest;
import org.apache.http.HttpResponse;

public final class HttpRequestStep extends AbstractStepImpl {

    private final String url;

    @DataBoundConstructor
    public HttpRequestStep(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Extension public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override public String getFunctionName() {
            return "httpRequest";
        }

        @Override public String getDisplayName() {
            return "Perform an HTTP Request and return the response as a string";
        }

    }

    public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<HttpResponse> {

        @Inject
        private transient HttpRequestStep step;

        @StepContextParameter
        private transient Run run;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient FilePath workspace;

        @Override protected HttpResponse run() throws Exception {
            HttpRequest httpRequest = new HttpRequest(step.url);
            HttpResponse response = httpRequest.performHttpRequest(run, workspace, launcher, listener);
            return response;
        }

        private static final long serialVersionUID = 1L;

    }

}
