package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.NLPActor.TextWithQuery;
import pl.edu.pw.elka.actors.SearcherAgent.SearchPathInfoQuery;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActor extends AbstractActor {

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
        for (PathInfoRecord p : paths) {
            // tworzymy pojedynczych aktorów na zapytanie (maksymalne zrównoleglenie)
            getContext().actorOf(NLPActor.props()).tell(new TextWithQuery(p.pathDescription, pathInfoQuery.query), getSelf());
        }
    }

    private void addToDatabase(PathInfoRecord record) {
        paths.add(record);
    }

    private List<PathInfoRecord> paths;

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
