package pl.edu.pw.elka.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
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
        try {
            WebElement element = driver.findElement(By.id(id));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            return "";
        }
    }

    public String getContentByCssClass(String cssClass) {
        if(driver == null) {
            driver.get("");
        }
        try {
            WebElement element = driver.findElement(By.className(cssClass));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            return "";
        }
    }

    public String getContentByName(String name) {
        if(driver == null) {
            driver.get("");
        }
        try {
            WebElement element = driver.findElement(By.name(name));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            return "";
        }
    }

    public String getContentByTagname(String tagname) {
        if(driver == null) {
            driver.get("");
        }
        try {
            WebElement element = driver.findElement(By.tagName(tagname));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            return "";
        }
    }

    public String getContentByCssSelector(String cssSelector) {
        if(driver == null) {
            driver.get("");
        }
        try {
            WebElement element = driver.findElement(By.cssSelector(cssSelector));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            return "";
        }
    }

    public String getContentByXpath(String xpath) {
        if(driver == null) {
            driver.get("");
        }
        try {
            WebElement element = driver.findElement(By.xpath(xpath));
            if(element != null) {
                return element.getText();
            } else {
                return "";
            }
        } catch (org.openqa.selenium.WebDriverException e) {
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

    public String prepareXpath(String tag, String propName, String propValuesAll) {
        String[] propValues = propValuesAll.split("\\s+");
        String xpath = "//" + tag + "[";

        for(String propValue : propValues) {
            xpath += "contains(@" + propName + ", '" + propValue + "') and ";
        }
        xpath = xpath.substring(0, xpath.length() - 5);
        xpath += "]";

        return xpath;
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
//        Crawler crawler = Crawler.getInstance();
        Crawler crawler = new Crawler();
        crawler.get("https://www.butterfield.com/blog/2016/04/21/most-memorable-moments-at-the-masters/");
        String tag = "section";
        String propName = "class";
        String propValueAll = "post-wrapper post-top ";

        String xpath = crawler.prepareXpath(tag, propName, propValueAll);

        System.out.println(xpath);
        String content = crawler.getContentByXpath(xpath);
        System.out.println(content);
//        crawler.saveScreenshot("D:\\selenium_wp.png");
        crawler.closeWebDriver();
    }
}
