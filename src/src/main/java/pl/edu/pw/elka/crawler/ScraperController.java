package pl.edu.pw.elka.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class ScraperController {

    public void crawl(String url, String tagType, String tagValue) throws Exception {

        /*
         * liczba crawlerow
         */
        int numberOfCrawlers = 10;

        CrawlConfig config = new CrawlConfig();

        config.setIncludeHttpsPages(true);

        config.setCrawlStorageFolder(".");

        /*
         * Be polite: zapewnienie wysylania requestow nie czesciej niz co pol sekundy
         */
        config.setPolitenessDelay(500);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        config.setMaxDepthOfCrawling(5);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(100);

        /*
         * Do you want crawler4j to crawl also binary data ?
         * example: the contents of pdf, or the metadata of images etc
         */
        config.setIncludeBinaryContentInCrawling(false);

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

        config.setConnectionTimeout(5000);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed(url);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(UrlScraper.class, numberOfCrawlers);

        if(!UrlScraper.scrapedUrls.isEmpty()) {
            Crawler crawler = new Crawler();
//            Integer it = 1;
            for(String scrapedUrl : UrlScraper.scrapedUrls) {
                crawler.get(scrapedUrl);

                String xpath = "//" + tagType + "[@" + tagValue.substring(0, tagValue.indexOf('=')) + "='" + tagValue.substring(tagValue.indexOf('=') + 1) + "']";
                String content = crawler.getContentByXpath(xpath);

                System.out.println(content);
//                crawler.saveScreenshot("D:\\selenium_wp_" + it.toString() + ".png");
//                it += 1;
            }
            crawler.closeWebDriver();
        }

    }

    public static void main(String[] args) throws Exception {
        ScraperController scraper = new ScraperController();
        scraper.crawl("https://www.sustrans.org.uk/ncn/map/route/tamsin-trail-richmond-park", "div", "class=main-content");
    }
}