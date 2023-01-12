package serveur;

import java.net.SocketException;

public class MainServeur {
    public static void main(String[] args) throws SocketException {

        int port = 1000;
        System.out.println("J'écoute sur "+port);
        Reception reception = new Reception(port);

        while(true){
            reception.listening();
        }
    }

}
