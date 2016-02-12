package jenkins.plugins.http_request;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HttpRequestSeleniumTestBase extends HttpRequestTestBase {

    public static WebDriver driver;

    @BeforeClass
    public static void beforeTestClass() throws Exception {
        driver = new FirefoxDriver();
    }

    @AfterClass
    public static void afterTestClass() throws Exception {
        driver.close();
        driver.quit();
    }

    protected WebElement findConfigForm(String configName) {
        WebElement element = driver.findElement(By.name("jenkins-plugins-http_request-HttpRequestGlobalConfig"));
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

    protected void clickSubmitButton()
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

    protected void addBasicAuth(String keyName, String userName, String password)
    throws IOException
    {
        WebElement basicAuthConfig = findConfigForm("Basic/Digest Authentication");
        WebElement button = basicAuthConfig.findElement(By.tagName("button"));
        button.click();
        (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.name("basicDigestAuthentication.keyName")));
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.keyName")).sendKeys(keyName);
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.userName")).sendKeys(userName);
        basicAuthConfig.findElement(By.name("basicDigestAuthentication.password")).sendKeys(password);
        clickSubmitButton();
    }
}
