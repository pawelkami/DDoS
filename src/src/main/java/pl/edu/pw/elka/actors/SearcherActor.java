package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.CrawlerActor.CrawlerStarter;
import pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class SearcherActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(SearcherActor.class);

    private ActorRef dbActor;
    private ActorRef crawlerActor;

    /**
     * Klasa reprezentująca zapytanie o ścieżkę.
     */
    static class SearchPathInfoQuery {
        String query;

        SearchPathInfoQuery(String query) {
            this.query = query;
        }
    }

    private SearcherActor(String url, String htmlElementType, String htmlElementValue) {
        dbActor = getContext().actorOf(DatabaseActor.props());
        crawlerActor = getContext().actorOf(CrawlerActor.props(url, htmlElementType, htmlElementValue));
        crawlerActor.tell(new CrawlerStarter(), getSelf()); // uruchamiamy crawlera
        log.debug("Created Searcher for website {}", url);
    }

    static Props props(String url, String htmlElementType, String htmlElementValue) {
        return Props.create(SearcherActor.class, () -> new SearcherActor(url, htmlElementType, htmlElementValue));
    }

    private void handlePathInfoQuery(SearchPathInfoQuery query) {
        log.debug("Received query {}", query.query);
        dbActor.tell(query, getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchPathInfoQuery.class,
                        this::handlePathInfoQuery)
                .match(PathInfoResponse.class,
                        (foundPathInfo -> getContext().getParent().tell(foundPathInfo, getSelf()))
                )
                .match(PathInfoRecord.class,
                        pathInfoRecord -> dbActor.tell(pathInfoRecord, getSelf())
                )
                .build();
    }
}
