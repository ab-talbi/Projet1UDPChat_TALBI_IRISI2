package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientRegisterController extends Client implements Initializable {

    @FXML
    private Pane registerPane;
    @FXML
    private Button annulerButton;
    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField usernameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private TextField nomInput;
    @FXML
    private TextField prenomInput;
    @FXML
    private Label errorMessageRegister;

    public ClientRegisterController() throws SocketException {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File logoFile = new File("Images/logo.png");
        Image logoImage = new Image(logoFile.toURI().toString());
        logoImageView.setImage(logoImage);
    }

    public void annulerButtonOnAction() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    public void registerButtonOnAction() throws IOException {
        if(usernameInput.getText().isBlank() == true || passwordInput.getText().isBlank() == true || nomInput.getText().isBlank() == true || prenomInput.getText().isBlank() == true){
            errorMessageRegister.setText("Remplir les champs d'abord");
        }else{
            demandeCreationCompte(nomInput.getText(),prenomInput.getText(), usernameInput.getText(),passwordInput.getText());
            String response = returnMessageOfRegistrationOrLoginFromSever();

            String[] splitMessage = response.split("[/]");
            String erreurOrSuccess = "";
            String message = "";
            for(int i = 0 ; i < splitMessage.length ; i++){
                if(i==0){
                    erreurOrSuccess = splitMessage[i];
                }else if(i==1){
                    message = splitMessage[i];
                }
            }
            if(erreurOrSuccess.equals("erreur")){
                errorMessageRegister.setText(message);
            }else{
                //Go To Login Window!!-------------------------------
                versLoginButtonOnAction();
            }
        }
    }

    public void versLoginButtonOnAction() throws IOException {
        BorderPane loginView = FXMLLoader.load(getClass().getResource("login.fxml"));
        registerPane.getChildren().setAll(loginView);
    }

}
