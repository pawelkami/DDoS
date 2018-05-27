package pl.edu.pw.elka.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.openqa.selenium.By;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Crawler
{
    private WebDriver driver;

    private void openWebBrowser() {
        System.out.println(Objects.requireNonNull(getClass().getClassLoader().getResource("chromedriver.exe")).getPath());

        System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(getClass().getClassLoader().getResource("chromedriver.exe")).getPath());
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.setBinary(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("chromedriver.exe")).getPath()));
        chromeOptions.addArguments("--headless");

        driver = new ChromeDriver(chromeOptions);
    }

    private void closeWebDriver() {
        driver.quit();
    }

    public void getContentByCssId(String cssId) {
        // TODO
    }

    public void saveScreenshot() {
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, new File("D:\\selenium_wp.png"));
        } catch (java.io.IOException e) {
            System.out.println("Exception while saving screenshot: " + e.getMessage());
        }
    }

    public void get(String url) {
        openWebBrowser();
        driver.get(url);
        closeWebDriver();
    }


    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.get("https://www.wp.pl");
    }
}
