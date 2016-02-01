package jenkins.plugins.http_request;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

/**
 * The response type returned to the Pipeline (Workflow) call to httpRequest.
 */
class ResponseContentSupplier implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient HttpResponse response;
    private String content;
    private int status;

    public ResponseContentSupplier(HttpResponse response) {
        this.response = response;
        this.status = response.getStatusLine().getStatusCode();
        this.content = getContent();
    }

    @Whitelisted
    public int getStatus() {
        return status;
    }

    @Whitelisted
    public String getContent() {
        if (content != null) {
            return content;
        }
        try {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            content = EntityUtils.toString(entity);
            EntityUtils.consume(response.getEntity());
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Status: "+status+", Response: "+content;
    }
}
