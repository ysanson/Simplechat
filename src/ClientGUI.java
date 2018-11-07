import client.ChatClient;
import common.ChatIF;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;


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
    private String loginName;
    private String serveurID;
    private int currentPort;

    @Override
    public void display(String message) {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 700, 600, Color.LIGHTGRAY);

        TextArea conversations = new TextArea();
        BorderPane.setAlignment(conversations, Pos.TOP_CENTER);
        primaryStage.setScene(scene);
        primaryStage.show();
        connexionUI();
    }
    
    public void connexionUI(){
        Stage connexionStage = new Stage();
        connexionStage.setTitle("Simplechat 4");
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        Scene scene = new Scene(gridPane, 400, 300);
        scene.setFill(Color.LIGHTBLUE);
        Text connexion = new Text("Connexion");
        connexion.setFont(new Font(20));
        Label userName = new Label("Nom d'utilisateur :");
        userName.setAlignment(Pos.CENTER_RIGHT);
        TextField un = new TextField();
        gridPane.add(connexion, 1,0,2,1);
        gridPane.add(userName, 0,1);
        gridPane.add(un, 1,1);

        Label serveur = new Label("Serveur :");
        serveur.setAlignment(Pos.CENTER_RIGHT);
        TextField serv = new TextField();
        gridPane.add(serveur, 0,2);
        gridPane.add(serv, 1,2);

        Label p = new Label("Port :");
        p.setAlignment(Pos.TOP_RIGHT);
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
        Text connexionState = new Text();
        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loginName = un.getText();
                serveurID = serv.getText();
                currentPort = Integer.parseInt(port.getText());
                if(!loginName.isEmpty()&&!serveurID.isEmpty()&&currentPort!=0) {
                    try {
                        createChatClient();
                        connexionState.setFill(Color.LAWNGREEN);
                        connexionState.setText("Connexion établie.");
                        connexionStage.close();


                    } catch (IOException e) {
                        connexionState.setFill(Color.FIREBRICK);
                        connexionState.setText("Impossible de se connecter au serveur !");
                    }
                }else{
                    connexionState.setFill(Color.ORANGE);
                    connexionState.setText("Tous les champs doivent être remplis !");
                }
            }
        });

        gridPane.add(confirm, 1,4);
        gridPane.add(connexionState, 0,5, 2,1);
        connexionStage.setScene(scene);
        connexionStage.show();
    }

    private void createChatClient() throws IOException {
        if(client!=null){
            client.quit();
        }
        client= new ChatClient(serveurID, currentPort, this, loginName);

    }
    public static void main(String[] args){
        Application.launch(ClientGUI.class, args);
    }

}
