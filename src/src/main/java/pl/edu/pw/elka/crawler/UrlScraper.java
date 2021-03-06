package pl.edu.pw.elka.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * https://github.com/yasserg/crawler4j
 */
public class UrlScraper extends WebCrawler {

    public static Map<String, HashSet<String>> scrapedUrls = new HashMap<String, HashSet<String>>();

    /**
     * Metoda weryfikuje kiedy crawler ma podany url odwiedzic.
     * Nasz warunek jest spelniony gdy zglebiamy podstrony danej strony i
     * nie wychodzimy poza nia
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String rootUrl = referringPage.getWebURL().getURL().toLowerCase();
        String domain = referringPage.getWebURL().getDomain();
        String href = url.getURL().toLowerCase();
        return href.contains(rootUrl) || href.contains(domain);
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
//        System.out.println("URL: " + url);

        boolean added = false;
        while(!added) {
            try {
                UrlScraper.scrapedUrls.putIfAbsent(page.getWebURL().getDomain(), new HashSet<String>());
                UrlScraper.scrapedUrls.get(page.getWebURL().getDomain()).add(url);
                added = true;
            } catch(NullPointerException e) {
            }
        }
    }
}
