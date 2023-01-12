package serveur;

import java.net.InetAddress;

public class Session {
    public String username;
    public int port;
    public InetAddress adresse;

    public Session(int port, InetAddress adresse, String username){
        this.port = port;
        this.adresse = adresse;
        this.username = username;
    }

    public String toString(){
        return "Utilisateur : "+ this.username+" - Port : "+this.port +" - Adresse : "+ this.adresse;
    }
}
