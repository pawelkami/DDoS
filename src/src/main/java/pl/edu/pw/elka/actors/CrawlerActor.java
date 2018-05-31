package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pl.edu.pw.elka.actors.NLPActor.TextToClassify;

import static pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class CrawlerActor extends AbstractActor {

    final String url;
    final String htmlElementType;
    final String htmlElementValue;

    /**
     * Wiadomość startująca działanie crawlera.
     */
    static class CrawlerStarter {

    }

    private CrawlerActor(String url, String htmlElementType, String htmlElementValue) {
        this.url = url;
        this.htmlElementType = htmlElementType;
        this.htmlElementValue = htmlElementValue;
    }

    static Props props(String url, String htmlElementType, String htmlElementValue) {
        return Props.create(CrawlerActor.class, () -> new CrawlerActor(url, htmlElementType, htmlElementValue));
    }

    private void searchDocuments() {

        //while (true) {
            // TODO szukamy dokumentów i je wysyłamy, reszta sama się już stanie - agent sam dostanie już wiadomość zwrotną asynchronicznie jeśli tekst był poprawny

            String text = "";
            // wysyłamy jako nasz rodzic, żeby wiadomość została zwrócona do naszego rodzica a nie do nas
            getContext().actorOf(NLPActor.props()).tell(new TextToClassify(text), getContext().getParent());   // wysyłamy do NLP wiadomość żeby sprawdził czy tekst pasuje do klasyfikatora

        //}
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
