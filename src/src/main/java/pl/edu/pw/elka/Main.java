package pl.edu.pw.elka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerFactory;
import pl.edu.pw.elka.actors.ConsoleActor;
import pl.edu.pw.elka.nlp.NLP;

import java.io.IOException;

class Main {


    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.create("ddos");
        final ActorRef consoleActor = actorSystem.actorOf(ConsoleActor.props(), "console");

        //final Inbox inbox = Inbox.create(actorSystem);

    }

}