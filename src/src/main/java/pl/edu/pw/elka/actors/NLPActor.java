package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;
import pl.edu.pw.elka.nlp.NLP;
import org.nd4j.linalg.primitives.Pair;

import java.io.IOException;
import java.util.*;

public class NLPActor extends AbstractActor {
    private final static double MINIMUM_RATING_SEARCH = 0.40;
    private final static double MINIMUM_RATING_CLASSIFY = 0.55;

    private static final Logger log = LoggerFactory.getLogger(NLPActor.class);

    static class TextWithQuery {
        final String text;
        final String query;

        TextWithQuery(String text, String query) {
            this.text = text;
            this.query = query;
        }
    }

    static class TextToClassify {
        final String text;

        TextToClassify(String text) {
            this.text = text;
        }
    }

    static Props props() {
        return Props.create(NLPActor.class, NLPActor::new);
    }

    /**
     * Funkcja sprawdza czy tekst ścieżki zgadza się z zapytaniem. Jeśli tak to wysyła wiadomość PathInfoResponse.
     * Jest to wiadomość od Searchera (a wcześniej od użytkownika).
     *
     * @param textWithQuery
     */
    private void analyseQuery(TextWithQuery textWithQuery) {
        try {
            NLP nlp = new NLP();
            double rating = nlp.checkTwoTextsSimilarity(textWithQuery.text, textWithQuery.query);
            if (rating > MINIMUM_RATING_SEARCH) {
                getContext().sender().tell(new PathInfoResponse(textWithQuery.text, rating), getSelf());
            }
        } catch (IOException e) {
            log.error("Some of resources are not initialized.");
            e.printStackTrace();
        }
    }

    /**
     * Wiadomość od crawlera.
     *
     * @param text
     */
    private void classifyText(TextToClassify text) {
        try {
            NLP nlp = new NLP();
            List<Pair<String, Double>> pairs = nlp.checkNewTextSimilarityToModel(text.text);

            for (Pair<String, Double> pair : pairs) {
                if ((pair.getFirst().equals("cycling") && pair.getSecond() >= MINIMUM_RATING_CLASSIFY)
                        || (pair.getFirst().equals("hiking") && pair.getSecond() >= MINIMUM_RATING_CLASSIFY)
                        || ((pair.getFirst().equals("running") && pair.getSecond() >= MINIMUM_RATING_CLASSIFY)))
                {
                    getContext().sender().tell(new PathInfoRecord(text.text), getSelf());
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TextWithQuery.class,
                        this::analyseQuery)
                .match(TextToClassify.class,
                        this::classifyText)
                .build();
    }
}
