package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.NLPActor.TextWithQuery;
import pl.edu.pw.elka.actors.SearcherActor.SearchPathInfoQuery;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseActor.class);

    /**
     * Klasa reprezentująca wiadomość, która zawiera opis ścieżki, która ma zostać dodana do bazy danych.
     */
    static class PathInfoRecord {
        final String pathDescription;   // tekst opisujący ścieżkę rowerową/biegową...

        PathInfoRecord(String pathDescription) {
            this.pathDescription = pathDescription;
        }
    }

    static Props props() {
        return Props.create(DatabaseActor.class, DatabaseActor::new);
    }

    private void searchPathInfos(SearchPathInfoQuery pathInfoQuery) {
        log.info("Searching query {} in database size {}", pathInfoQuery.query, paths.size());
        getContext().actorOf(NLPActor.props()).tell(new TextWithQuery(paths, pathInfoQuery.query), getSelf());
    }

    private void addToDatabase(PathInfoRecord record) {
        paths.add(record.pathDescription);
    }

    private List<String> paths;

    private DatabaseActor() {
        paths = new ArrayList<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PathInfoRecord.class,
                        this::addToDatabase
                )
                .match(SearchPathInfoQuery.class,
                        this::searchPathInfos
                )
                .match(PathInfoResponse.class,
                        pathInfoResponse -> getContext().getParent().tell(pathInfoResponse, getSelf()))
                .build();
    }
}
