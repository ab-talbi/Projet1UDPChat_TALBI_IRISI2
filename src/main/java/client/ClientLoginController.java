package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientLoginController extends Client implements Initializable {

    @FXML
    private Pane loginPane;
    @FXML
    private Button annulerButton;
    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField usernameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Label errorMessageLogin;

    public ClientLoginController() throws SocketException {}


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File logoFile = new File("Images/logo.png");
        Image logoImage = new Image(logoFile.toURI().toString());
        logoImageView.setImage(logoImage);

        //ENTRER BUTTON Login
        passwordInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    loginButtonOnAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void annulerButtonOnAction() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    public void loginButtonOnAction() throws IOException {
        if(usernameInput.getText().isBlank() == true || passwordInput.getText().isBlank() == true){
            errorMessageLogin.setText("Remplir les champs d'abord");
        }else{
            envoiLogin(usernameInput.getText(),passwordInput.getText());
            String response = returnMessageOfRegistrationOrLoginFromSever(); //La reponse de login de serveur

            String[] splitMessage = response.split("[/]");
            if(splitMessage[0].equals("")){
                errorMessageLogin.setText("Erreur, vous avez recoit un packet aleatoire de serveur");
            }else{
                String erreurOrSuccess = splitMessage[0];
                String message = splitMessage[1];
                if(erreurOrSuccess.equals("erreur")){
                    errorMessageLogin.setText(message);
                }else{
                    Stage stage = (Stage) annulerButton.getScene().getWindow();
                    stage.close();

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chat.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), 650, 520);
                    Stage newStage = new Stage();
                    newStage.setTitle("Chat");
                    //newStage.initStyle(StageStyle.UNDECORATED);
                    newStage.setScene(scene);
                    newStage.show();
                }
            }
        }
    }

    public void versRegisterButtonOnAction() throws IOException {
        AnchorPane registerView = FXMLLoader.load(getClass().getResource("register.fxml"));
        loginPane.getChildren().setAll(registerView);
    }

}