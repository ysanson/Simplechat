import client.ChatClient;
import common.ChatIF;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
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
    private BooleanProperty connexionStatus = new SimpleBooleanProperty(false);
    private StringProperty msg = new SimpleStringProperty();

    @Override
    public void display(String message) {
        msg.setValue(message);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 700, Color.LIGHTGRAY);
        HBox titleBar = new HBox();
        Text title = new Text("Simple Chat 4");
        title.setFont(Font.font("Cambria", 20));
        root.setTop(titleBar);
        titleBar.setSpacing(20);
        titleBar.setPadding(new Insets(15, 12, 15, 12));
        Text status = new Text("Statut: déconnecté");
        status.setFill(Color.FIREBRICK);
        Text pseudo = new Text("Pseudo :");
        Text serverName = new Text("Serveur : ");
        Text portNumber = new Text("Port : ");
        ScrollPane conversations = new ScrollPane();
        conversations.setFitToWidth(true);
        conversations.setFitToHeight(true);
        TextArea convo = new TextArea();
        convo.setPrefSize(799,600);
        convo.setDisable(true);
        conversations.setPrefSize(800, 500);
        conversations.setContent(convo);
        HBox sendMsgBar = new HBox();
        sendMsgBar.setSpacing(20);
        sendMsgBar.setPadding(new Insets(15, 12, 15, 12));
        Text enterMsg = new Text("Entrez votre message : ");
        enterMsg.setFont(Font.font("Cambria", FontPosture.ITALIC, 15));
        TextField msgInput = new TextField();
        msgInput.setPrefWidth(500);
        Button sendBtn = new Button("Envoyer");


        connexionStatus.addListener((observable, oldValue, newValue) -> {
            if(connexionStatus.getValue()) {
                status.setText("Statut : connecté");
                status.setFill(Color.DARKGREEN);
                pseudo.setText("Pseudo : " + loginName);
                pseudo.setFill(Color.DARKBLUE);
                serverName.setText("Serveur : " +serveurID);
                portNumber.setText("Port : " + currentPort);
            }
            else {
                status.setText("Statut : déconnecté");
                status.setFill(Color.FIREBRICK);
                pseudo.setText("Pseudo : ");
                serverName.setText("Serveur : ");
                portNumber.setText("Port : ");
            }
        });

        msg.addListener((observable, oldValue, newValue) -> convo.setText(convo.getText() + "\n" + msg.getValue()));
        root.setBottom(sendMsgBar);
        root.setCenter(conversations);
        titleBar.getChildren().addAll(title, status, pseudo, serverName, portNumber);
        sendMsgBar.getChildren().addAll(enterMsg, msgInput, sendBtn);
        primaryStage.setScene(scene);
        primaryStage.show();
        connexionUI();
    }
    
    public void connexionUI(){
        Stage connexionStage = new Stage();
        connexionStage.setTitle("Connexion");
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
        port.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d{0,4}?"))
                port.setText(oldValue);
        });
        gridPane.add(p, 0,3);
        gridPane.add(port, 1,3);

        Button confirm = new Button("Connexion");
        Text connexionState = new Text();
        confirm.setOnAction(event -> {
            loginName = un.getText();
            serveurID = serv.getText();
            currentPort = Integer.parseInt(port.getText());
            if(!loginName.isEmpty()&&!serveurID.isEmpty()&&currentPort!=0) {
                try {
                    createChatClient();
                    connexionState.setFill(Color.LAWNGREEN);
                    connexionState.setText("Connexion établie.");
                    connexionStatus.setValue(true);
                    connexionStage.close();


                } catch (IOException e) {
                    connexionState.setFill(Color.FIREBRICK);
                    connexionState.setText("Impossible de se connecter au serveur !");
                }
            }else{
                connexionState.setFill(Color.ORANGE);
                connexionState.setText("Tous les champs doivent être remplis !");
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
