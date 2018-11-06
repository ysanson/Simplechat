import client.ChatClient;
import common.ChatIF;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class ClientGUI extends Application implements ChatIF {
    //Class variables *************************************************

    /**
     * The default port to connect on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Instance variables **********************************************

    /**
     * The instance of the client that created this ConsoleChat.
     */
    ChatClient client;

    @Override
    public void display(String message) {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Simplechat 4");
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(gridPane, 400, 300);
        scene.setFill(Color.LIGHTBLUE);
        Text connexion = new Text("Connexion");
        connexion.setFont(new Font(20));
        Label userName = new Label("Nom d'utilisateur :");
        TextField un = new TextField();
        gridPane.add(connexion, 1,0,2,1);
        gridPane.add(userName, 0,1);
        gridPane.add(un, 1,1);

        Label serveur = new Label("Serveur :");
        TextField serv = new TextField();
        gridPane.add(serveur, 0,2);
        gridPane.add(serv, 1,2);

        Label p = new Label("Port :");
        TextField port = new TextField();
        port.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("\\d{0,4}?"))
                    port.setText(oldValue);
            }
        });
        gridPane.add(p, 0,3);
        gridPane.add(port, 1,3);

        Button confirm = new Button("Connexion");
        gridPane.add(confirm, 1,4);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        Application.launch(ClientGUI.class, args);
    }

}
