module client.projet1udpchat_talbi_irisi2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens client to javafx.fxml;
    exports client;
}