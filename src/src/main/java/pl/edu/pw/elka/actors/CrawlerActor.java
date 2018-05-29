package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pl.edu.pw.elka.actors.NLPActor.TextToClassify;

import static pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class CrawlerActor extends AbstractActor {

    final String url;
    final String htmlElementId;

    /**
     * Wiadomość startująca działanie crawlera.
     */
    static class CrawlerStarter {

    }

    private CrawlerActor(String url, String htmlElementId) {
        this.url = url;
        this.htmlElementId = htmlElementId;
    }

    static Props props(String url, String htmlElementId) {
        return Props.create(CrawlerActor.class, () -> new CrawlerActor(url, htmlElementId));
    }

    private void searchDocuments() {

        while (true) {
            // TODO szukamy dokumentów i je wysyłąmy, reszta sama się już stanie - agent sam dostanie już wiadomość zwrotną asynchronicznie jeśli tekst był poprawny

            String text = "";
            getContext().actorOf(NLPActor.props()).tell(new TextToClassify(text), getSelf());   // wysyłamy do NLP wiadomość żeby sprawdził czy tekst pasuje do klasyfikatora

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
