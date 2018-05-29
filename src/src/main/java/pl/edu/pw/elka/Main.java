package pl.edu.pw.elka;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import pl.edu.pw.elka.actors.ConsoleActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

class Main
{
    public static void main(String[] args)
    {
        final ActorSystem actorSystem = ActorSystem.create("ddos");
        final ActorRef consoleActor = actorSystem.actorOf(ConsoleActor.props(), "console");

        final Inbox inbox = Inbox.create(actorSystem);

        //consoleActor.tell(new ConsoleActor.ConsolePing(), ActorRef.noSender());

        actorSystem.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), consoleActor, new ConsoleActor.ConsolePing(), actorSystem.dispatcher(), ActorRef.noSender());

    }

}