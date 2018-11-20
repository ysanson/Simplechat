import common.ChatIF;
import server.EchoServer;
import java.io.*;
import java.util.List;

public class ServerConsole implements ChatIF {

    final public static int DEFAULT_PORT = 5555;
    private EchoServer server;

    public ServerConsole(int port){
        server=new EchoServer(port, this);

    }

    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }

    @Override
    public void updateClientList(List<String> clients) {

    }


    /**
     * This method waits for input from the console.  Once it is
     * received, it sends it to the client's message handler.
     */
    public void accept()
    {
        try
        {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (true)
            {
                message = fromConsole.readLine();
                server.handleMessageFromServerUI(message);
            }
        }
        catch (Exception ex)
        {
            System.out.println
                    ("Unexpected error while reading from console!");
        }
    }

    /**
     * Main method. Entry point of the server, in console mode.
     * @param args
     */
    public static  void main(String[] args){
        int port = 0; //Port to listen on

        try
        {
            port = Integer.parseInt(args[0]); //Get port from command line
        }
        catch(Throwable t)
        {
            port = DEFAULT_PORT; //Set port to 5555
        }

        ServerConsole sc = new ServerConsole(port);

        sc.accept();

    }
}
