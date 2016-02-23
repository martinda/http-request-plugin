package jenkins.plugins.http_request;

import hudson.Extension;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import jenkins.plugins.http_request.auth.Authenticator;
import jenkins.plugins.http_request.auth.BasicDigestAuthentication;
import jenkins.plugins.http_request.auth.FormAuthentication;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class HttpRequestGlobalConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(HttpRequestGlobalConfig.class.getName());

    private List<BasicDigestAuthentication> basicDigestAuthentications = new ArrayList<BasicDigestAuthentication>();
    private List<FormAuthentication> formAuthentications = new ArrayList<FormAuthentication>();

    public HttpRequestGlobalConfig() {
        load();
        LOGGER.info("configfile: "+getConfigFile().getFile().getName());
        try {
            LOGGER.info("configfile: "+getConfigFile().asString());
        } catch (java.io.IOException ioe) {
            LOGGER.severe("IOException reading "+getConfigFile().getFile().getName());
        }
        LOGGER.info("load() (basic): "+basicDigestAuthentications.size());
        LOGGER.info("load() (form): "+formAuthentications.size());
    }

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addAliases() {
        LOGGER.info("Added aliases");
        Items.XSTREAM2.addCompatibilityAlias("jenkins.plugins.http_request.HttpRequest$DescriptorImpl", HttpRequestGlobalConfig.class);
    }
/*
    @Override
    protected XmlFile getConfigFile() {
        XmlFile f = super.getConfigFile();
        LOGGER.info("Super getConfigFile: "+f);
        String configName = HttpRequest.class.getName();
        LOGGER.info("configName: "+Jenkins.getInstance().getRootDir()+"/"+configName+".xml");
        XmlFile x = new XmlFile(new File(Jenkins.getInstance().getRootDir(),configName+".xml"));
        LOGGER.info("x: "+x.getFile().exists());
        try {
            LOGGER.info("x: "+x.asString());
        } catch (IOException ioe) {
            LOGGER.warning("Unable to convert "+x+" to a string");
        }
        return x;
    }
*/
    @Override
    public boolean configure(StaplerRequest req, JSONObject json)
    throws FormException
    {
        LOGGER.info("Loading config: "+json);
        req.bindJSON(this, json);
        save();
        return true;
    }

    public static HttpRequestGlobalConfig get() {
        return GlobalConfiguration.all().get(HttpRequestGlobalConfig.class);
    }

    public List<BasicDigestAuthentication> getBasicDigestAuthentications() {
        return basicDigestAuthentications;
    }

    public void setBasicDigestAuthentications(
            List<BasicDigestAuthentication> basicDigestAuthentications) {
        this.basicDigestAuthentications = basicDigestAuthentications;
    }

    public List<FormAuthentication> getFormAuthentications() {
        return formAuthentications;
    }

    public void setFormAuthentications(
            List<FormAuthentication> formAuthentications) {
        this.formAuthentications = formAuthentications;
    }

    public List<Authenticator> getAuthentications() {
        List<Authenticator> list = new ArrayList<Authenticator>();
        list.addAll(basicDigestAuthentications);
        list.addAll(formAuthentications);
        return list;
    }

    public Authenticator getAuthentication(String keyName) {
        for (Authenticator authenticator : getAuthentications()) {
            if (authenticator.getKeyName().equals(keyName)) {
                return authenticator;
            }
        }
        return null;
    }

    public FormValidation checkKeyName(String value) {
        List<Authenticator> list = getAuthentications();

        int count = 0;
        for (Authenticator basicAuthentication : list) {
            if (basicAuthentication.getKeyName().equals(value)) {
                count++;
            }
        }

        if (count > 1) {
            return FormValidation.error("The Key Name must be unique");
        }

        return FormValidation.validateRequired(value);

    }

}
