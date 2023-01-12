package client;

import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client {

    private static int number = 1; //pour que dataSocketClient soit instanciee une seule fois
    public static DatagramSocket dataSockClient;
    public static final int BUFSIZE = 1024;
    public static String[] listeUtilisateurs = new String[1000];
    public static String[] listeGroups = new String[1000];

    int port = 1000;
    String host = "127.0.0.1";

    public Client() throws SocketException {
        super();
        if(number == 1){
            dataSockClient = new DatagramSocket();
            number++;
        }
    }


    public void envoiLogin(String username, String password) {
        String msg = "login/"+username + "/" + password + "/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void demandeCreationCompte(String nom, String prenom, String username, String password) {
        String msg = "register/"+nom + "/" + prenom + "/"+username + "/" + password + "/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized String returnMessageOfRegistrationOrLoginFromSever() {
        byte[] tampon = new byte[BUFSIZE];
        String msg = "";
        try{
            DatagramPacket packetIn = new DatagramPacket(tampon, tampon.length);
            dataSockClient.receive(packetIn);
            msg = new String(packetIn.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] splitMessage = msg.split("[/]");
        if(splitMessage[0].equals("register") || splitMessage[0].equals("login")){
            String erreurOrSuccess = splitMessage[1];
            String message = splitMessage[2];
            System.out.println(erreurOrSuccess + " : " + message);

            return erreurOrSuccess + "/" + message;
        }else{
            return "";
        }

    }


    public void demandeUserName(){
        String msg = "usernameDemande/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getUserName(){
        byte[] tampon = new byte[BUFSIZE];
        String msg = "";
        try{
            DatagramPacket packetIn = new DatagramPacket(tampon, tampon.length);
            dataSockClient.receive(packetIn);
            msg = new String(packetIn.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] splitMessage = msg.split("[/]");
        if(splitMessage[0].equals("username")){
            String username = splitMessage[1];
            return username;
        }else{
            return "Anonyme";
        }
    }


    public void envoiMessage(String message, String destination) {
        String msg = "envoiMessage/"+ destination + "/" + message + "/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void decconnectUser() {
        String msg = "decconnectUser/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void demandeListe() {
        String msg = "liste/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static synchronized String[] receptListeInitial(String monNomUtilisateur) {
        byte[] tampon = new byte[BUFSIZE];
        String msg = "";

        try{
            DatagramPacket packetIn = new DatagramPacket(tampon, tampon.length);
            dataSockClient.receive(packetIn);
            msg = new String(packetIn.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] splitMessage = msg.split("[/]");
        int nbr = 0;
        if(splitMessage[0].equals("liste")){
            nbr = Integer.valueOf(splitMessage[1]);
            String[] listUtilisateurs = new String[nbr-1];
            for(int j = 2; j <= 1+nbr;j++ ){
                if(!splitMessage[j].equals(monNomUtilisateur)){
                    listUtilisateurs[j-2] = splitMessage[j];
                }
            }
            return listUtilisateurs;
        }else{
            return new String[0];
        }
    }


    public static synchronized void reception(String monNomUtilisateur, VBox vBox, ListView<String> listeAfficheUtilisateurs) throws SocketException {
        ThreadReceiver receiveSocket = new ThreadReceiver(monNomUtilisateur,vBox,listeAfficheUtilisateurs);
        new Thread(receiveSocket).start();
    }


    public void demandeCreationGroupe(String name,int nombreUtilisateur,String users) {
        String msg = "group_creation/"+name+"/"+nombreUtilisateur+"/"+users;
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void demandeGroupeListe() {
        String msg = "liste_groupes/";
        byte[] tampon = msg.getBytes();
        try {
            InetAddress adresse = InetAddress.getByName(host);
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
            dataSockClient.send(packetOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
