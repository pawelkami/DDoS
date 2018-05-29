package pl.edu.pw.elka.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pl.edu.pw.elka.actors.ConsoleActor.PathInfoResponse;
import pl.edu.pw.elka.actors.DatabaseActor.PathInfoRecord;

public class NLPActor extends AbstractActor {

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

    private void analyseQuery(TextWithQuery textWithQuery) {
        // TODO analiza wiadomości

        // TODO jeśli spełnia zapytanie to wysyłamy zwrotnie wiadomość z tekstem, w przeciwnym przypadku nic nie robimy
        if(true)
            getContext().getParent().tell(new PathInfoResponse(textWithQuery.text), getSelf());
    }

    private void classifyText(TextToClassify text) {
        // TODO klasyfikowanie tekstu - sprawdzenie czy pasuje do naszego wzorca
        if(true)    // TODO jeśli jest z naszej kategorii to wysyłamy wiadomość, w przeciwnym wypadku nic
            getContext().sender().tell(new PathInfoRecord(text.text), getSelf());
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
