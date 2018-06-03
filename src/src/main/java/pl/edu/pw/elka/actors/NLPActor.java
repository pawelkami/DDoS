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
import java.util.List;

public class NLPActor extends AbstractActor {
    private static final Logger log = LoggerFactory.getLogger(NLPActor.class);

    static class TextWithQuery {
//        final List<String> texts;
        final String text;
        final String query;

        TextWithQuery(String text, String query) {
//            this.texts = texts;
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
     * @param textWithQuery
     */
    private void analyseQuery(TextWithQuery textWithQuery) {
        try {
            NLP nlp = NLP.getInstance();
        } catch (IOException e) {
            log.error("Some of resources are not initialized.");
            e.printStackTrace();
        }
        String text = "a";

        // TODO jeśli spełnia zapytanie to wysyłamy zwrotnie wiadomość z tekstem, w przeciwnym przypadku nic nie robimy
        if(true)
            getContext().sender().tell(new PathInfoResponse(textWithQuery.text), getSelf());
    }

    /**
     * Wiadomość od crawlera.
     * @param text
     */
    private void classifyText(TextToClassify text) {
        try {
            NLP nlp = NLP.getInstance();
            List<Pair<String, Double>> pairs = nlp.checkNewTextSimilarityToModel(text.text);

            Pair<String, Double> best = null;

            for (Pair<String, Double> pair : pairs) {
                if (best == null) {
                    best = pair;
                } else {
                    if (pair.getSecond() > best.getSecond()) {
                        best = pair;
                    }
                }
            }
            log.info("Best found classifier is " + best);
            if(best != null && best.getSecond() >= 0.4)
                getContext().sender().tell(new PathInfoRecord(text.text), getSelf());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
