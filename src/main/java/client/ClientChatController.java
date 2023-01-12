package client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientChatController extends Client implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label discussionLabel;
    @FXML
    private Button deconnecterButton;
    @FXML
    private ListView<String> listeAfficheUtilisateurs;
    @FXML
    private AnchorPane discussionPane;
    @FXML
    private ScrollPane messageTextArea;
    @FXML
    public VBox vBoxMessages;
    @FXML
    private TextField messageInput;
    @FXML
    private Button creerUnGroupe;
    @FXML
    private Button sendMessageButton;


    private String destination_utilisateur = "";
    private String nom_utilisateur = "";
    private static int check_liste = 0;
    private TextField groupeName = new TextField();
    private ListView<String> listeUtilisateursPourChoisir = null;
    private Button createTheGroup = new Button();

    public ClientChatController() throws SocketException {}


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        creerUnGroupe.setVisible(false);
        check_liste = 0; //seulement pour eviter le probleme de la liste affichee lorsque la liste est met a jour --Hard Coding!!

        //-------------------------------------Afficher nom d'utilisateur
        demandeUserName();
        nom_utilisateur = getUserName(); //Reponse de serveur
        welcomeLabel.setText(nom_utilisateur);
        //-------------------------------------End Afficher nom d'utilisateur

        //-------------------------------------Afficher les utilisateurs connectes
        demandeListe();
        listeUtilisateurs = receptListeInitial(nom_utilisateur); //Reponse de serveur initiale
        updateListOnAction();
        //-------------------------------------End Afficher les utilisateurs connectes

        //----------------Le thread pour recevoir les autres packets tous le temps
        try {
            reception(nom_utilisateur,vBoxMessages,listeAfficheUtilisateurs);
        } catch (SocketException e) {}
        //----------------End Le thread pour recevoir les autres packets tous le temps
    }

    public void deconnecterButtonOnAction() throws IOException {
        //-----------------The server removes the user from session
        decconnectUser();
        //-----------------La feetre de Chat est fermee
        Stage stage = (Stage) deconnecterButton.getScene().getWindow();
        stage.close();
        //-----------------La fenetre de Login est affichee
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 350);
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setScene(scene);
        newStage.show();
    }

    public void sendMessageButtonOnAction(){
        if(messageInput.getText().isBlank() == false){
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.setPadding(new Insets(5,5,5,10));

            Text text = new Text(messageInput.getText());
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-color:rgb(239,242,255);"+" -fx-background-color:rgb(15,125,242);"+" -fx-background-radius: 7px 2px 2px 7px");
            textFlow.setPadding(new Insets(5,10,5,10));
            textFlow.setMaxWidth(300);
            text.setFill(Color.color(0.934,0.945,0.996));

            hbox.getChildren().add(textFlow);
            vBoxMessages.getChildren().add(hbox);

            envoiMessage(messageInput.getText(),destination_utilisateur);
            messageInput.clear();
        }
    }

    public void updateListOnAction(){
        creerUnGroupe.setVisible(false);
        discussionPane.setVisible(false);
        if(groupeName != null){
            groupeName.setVisible(false);
        }
        if(listeUtilisateursPourChoisir != null){
            listeUtilisateursPourChoisir.setVisible(false);
        }
        if(createTheGroup != null){
            createTheGroup.setVisible(false);
        }

        messageTextArea.setVisible(true);
        messageInput.setVisible(true);
        sendMessageButton.setVisible(true);
        listeAfficheUtilisateurs.setVisible(true);
        check_liste = 0;
        System.out.println("Updating...");
        System.out.println("The List : ");
        for(int i = 0 ; i < listeUtilisateurs.length ; i++){
            System.out.println(listeUtilisateurs[i]);
        }
        listeAfficheUtilisateurs.getItems().setAll(listeUtilisateurs);
        listeAfficheUtilisateurs.getItems();
        listeAfficheUtilisateurs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(check_liste == 0){
                    destination_utilisateur = listeAfficheUtilisateurs.getSelectionModel().getSelectedItem();
                }
                if(destination_utilisateur != null){
                    int count = 0;
                    int notSelected = -1;
                    ObservableList<String> list = listeAfficheUtilisateurs.getItems();
                    for (String item : list)
                    {
                        System.out.println("foooooooooooooooooooor");
                        System.out.println(list);
                        System.out.println(item);
                        if(item.equals(destination_utilisateur)){
                            System.out.println("equals : "+destination_utilisateur);
                            listeAfficheUtilisateurs.getSelectionModel().select(count);
                            notSelected = 0;
                            break;
                        }else{
                            notSelected = -1;
                        }
                        count++;
                    }
                    if(notSelected == 0){
                        discussionLabel.setText(destination_utilisateur);
                        discussionPane.setVisible(true);

                        //Messages
                        vBoxMessages.heightProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                                messageTextArea.setVvalue((Double) t1);
                            }
                        });

                        messageInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
                            if (event.getCode() == KeyCode.ENTER) {
                                sendMessageButtonOnAction();
                            }
                        });
                    }else{
                        discussionLabel.setText("Votre Espace");
                        discussionPane.setVisible(false);
                    }
                }else{
                    discussionLabel.setText("Votre Espace");
                    discussionPane.setVisible(false);
                }
            }
        });
    }

    public static void showMessage(String messageClient, VBox vBox){
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,5,5,10));

        Text text = new Text(messageClient);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
        textFlow.setPadding(new Insets(5,10,5,10));
        textFlow.setMaxWidth(300);

        hbox.getChildren().add(textFlow);

        //vBox.getChildren().add(hbox);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hbox);
            }
        });
    }

    public static void showListe(ListView<String> listeAfficheUtilisateurs){
        System.out.println("Updating...");
        System.out.println("The List : ");
        for(int i = 0 ; i < listeUtilisateurs.length ; i++){
            System.out.println(listeUtilisateurs[i]);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                check_liste = 1;
                listeAfficheUtilisateurs.getItems().setAll(listeUtilisateurs);
                listeAfficheUtilisateurs.getItems();
                check_liste = 0;
            }
        });
    }

    public void updateGroupsOnAction(){
        demandeGroupeListe();
        discussionPane.setVisible(false);
        //listeAfficheUtilisateurs.setVisible(false);
        creerUnGroupe.setVisible(true);

        System.out.println("Updating...");
        System.out.println("The List of Groups: ");
        int check = 0;
        for(int i = 0 ; i < listeGroups.length ; i++){
            if(listeGroups[i] == null){
                check = 1;
                break;
            }
            System.out.println(listeGroups[i]);
        }
        if(check == 0){
            listeAfficheUtilisateurs.setVisible(true);
            listeAfficheUtilisateurs.getItems().setAll(listeGroups);
            listeAfficheUtilisateurs.getItems();
            listeAfficheUtilisateurs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    destination_utilisateur = listeAfficheUtilisateurs.getSelectionModel().getSelectedItem();

                    String selectedGroupe = listeAfficheUtilisateurs.getSelectionModel().getSelectedItem();
                    discussionLabel.setText(selectedGroupe);
                    discussionPane.setVisible(true);
                    messageTextArea.setVisible(true);
                    messageInput.setVisible(true);
                    sendMessageButton.setVisible(true);
                    if(groupeName != null){
                        groupeName.setVisible(false);
                    }
                    if(listeUtilisateursPourChoisir != null){
                        listeUtilisateursPourChoisir.setVisible(false);
                    }
                    if(createTheGroup != null){
                        createTheGroup.setVisible(false);
                    }


                }
            });
        }
        else{
            listeAfficheUtilisateurs.setVisible(false);
        }
    }

    public void createGroupOnAction(){
        discussionPane.setVisible(true);
        messageTextArea.setVisible(false);
        messageInput.setVisible(false);
        sendMessageButton.setVisible(false);

        groupeName.setPromptText("Nom du Groupe");

        ObservableList<String> items = FXCollections.observableArrayList(listeUtilisateurs);
        listeUtilisateursPourChoisir = new ListView<>(items);
        ListView<String> selected = new ListView<>();

        listeUtilisateursPourChoisir.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listeUtilisateursPourChoisir.getSelectionModel().selectedItemProperty().addListener((obs,ov,nv)->{
            selected.setItems(listeUtilisateursPourChoisir.getSelectionModel().getSelectedItems());
        });

        listeUtilisateursPourChoisir.setPrefWidth(150);

        createTheGroup.setStyle("-fx-color:rgb(255,255,255);"+" -fx-background-color:rgb(0,4,92);");
        createTheGroup.setMaxWidth(100);
        createTheGroup.setText("Creer le groupe");

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                if(groupeName.getText().isBlank() == false){
                    int count = 1;
                    String users = nom_utilisateur+"/";
                    ObservableList<String> list = selected.getItems();
                    for (String item : list)
                    {
                        System.out.println("Selected : ");
                        System.out.println(list);
                        System.out.println(item);
                        count++;
                        users+=item+"/";
                    }
                    demandeCreationGroupe(groupeName.getText(),count,users);
                    groupeName.clear();
                }
            }
        };

        createTheGroup.setOnAction(event);

        groupeName.setVisible(true);
        listeUtilisateursPourChoisir.setVisible(true);
        createTheGroup.setVisible(true);

        HBox hbox = new HBox(groupeName,listeUtilisateursPourChoisir,createTheGroup);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(5,5,5,5));
        hbox.setSpacing(5.0);

        discussionPane.getChildren().add(hbox);
        listeAfficheUtilisateurs.setVisible(true);

    }

    public static void showGroupeListe(ListView<String> listeAfficheUtilisateurs){
        System.out.println("Updating...");
        System.out.println("The List : ");
        for(int i = 0 ; i < listeGroups.length ; i++){
            System.out.println(listeGroups[i]);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                listeAfficheUtilisateurs.getItems().setAll(listeGroups);
                listeAfficheUtilisateurs.getItems();
            }
        });
    }

}