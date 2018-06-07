package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.NLPActor.TextWithQuery;
import pl.edu.pw.elka.actors.SearcherActor.SearchPathInfoQuery;
import pl.edu.pw.elka.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    static Props props(String url) {
        return Props.create(DatabaseActor.class, () -> new DatabaseActor(url));
    }

    private void searchPathInfos(SearchPathInfoQuery pathInfoQuery) {
        log.info("Searching query {} in database size {}", pathInfoQuery.query, paths.size());
        for (String path : paths) {
            getContext().actorOf(NLPActor.props()).tell(new TextWithQuery(path, pathInfoQuery.query), getSelf());
        }
    }

    private void addToDatabase(PathInfoRecord record) {
        for (String p : paths) {
            // sprawdzenie czy nie ma już takiego opisu ścieżki w bazie danych
            if (p.equals(record.pathDescription))
                return;
        }

        saveRecordToDisk(record);
        paths.add(record.pathDescription);
    }

    private String getDatabaseFolderName() {
        WebURL webUrl = new WebURL();
        webUrl.setURL(url);
        return webUrl.getDomain();
    }


    private void saveRecordToDisk(PathInfoRecord record) {
        String fileName = Utils.generateUniqueFileName();
        String folderPath = "./records/" + getDatabaseFolderName();
        File f = new File(folderPath);
        f.mkdirs();

        try (PrintWriter writer = new PrintWriter(folderPath + "/" + fileName + ".txt", "UTF-8")) {
            writer.println(record.pathDescription);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void readDatabaseFromDisk() {
        String folderPath = "./records/" + getDatabaseFolderName();
        if (Files.isDirectory(Paths.get(folderPath))) {
            File dir = new File(folderPath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    try (FileReader reader = new FileReader(child)) {
                        char[] chars = new char[(int) child.length()];
                        reader.read(chars);
                        paths.add(new String(chars));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private List<String> paths;

    private String url;

    private DatabaseActor(String url) {
        paths = new ArrayList<>();
        this.url = url;
        readDatabaseFromDisk();
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
