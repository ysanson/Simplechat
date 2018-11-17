import client.ChatClient;
import common.ChatIF;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Optional;


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
    private ChatClient client;
    private String loginName;
    private String serveurID;
    private int currentPort;
    private BooleanProperty connexionStatus = new SimpleBooleanProperty(false); //False: not connected True: connected
    private StringProperty msg = new SimpleStringProperty();
    private Text connectedUsers;

    /**
     * Display the message being received.
     * @param message The message received.
     */
    @Override
    public void display(String message) {
        msg.setValue(message);
    }

    /**
     * ChatIF method: call the refreshing of the client list in the UI.
     */
    @Override
    public void updateClientList(){
        StringBuilder sb = new StringBuilder();
        for (String c:client.getListofClientsConnected()) {
            sb.append(c);
            sb.append("\n");
        }
        connectedUsers.setText(sb.toString());
    }

    /**
     * Creating the main frame
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage){
        BorderPane root = new BorderPane();
        int defaultHeight = 800, defaultWidth = 1000;
        Scene scene = new Scene(root, defaultWidth, defaultHeight, Color.LIGHTGRAY);
        //Title bar
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
        Button connectBtn = new Button("Connexion");
        connectBtn.setOnAction(event -> {
            if(client!=null){
                Alert disc = new Alert(Alert.AlertType.CONFIRMATION);
                disc.setTitle("Déconnexion");
                disc.setContentText("Voulez-vous vraiment vous déconnecter ?");
                Optional<ButtonType> result = disc.showAndWait();
                if(result.get()==ButtonType.OK){
                    client.disconnect();
                    client=null;
                    connexionStatus.set(false);
                    connectedUsers.setText("");
                }
            }else{
                connectionUI();
            }
        });
        titleBar.getChildren().addAll(title, status, pseudo, serverName, portNumber, connectBtn);

        //Middle part
        ScrollPane conversations = new ScrollPane();
        conversations.setFitToWidth(true);
        conversations.setFitToHeight(true);
        conversations.setPrefSize(700, defaultHeight);
        Text convo = new Text();
        convo.setFontSmoothingType(FontSmoothingType.LCD);
        convo.setWrappingWidth(690);
        conversations.setContent(convo);

        //Bottom part
        HBox sendMsgBar = new HBox();
        sendMsgBar.setSpacing(20);
        sendMsgBar.setPadding(new Insets(15, 12, 15, 12));
        Text enterMsg = new Text("Entrez votre message : ");
        enterMsg.setFont(Font.font("Cambria", FontPosture.ITALIC, 15));
        TextField msgInput = new TextField();
        msgInput.setPrefWidth(defaultWidth-300);
        Button sendBtn = new Button("Envoyer");
        msgInput.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                if(client!=null){
                    client.handleMessageFromClientUI(msgInput.getText());
                    msgInput.setText("");
                }
            }
        });
        sendBtn.setOnAction(event -> {
            if(client!=null){
                client.handleMessageFromClientUI(msgInput.getText());
                msgInput.setText("");
            }
        });

        sendMsgBar.getChildren().addAll(enterMsg, msgInput, sendBtn);

        //Right part
        VBox connectedPane = new VBox();
        connectedPane.setSpacing(20);
        connectedPane.setPrefWidth(200);
        connectedPane.setPadding(new Insets(15, 12, 15, 12));
        root.setRight(connectedPane);
        Text connectedTitle = new Text("Utilisateurs connectés");
        connectedTitle.setFont(Font.font("Cambria", 15));
        connectedUsers = new Text();
        connectedUsers.setWrappingWidth(140);
        connectedPane.getChildren().addAll(connectedTitle, connectedUsers);

        msg.addListener((observable, oldValue, newValue) -> convo.setText(convo.getText() + "\n" + msg.getValue()));
        root.setBottom(sendMsgBar);
        root.setCenter(conversations);
        connexionStatus.addListener((observable, oldValue, newValue) -> {
            if(connexionStatus.getValue()) {
                status.setText("Statut : connecté");
                status.setFill(Color.DARKGREEN);
                pseudo.setText("Pseudo : " + loginName);
                pseudo.setFill(Color.DARKBLUE);
                serverName.setText("Serveur : " +serveurID);
                portNumber.setText("Port : " + currentPort);
                connectBtn.setText("Déconnexion");
            }
            else {
                status.setText("Statut : déconnecté");
                status.setFill(Color.FIREBRICK);
                pseudo.setText("Pseudo : ");
                serverName.setText("Serveur : ");
                portNumber.setText("Port : ");
                connectBtn.setText("Connexion");
            }
        });


        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            if(client!=null){
                client.quit();
            }
        });
        connectionUI();
    }

    /**
     * Creating the Connection window.
     */
    private void connectionUI(){
        Stage connexionStage = new Stage();
        connexionStage.setAlwaysOnTop(true);
        connexionStage.setResizable(false);
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

    /**
     * Used to create a ChatClient instance.
     * @throws IOException
     */
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
