package pl.edu.pw.elka.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.openqa.selenium.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Crawler
{
    private WebDriver driver;
    private String lastUrl;

    public Crawler() {
        driver = null;
        lastUrl = "";

        System.out.println(Objects.requireNonNull(getClass().getClassLoader().getResource("chromedriver.exe")).getPath());
        System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(getClass().getClassLoader().getResource("chromedriver.exe")).getPath());
        openWebBrowser();
    }

    private void openWebBrowser() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        driver = new ChromeDriver(chromeOptions);
    }

    public void closeWebDriver() {
        if(driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public String getContentById(String id) {
        if(driver == null) {
            driver.get("");
        }
        WebElement element = driver.findElement(By.id(id));
        if(element != null) {
            return element.getText();
        } else {
            return "";
        }
    }

    public String getContentByCssClass(String cssClass) {
        if(driver == null) {
            driver.get("");
        }
        WebElement element = driver.findElement(By.className(cssClass));
        if(element != null) {
            return element.getText();
        } else {
            return "";
        }
    }

    public String getContentByName(String name) {
        if(driver == null) {
            driver.get("");
        }
        WebElement element = driver.findElement(By.name(name));
        if(element != null) {
            return element.getText();
        } else {
            return "";
        }
    }

    public String getContentByTagname(String tagname) {
        if(driver == null) {
            driver.get("");
        }
        WebElement element = driver.findElement(By.tagName(tagname));
        if(element != null) {
            return element.getText();
        } else {
            return "";
        }
    }

    public String getContentByCssSelector(String cssSelector) {
        if(driver == null) {
            driver.get("");
        }
        WebElement element = driver.findElement(By.cssSelector(cssSelector));
        if(element != null) {
            return element.getText();
        } else {
            return "";
        }
    }

    public void saveScreenshot(String path) {
        if(driver == null) {
            driver.get("");
        }
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, new File(path));
        } catch (java.io.IOException e) {
            System.out.println("Exception while saving screenshot: " + e.getMessage());
        }
    }

    public void get(String url) {
        if(driver == null) {
            openWebBrowser();
        }
        if(!url.isEmpty()) {
            lastUrl = url;
            driver.get(lastUrl);
        } else if(!lastUrl.isEmpty()) {
            driver.get(lastUrl);
        }
    }


    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.get("https://www.wp.pl");
        crawler.saveScreenshot("D:\\selenium_wp.png");
        crawler.closeWebDriver();
        crawler.get("https://www.onet.pl");
        crawler.saveScreenshot("D:\\selenium_onet.png");
    }
}
