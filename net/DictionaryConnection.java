package ca.yorku.eecs3214.dict.net;

import ca.yorku.eecs3214.dict.model.Database;
import ca.yorku.eecs3214.dict.model.Definition;
import ca.yorku.eecs3214.dict.model.MatchingStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class DictionaryConnection {

    private static final int DEFAULT_PORT = 2628;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Establishes a new connection with a DICT server using an explicit host and port number, and handles initial
     * welcome messages. This constructor does not send any request for additional data.
     *
     * @param host Name of the host where the DICT server is running
     * @param port Port number used by the DICT server
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the welcome
     *                                 messages are not successful.
     * @throws IOException
     * @throws UnknownHostException
     */
    public DictionaryConnection(String host, int port) throws DictConnectionException {

        try{
            clientSocket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            if(Status.readStatus(in).getStatusType() > 2)
                throw new DictConnectionException();
            else
                System.out.println("Connection Successful!");
        } catch(IOException e) {
            throw new DictConnectionException();
        }
    }

    /**
     * Establishes a new connection with a DICT server using an explicit host, with the default DICT port number, and
     * handles initial welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the welcome
     *                                 messages are not successful.
     * @throws IOException
     * @throws UnknownHostException
     */
    public DictionaryConnection(String host) throws DictConnectionException {
        this(host, DEFAULT_PORT);
    }

    /**
     * Sends the final QUIT message, waits for its reply, and closes the connection with the server. This function
     * ignores any exception that may happen while sending the message, receiving its reply, or closing the connection.
     * @throws IOException
     */
    public synchronized void close() {

        try {
            out.write("QUIT");
            System.out.println("QUIT!");
            out.close();
            in.close();
            clientSocket.close();
        } catch(IOException e) {}
    }

    /**
     * Requests and retrieves a map of database name to an equivalent database object for all valid databases used in
     * the server.
     *
     * @return A map linking database names to Database objects for all databases supported by the server, or an empty
     * map if no databases are available.
     * @throws DictConnectionException If the connection is interrupted or the messages don't match their expected
     *                                 value.
     * @throws IOException
     */
    public synchronized Map<String, Database> getDatabaseList() throws DictConnectionException {
        Map<String, Database> databaseMap = new HashMap<>();
        
        out.write("SHOW DB\r\n");
        out.flush();
        try {
            if(Status.readStatus(in).getStatusType() > 1)
                return databaseMap;

            String line;
            while (!((line = in.readLine()).equals("."))) {
                List<String> list = DictStringParser.splitAtoms(line);
                Database database = new Database(list.get(0), list.get(1));
                databaseMap.put(database.getName(), database);
            }
        } catch(IOException e) {
            throw new DictConnectionException();
        }

        if(Status.readStatus(in).getStatusCode() == 250)
            return databaseMap;
        else
            throw new DictConnectionException();
    }

    /**
     * Requests and retrieves a list of all valid matching strategies supported by the server. Matching strategies are
     * used in getMatchList() to identify how to suggest words that match a specific pattern. For example, the "prefix"
     * strategy suggests words that start with a specific pattern.
     *
     * @return A set of MatchingStrategy objects supported by the server, or an empty set if no strategies are
     * supported.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected
     *                                 value.
     */
    public synchronized Set<MatchingStrategy> getStrategyList() throws DictConnectionException {
        Set<MatchingStrategy> set = new LinkedHashSet<>();

        out.write("SHOW STRAT\r\n");
        out.flush();

        try {
            if(Status.readStatus(in).getStatusType() > 1)
                return set;

            String line;
            while(!((line = in.readLine()).equals("."))) {
                List<String> list = DictStringParser.splitAtoms(line);
                MatchingStrategy strat = new MatchingStrategy(list.get(0), list.get(1));
                set.add(strat);
            }
        } catch(IOException e) {
            throw new DictConnectionException();
        }
        
        if(Status.readStatus(in).getStatusCode() == 250)
            return set;
        else
            throw new DictConnectionException();
    }

    /**
     * Requests and retrieves a list of matches for a specific word pattern.
     *
     * @param pattern  The pattern to use to identify word matches.
     * @param strategy The strategy to be used to compare the list of matches.
     * @param database The database where matches are to be found. Special databases like Database.DATABASE_ANY or
     *                 Database.DATABASE_FIRST_MATCH are supported.
     * @return A set of word matches returned by the server based on the word pattern, or an empty set if no matches
     * were found.
     * @throws DictConnectionException If the connection was interrupted, the messages don't match their expected value,
     *                                 or the database or strategy are not supported by the server.
     */
    public synchronized Set<String> getMatchList(String pattern, MatchingStrategy strategy, Database database) throws DictConnectionException {
        Set<String> set = new LinkedHashSet<>();

        String pat = "\"".concat(pattern).concat("\"");
        out.write("MATCH " + database.getName() + " " + strategy.getName() + " " + pat + "\r\n");
        out.flush();

        int code = Status.readStatus(in).getStatusCode();
        
        try {
            if(code == 552)
                return set;
            else if(code > 152)
                throw new DictConnectionException();
            
            String line;
            while(!((line = in.readLine()).equals("."))) {
                List<String> list = DictStringParser.splitAtoms(line);
                set.add(list.get(1));
            }
        } catch(IOException e) {
            throw new DictConnectionException();
        }
        

        if(Status.readStatus(in).getStatusCode() == 250)
            return set;
        else
            throw new DictConnectionException();
    }

    /**
     * Requests and retrieves all definitions for a specific word.
     *
     * @param word     The word whose definition is to be retrieved.
     * @param database The database to be used to retrieve the definition. Special databases like Database.DATABASE_ANY
     *                 or Database.DATABASE_FIRST_MATCH are supported.
     * @return A collection of Definition objects containing all definitions returned by the server, or an empty
     * collection if no definitions were available.
     * @throws DictConnectionException If the connection was interrupted, the messages don't match their expected value,
     *                                 or the database is not supported by the server.
     */
    public synchronized Collection<Definition> getDefinitions(String word, Database database) throws DictConnectionException {
        Collection<Definition> set = new ArrayList<>();

        String w = "\"".concat(word).concat("\"");
        out.write("DEFINE " + database.getName() + " " + w + "\r\n");
        out.flush();

        int code = Status.readStatus(in).getStatusCode();
        List<String> str;
        
        try {
            
            if(code == 552)
                return set;
            else if(code > 150)
                throw new DictConnectionException();
            
            String line;
            
            while((str = DictStringParser.splitAtoms(in.readLine())).get(0).equals("151")) {
                String db = str.get(2);
                Definition definition = new Definition(word, db);
                while(!((line = in.readLine()).equals(".")))
                   definition.appendDefinition(line);
                set.add(definition);
            }
        } catch(IOException e) {
            throw new DictConnectionException();
        } catch(NumberFormatException e) {
            throw new DictConnectionException();
        }
        
        if(str.get(0).equals("250"))
            return set;
        else
            throw new DictConnectionException();

    }
}
