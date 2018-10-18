// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import com.lloseng.ocsf.client.ObservableClient;
import common.*;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient implements Observer {
  //Instance variables **********************************************

  /**
   * The interface type variable.  It allows the implementation of
   * the display method in the client.
   */
  ChatIF clientUI;
  private String id;
  private ObservableClient comm;


  //Constructors ****************************************************

  /**
   * Constructs an instance of the chat client.
   *
   * @param host     The server to connect to.
   * @param port     The port number to connect on.
   * @param clientUI The interface type variable.
   */

  public ChatClient(String host, int port, ChatIF clientUI, String id) throws IOException {
    comm = new ObservableClient(host, port);
    comm.addObserver(this);
    this.clientUI = clientUI;
    this.id = id;
    clientUI.display("Started. Type #help for a list of available commands.");
    comm.openConnection();
    if(comm.isConnected()){
        clientUI.display("Now connected. You may be able to send messages from now on.\nType #help for a list of available commands.");
        comm.sendToServer("#login " + id);
    }
  }


  //Instance methods ************************************************

  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) {
      if(((String)msg).equals("#logoff")){
          clientUI.display("Now logging off");
          try {
              comm.closeConnection();
          } catch (IOException e) {}
      }
      clientUI.display(msg.toString());
  }

  /**
   * This method handles all data coming from the UI
   *
   * @param message The message from the UI.
   */
  public void handleMessageFromClientUI(String message) {
    try {
       if(message.startsWith("#")){
           handleCommand(message.substring(1));
       }
       else
           comm.sendToServer(message);
    } catch (IOException e) {
      clientUI.display
              ("Could not send message to server.  Terminating client.");
      quit();
    }
  }

    /**
     * This method handles all commands that comes from the UI
     * @param command the command from the UI
     */
  public void handleCommand(String command){
        if(command.equals("quit")){
            if(!comm.isConnected())
                quit();
            else
                clientUI.display("You can't quit without being disconnected");
        }
        else if(command.equals("logoff")){
            if(comm.isConnected()) {
                try {
                    comm.sendToServer("#logoff");
                    comm.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(command.contains("sethost")){
            String[] args = command.split(" ");
            try{
                comm.setHost(args[1]);
                clientUI.display("Host set.");
            }catch (Exception e){
                clientUI.display("Invalid arguments. Command #sethost [host]");
            }
        }
        else if(command.contains("setport")){
            String[] args = command.split(" ");
            try{
                comm.setPort(Integer.parseInt(args[1]));
                clientUI.display("Port set.");
            }catch (Exception e){
                clientUI.display("Invalid arguments. Command #setport [port]");
            }
        }
        else if(command.contains("login")){
            if(!comm.isConnected()) {
                try {
                    comm.openConnection();
                    if(comm.isConnected()){
                        clientUI.display("Now connected. You may be able to send messages from now on.\nType #help for a list of available commands.");
                        comm.sendToServer("#login " + id);
                    }
                } catch (IOException e) {
                    clientUI.display("Error in connecting to the server.");
                }
            }
            else
                clientUI.display("You are already connected to a server. Try disconnect first.");
        }else if(command.contains("id")){
            try{
                id=command.split(" ")[1];
                clientUI.display("ID changed. Now is " + id);
            }catch (Exception e){
                clientUI.display("Error. Command usage: #id [name]");
            }
        }
        else if(command.contains("help"))
            clientUI.display("#quit / #logoff / sethost [host] / #setport [port] / #login");
        else
            clientUI.display("Error. Invalid command. PLease type #help for a list of commands.");
  }

  /**
   * This method terminates the client.
   */
  public void quit() {
      if(comm.isConnected()) {
          try {
              comm.sendToServer("#logoff");
              comm.closeConnection();
          } catch (IOException e) {
          }
      }
    System.exit(0);
  }

  public void connectionClosed(){
      clientUI.display("The connection is closed by the server");
  }

  public void connectionEstablished(){
      clientUI.display("Connection is established with the server");
  }

  public void connectionException(Exception exc){
      clientUI.display("Exception with the connection to the server: " + exc.toString());
  }


    @Override
    public void update(Observable o, Object arg) {
      if(arg instanceof String){
          if(arg.equals(ObservableClient.CONNECTION_CLOSED))
              this.connectionClosed();
          else if(arg.equals(ObservableClient.CONNECTION_ESTABLISHED))
              connectionEstablished();
          else
              this.handleMessageFromServer(arg);
      }
      else if(arg instanceof Exception)
          this.connectionException((Exception)arg);

    }
}
//End of ChatClient class
