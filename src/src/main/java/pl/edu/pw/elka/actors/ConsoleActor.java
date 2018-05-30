package pl.edu.pw.elka.actors;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.FSM;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import pl.edu.pw.elka.actors.SearcherAgent.SearchPathInfoQuery;
import pl.edu.pw.elka.utils.ConfigParser;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

enum ConsoleState {
    USER_INPUT,
    WAITING_FOR_RESPONSES,
}

class ConsoleNoDataIsNeeded {
}

public class ConsoleActor extends AbstractFSM<ConsoleState, ConsoleNoDataIsNeeded> {

    /**
     * Klasa reprezentująca wiadomość, która startuje agenta.
     */
    static public class ConsolePing {
    }

    static class PathInfoResponse {
        final String pathInfo;

        PathInfoResponse(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    }

    /**
     * Czekamy 5 sekund na wiadomości zwrotne.
     */
    private final long MAX_WAIT_RESPONSE = 5000;

    private long queryStart;

    private Router router;

    public static Props props() {
        return Props.create(ConsoleActor.class, ConsoleActor::new);
    }

    private FSM.State<ConsoleState, ConsoleNoDataIsNeeded> printReceivedPath(PathInfoResponse pathInfo, ConsoleNoDataIsNeeded noData) {
        System.out.println(pathInfo.pathInfo);
        return checkIfTimeout();
    }

    private FSM.State<ConsoleState, ConsoleNoDataIsNeeded> checkIfTimeout() {
        System.out.println("SPRAWDZAMY TIMEOUT");
        if (Instant.now().toEpochMilli() - queryStart >= MAX_WAIT_RESPONSE)
            return goTo(ConsoleState.USER_INPUT);
        else
            return stay();
    }

    private FSM.State<ConsoleState, ConsoleNoDataIsNeeded> searchPaths(SearchPathInfoQuery path, ConsoleNoDataIsNeeded noData) {
        queryStart = Instant.now().toEpochMilli();
        router.route(new SearchPathInfoQuery(path.query), getContext().self());
        return goTo(ConsoleState.WAITING_FOR_RESPONSES);
    }


    private void printMenu() {
        System.out.println("What do you want to do?");
        System.out.println("1. Search path.");
        System.out.println("2. Exit program.");
    }

    private UserChoice getUserChoice() {
        Scanner scan = new Scanner(System.in);
        int choice = -1;
        while (choice == -1) {
            try {
                choice = scan.nextInt();
            } catch (Exception ignored) {
                scan = new Scanner(System.in);
            }
        }
        if (choice == UserChoice.EXIT_PROGRAM.ordinal() + 1)
            return UserChoice.EXIT_PROGRAM;
        else if (choice == UserChoice.SEARCH_PATH.ordinal() + 1)
            return UserChoice.SEARCH_PATH;
        else
            return UserChoice.UNDEFINED_USER_CHOICE;
    }

    private String getUserTextToFind() {
        System.out.print("Search query: ");
        Scanner scan = new Scanner(System.in);
        return scan.nextLine();
    }

    enum UserChoice {
        SEARCH_PATH,
        EXIT_PROGRAM,
        UNDEFINED_USER_CHOICE

    }

    private void run() {
        boolean isInput = false;
        while (!isInput) {
            printMenu();
            switch (getUserChoice()) {
                case SEARCH_PATH:
                    isInput = true;
                    context().self().tell(new SearchPathInfoQuery(getUserTextToFind()), context().self());
                    break;
                case EXIT_PROGRAM:
                    System.exit(0);
                    break;
                case UNDEFINED_USER_CHOICE:
                    break;
            }
        }

    }

    {
        startWith(ConsoleState.USER_INPUT, new ConsoleNoDataIsNeeded());

        when(ConsoleState.USER_INPUT,
                matchEvent(ConsolePing.class,
                        ConsoleNoDataIsNeeded.class,
                        (starter, noData) -> {
                            return stay();
                        })
        );

        when(ConsoleState.USER_INPUT,
                matchEvent(PathInfoResponse.class,
                        (pathInfo, noData) -> {
                            System.out.println("Message after timeout. We ignore it!");
                            return stay();
                        })
        );

        when(ConsoleState.WAITING_FOR_RESPONSES,
                matchEvent(PathInfoResponse.class,
                        ConsoleNoDataIsNeeded.class,
                        this::printReceivedPath));

        when(ConsoleState.WAITING_FOR_RESPONSES,
                matchEvent(ConsolePing.class,
                        ConsoleNoDataIsNeeded.class,
                        (starter, noData) -> checkIfTimeout()
                )
        );

        when(ConsoleState.USER_INPUT,
                matchEvent(SearchPathInfoQuery.class,
                        ConsoleNoDataIsNeeded.class,
                        this::searchPaths));

        onTransition(
                matchState(ConsoleState.WAITING_FOR_RESPONSES,
                        ConsoleState.USER_INPUT,
                        this::run)
        );

        whenUnhandled(matchAnyEvent((any, noData) -> {
                    System.out.println("Received unhandled request");
                    return stay();
                })
        );
        timer


        List<Routee> routees = new ArrayList<>();
        // TODO czytać z configa i tworzyć odpowiednie agenty, teraz tymczasowe rozwiązanie
        try {
            ConfigParser configParser = new ConfigParser(Objects.requireNonNull(getClass().getClassLoader().getResource("config.json")).getPath());
            for (Object o : configParser.getAttributesList()) {
                JSONObject jsonObj = (JSONObject)o;
                ActorRef r = getContext().actorOf(SearcherAgent.props((String)jsonObj.get("url"), (String)jsonObj.get("htmlTagType"), (String)jsonObj.get("htmlTagValue")));
                getContext().watch(r);
                routees.add(new ActorRefRoutee(r));
            }
            router = new Router(new BroadcastRoutingLogic(), routees);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("Couldn't parse config.json file!!!");
            System.exit(1);
        }


        run();
    }
}
