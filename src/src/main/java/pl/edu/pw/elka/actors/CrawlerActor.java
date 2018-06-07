package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pl.edu.pw.elka.actors.NLPActor.TextToClassify;
import pl.edu.pw.elka.crawler.ScraperController;

import java.util.ArrayList;
import java.util.List;

import static pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class CrawlerActor extends AbstractActor {

    private final String url;
    private final String htmlElementType;
    private final String propertyName;
    private final String propertyValue;

    /**
     * Wiadomość startująca działanie crawlera.
     */
    static class CrawlerStarter {

    }

    private CrawlerActor(String url, String htmlElementType, String propertyName, String propertyValue) {
        this.url = url;
        this.htmlElementType = htmlElementType;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    static Props props(String url, String htmlElementType, String propertyName, String propertyValue) {
        return Props.create(CrawlerActor.class, () -> new CrawlerActor(url, htmlElementType, propertyName, propertyValue));
    }

    private void searchDocuments() {
        ScraperController controller = new ScraperController();
        List<String> foundTexts = null;

        try {
            foundTexts = new ArrayList<String>(controller.crawl(this.url, this.htmlElementType, this.propertyName, this.propertyValue));
        } catch (Exception e) {
            foundTexts = new ArrayList<String>();
        }

        for (String text : foundTexts) {
            // wysyłamy jako nasz rodzic, żeby wiadomość została zwrócona do naszego rodzica a nie do nas
            if (!text.isEmpty()) {
                getContext().actorOf(NLPActor.props()).tell(new TextToClassify(text), getContext().getParent());   // wysyłamy do NLP wiadomość żeby sprawdził czy tekst pasuje do klasyfikatora
            }
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CrawlerStarter.class,
                        crawlerStarter -> searchDocuments())
                .match(PathInfoRecord.class,
                        pathInfoRecord -> getContext().getParent().tell(pathInfoRecord, getSelf()))
                .build();
    }
}
