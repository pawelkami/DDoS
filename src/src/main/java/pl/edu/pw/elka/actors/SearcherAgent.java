package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.CrawlerActor.CrawlerStarter;
import pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class SearcherAgent extends AbstractActor {

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

    private SearcherAgent(String url, String htmlElementType, String htmlElementValue) {
        dbActor = getContext().actorOf(DatabaseActor.props());
        crawlerActor = getContext().actorOf(CrawlerActor.props(url, htmlElementType, htmlElementValue));
        crawlerActor.tell(new CrawlerStarter(), getSelf()); // uruchamiamy crawlera
    }

    static Props props(String url, String htmlElementType, String htmlElementValue) {
        return Props.create(SearcherAgent.class, () -> new SearcherAgent(url, htmlElementType, htmlElementValue));
    }

    private void handlePathInfoQuery(SearchPathInfoQuery query) {
        System.out.println("OTRZYMALEM ZAPYTANIE " + query.query);
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
