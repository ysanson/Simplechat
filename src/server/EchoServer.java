package server;// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.util.Observable;
import java.util.Observer;
import com.lloseng.ocsf.server.ConnectionToClient;
import com.lloseng.ocsf.server.ObservableOriginatorServer;
import com.lloseng.ocsf.server.OriginatorMessage;
import common.ChatIF;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer implements Observer {
    //Class variables *************************************************

    ChatIF serverUI;
    ObservableOriginatorServer comm;

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port, ChatIF serverUI) {
        comm = new ObservableOriginatorServer(port);
        comm.addObserver(this);
        this.serverUI = serverUI;
        serverUI.display("Type #help for a list of commands, #start to start the server.");
    }


    //Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client) {
        if (((String) msg).startsWith("#"))
            handleCmdFromClient(((String) msg).substring(1), client);
        else {
            serverUI.display("Message received: " + msg + " from " + client.getInfo("id"));
            comm.sendToAllClients(client.getInfo("id") + ">" + msg);
        }
    }

    /**
     * This method handles commands received from the client.
     *
     * @param cmd    the command
     * @param client The connection from which the message originated
     */
    public void handleCmdFromClient(String cmd, ConnectionToClient client) {
        if (!cmd.contains("login") && client.getInfo("id") == null) { //If the client has not connected yet and the command is not a login
            try {
                client.sendToClient("Error: unexpected command. On first connection, please consider to use #login [id].");
                client.close();
            } catch (IOException e) {
            }
        } else {
            if (cmd.equals("logoff")) {
                try {
                    client.close();
                } catch (IOException e) {
                    serverUI.display("Error in disconnecting the client");
                }
            } else if (cmd.contains("login")) {
                if (client.getInfo("id") == null) {
                    try {
                        client.setInfo("id", cmd.split(" ")[1]);
                        serverUI.display("Client " + client.getInfo("id") + " has connected.");
                    } catch (Exception e) {
                        serverUI.display("Invalid command format for the client " + client.getId());
                    }
                } else {
                    try {
                        client.sendToClient("Invalid command: you are already connected. ");
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        serverUI.display
                ("Server listening for connections on port " + comm.getPort());
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped() {
        serverUI.display
                ("Server has stopped listening for connections.");
    }

    protected void serverClosed(){
        serverUI.display("Server closed.");
    }

    protected void listeningException(Object msg){
        serverUI.display(msg.toString());
    }

    protected void clientConnected(ConnectionToClient client) {
        serverUI.display("Client " + client.getId() + " has connected.");
    }


    synchronized protected void clientDisconnected(ConnectionToClient client) {
        serverUI.display("Client " + client.getId() + " has disconnected.");
    }

    synchronized protected void clientException(
            ConnectionToClient client, Throwable exception) {
        serverUI.display("Client " + client.getId() + " disconnected abnormally.");
    }


    public void handleMessageFromServerUI(String message) {
        if (message.startsWith("#")) {
            handleCommand(message.substring(1));
        } else
            comm.sendToAllClients("SERVER MSG >" + message);
    }

    public void handleCommand(String command) {
        if (command.equals("start")) {
            if (!comm.isListening()) {
                try {
                    comm.listen();
                } catch (IOException e) {
                    serverUI.display("The server can't start listening, as it's already listening.");
                }
            }
        } else if (command.equals("quit")) {
            try {
                comm.close();
            } catch (IOException e) {
                serverUI.display("Can't close properly.");
            }
        } else if (command.equals("stop")) {
            comm.stopListening();
        } else if (command.equals("close")) {
            comm.stopListening();
            Thread[] clientThreadList = comm.getClientConnections();
            for (int i = 0; i < clientThreadList.length; i++) {
                try {
                    ((ConnectionToClient) clientThreadList[i]).close();
                }
                // Ignore all exceptions when closing clients.
                catch (Exception ex) {
                }
            }
        } else if (command.contains("setport")) {
            String[] args = command.split(" ");
            try {
                comm.setPort(Integer.parseInt(args[1]));
                serverUI.display("Port set.");
            } catch (Exception e) {
                serverUI.display("Error. Command usage: setport [port]");
            }
        } else if (command.contains("getport")) {
            serverUI.display(Integer.toString(comm.getPort()));
        } else if (command.contains("help"))
            serverUI.display("#start / #quit / #stop / #close / #setport [port] / #getport");
        else
            serverUI.display("Unknown command. Type #help for a list of commands.");
    }

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof OriginatorMessage){
            if(((OriginatorMessage) o).getOriginator()==null){
                if(((OriginatorMessage) o).getMessage().equals(ObservableOriginatorServer.SERVER_STARTED)){
                    this.serverStarted();
                }else if(((OriginatorMessage) o).getMessage().equals(ObservableOriginatorServer.SERVER_STOPPED))
                    this.serverStopped();
                else if(((OriginatorMessage) o).getMessage().equals(ObservableOriginatorServer.SERVER_CLOSED))
                    this.serverClosed();
                else if(((OriginatorMessage) o).getMessage().toString().contains(ObservableOriginatorServer.LISTENING_EXCEPTION))
                    this.listeningException(((OriginatorMessage) o).getMessage());
            }
            else{
                if(((OriginatorMessage) o).getMessage().equals(ObservableOriginatorServer.CLIENT_CONNECTED))
                    this.clientConnected(((OriginatorMessage) o).getOriginator());
                else if(((OriginatorMessage) o).getMessage().equals(ObservableOriginatorServer.CLIENT_DISCONNECTED))
                    this.clientDisconnected(((OriginatorMessage) o).getOriginator());
                else if(((OriginatorMessage) o).getMessage().toString().contains(ObservableOriginatorServer.CLIENT_EXCEPTION))
                    this.clientException(((OriginatorMessage) o).getOriginator(), (Throwable) ((OriginatorMessage) o).getMessage());
                else
                    this.handleMessageFromClient(((OriginatorMessage) o).getMessage(), ((OriginatorMessage) o).getOriginator());

            }
        }
    }

    //Class methods ***************************************************
}
//End of server.EchoServer class
