package serveur;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;


public class Reception {
    private final int port;
    private static ArrayList<Session> session = new ArrayList<Session>();
    private static ArrayList<Groups> groups = new ArrayList<Groups>();

    private DatagramSocket dataSockDest;
    public static final int BUFSIZE = 1024;

    static final String DB_URL = "jdbc:mysql://localhost:3310/TestUDP";
    static final String USER = "root";
    static final String PASS = "";

    public Reception(int port) throws SocketException {
        super();
        this.port = port;
        dataSockDest = new DatagramSocket(this.port);
    }

    public void listening() {
        byte[] tampon = new byte[BUFSIZE];
        try{
            DatagramPacket packetIn = new DatagramPacket(tampon, tampon.length);
            dataSockDest.receive(packetIn);
            new Thread(new ClientHandler(packetIn)).start(); // a new Thread will deal with the packet received
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void createAccount(String msg,int portClient,InetAddress addrClient) {
        byte[] tampon;
        String[] splitMessage = msg.split("[/]");
        String nom = splitMessage[1];
        String prenom = splitMessage[2];
        String username = splitMessage[3];
        String password = splitMessage[4];
        System.out.println("Username to add to db is : "+nom+" "+prenom+" : "+username +", and his password : "+password);
        try{
            // Check if the username is unique
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();

            String selectQuery = "Select * from registration where username= ?";
            PreparedStatement st = conn.prepareStatement(selectQuery);
            st.setString(1,username);

            ResultSet rs = st.executeQuery();
            boolean exists = false;
            while (rs.next()) {
                exists = true;
            }

            //Pour envoyer un message à l'utilisateur, soit erreur ou success
            if(exists){
                String erreur = "register/erreur/L'utilisateur existe déja/";
                tampon = erreur.getBytes();
                DatagramSocket dataSockClient = new DatagramSocket();
                DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
                dataSockClient.send(packetOut);
                System.out.println("the error message is sent to client");
                dataSockClient.close();
            }else{
                String insertQuery = "INSERT INTO registration VALUES ('"+username+"','"+nom+"','"+prenom+"','"+password+"')";
                stmt.executeUpdate(insertQuery);

                String success = "register/success/Le compte est creé/";
                tampon = success.getBytes();
                DatagramSocket dataSockClient = new DatagramSocket();
                DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
                dataSockClient.send(packetOut);
                System.out.println("the success message is sent to client");
                dataSockClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void login(String msg,int portClient,InetAddress addrClient) {
        byte[] tampon;
        String[] splitMessage = msg.split("[/]");
        boolean exists = false;
        String username = splitMessage[1];
        String password = splitMessage[2];
        String passwordFromDataBase = "";
        try{
            for(int i = 0 ; i < session.size(); i++){
                if(session.get(i).port == portClient || session.get(i).username.equals(username)){
                    //Un utilisateur peux pas etre connecte deux foix à la meme
                    exists = true;
                    String erreur = "login/erreur/Vous etes deja connecte!!/";
                    tampon = erreur.getBytes();
                    DatagramSocket dataSockClient = new DatagramSocket();
                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
                    dataSockClient.send(packetOut);
                    System.out.println("the error message is sent to client");
                    dataSockClient.close();
                }
            }

            if(!exists){

                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                String strSelect = "Select * from registration where username= ?";

                PreparedStatement st = conn.prepareStatement(strSelect);
                st.setString(1,username);

                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    passwordFromDataBase = rs.getString(4);
                }
                if(password.equals(passwordFromDataBase)){

                    session.add(new Session(portClient,addrClient,username));
                    System.out.println("=====Les utilisateurs connectes=====");
                    for (int i = 0 ; i<session.size();i++){
                        System.out.println(session.get(i).toString());
                    }
                    System.out.println("====================================");

                    String success = "login/success/Vous etes connecté/";
                    tampon = success.getBytes();
                    DatagramSocket dataSockClient = new DatagramSocket();
                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
                    dataSockClient.send(packetOut);
                    System.out.println("the success message is sent to client");
                    dataSockClient.close();

                    for(int i = 0 ; i < session.size() ; i++){
                        if(!session.get(i).username.equals(username))
                        envoiListe(session.get(i).port,session.get(i).adresse);
                    }

                }else{
                    String erreur = "login/erreur/Nom d'utilisateur ou mot de passe est incorrecte/";
                    tampon = erreur.getBytes();
                    DatagramSocket dataSockClient = new DatagramSocket();
                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
                    dataSockClient.send(packetOut);
                    System.out.println("the error message is sent to client");
                    dataSockClient.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void demandeUserName(int port,InetAddress addrClient) throws SocketException {
        String username = "username/";
        for(int i = 0 ; i < session.size(); i++){
            if(session.get(i).port == port){
                username += session.get(i).username + "/";
                byte[] tampon = username.getBytes();
                DatagramSocket dataSockClient = new DatagramSocket();
                DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, port);
                try {
                    dataSockClient.send(packetOut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataSockClient.close();
            }
        }
    }


    public static synchronized void envoiMessage(String msg,int portClient) {
        String[] splitMessage = msg.split("[/]");
        String destination = splitMessage[1];
        String message = splitMessage[2];

        //get the username of soource
        String username = "";
        for(int i = 0;i<session.size();i++ ){
            if(session.get(i).port == portClient){
                username = session.get(i).username;
            }
        }

        msg = "envoiMessage/"+username+"/"+message+"/";

        //Trouver le port et l'adresse de lutilisateur de destination pour l'envoyer le pcket
        int check = 0;
        for(int i = 0;i<session.size();i++ ){
            if(session.get(i).username.equals(destination)){
                InetAddress adresse = session.get(i).adresse;
                int port = session.get(i).port;
                byte[] tampon = msg.getBytes();
                try {
                    DatagramSocket dataSockClient = new DatagramSocket();
                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
                    dataSockClient.send(packetOut);
                    dataSockClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                check++;
            }
        }
        if (check==0){
            System.out.println("This username is not connected!!!!!");
        }
    }


    public static synchronized void envoiListe(int portClient,InetAddress addrClient) {
        String toSend = "liste/"+session.size()+"/";
        for(int i = 0;i<session.size();i++ ){
            toSend += session.get(i).username +"/";
        }
        byte[] tampon = toSend.getBytes();
        try{
            DatagramSocket dataSockClient = new DatagramSocket();
            DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addrClient, portClient);
            dataSockClient.send(packetOut);
            System.out.println("La liste des utilisateurs est envoyer à l'utilisateur dans le port : "+portClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void envoiGroupesListe(int portClient,InetAddress addrClient) {
        //Seulement les groupes dans lauelles l'utilisateur fait partie serant envoyee
        String toSend = "liste_groupes/";
        String groupeNames = "";
        int nombreGroupes = 0;
        try{
            //Boucle sur tous les utilisateurs
            for(int i = 0 ; i < session.size() ; i++){
                if(session.get(i).port== portClient){
                    for(int j = 0 ; j < groups.size();j++){
                        for(int k = 0;k<groups.get(j).users.size();k++){
                            if(groups.get(j).users.get(k).equals(session.get(i).username)){
                                // c'est a dire l'utilisateur est dans ce groupe
                                nombreGroupes++;
                                groupeNames += groups.get(j).name + "/";
                            }
                        }
                    }
                }
            }
            if(nombreGroupes != 0){
                System.out.println("envoi de la liste des groupe a port : "+portClient);
                toSend += nombreGroupes + "/" + groupeNames + "/";
                byte[] tampon = toSend.getBytes();
                int port = portClient;
                InetAddress addr = addrClient;
                DatagramSocket dataSockClient = new DatagramSocket();
                DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addr, port);
                dataSockClient.send(packetOut);
                dataSockClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void envoiCreateGroup(String msg) {
        //1 - Creation du groupe
        String[] splitMessage = msg.split("[/]");
        String GroupeName = splitMessage[1];
        int nombreUtilisateurs = Integer.valueOf(splitMessage[2]);

        ArrayList<String> group = new ArrayList<String>();
        for(int i = 3; i <= 2+nombreUtilisateurs;i++ ){
            group.add(splitMessage[i]);
        }

        groups.add(new Groups(GroupeName,group));

        // Group est crreee

        try{
            //Boucle sur tous les utilisateurs
            for(int i = 0 ; i < session.size() ; i++){
                String toSend = "liste_groupes/";
                String groupeNames = "";
                int nombreGroupes = 0;
                //Boucle sur tous les groupes pour choisir les groupes de cet utilisateur
                for(int j = 0 ; j < groups.size();j++){
                    System.out.println("This is the name of the group : "+groups.get(j).name);
                    for(int k = 0;k<groups.get(j).users.size();k++){
                        if(groups.get(j).users.get(k).equals(session.get(i).username)){
                            // c'est a dire l'utilisateur est dans ce groupe
                            nombreGroupes++;
                            groupeNames += groups.get(j).name + "/";
                        }
                    }
                }
                //Un ou plusieurs groupes sont trouvee pour cet utilisateur
                if(nombreGroupes != 0){
                    System.out.println("envoi liste des groupes a "+session.get(i).username);
                    toSend += nombreGroupes + "/" + groupeNames + "/";
                    byte[] tampon = toSend.getBytes();
                    int port = session.get(i).port;
                    InetAddress addr = session.get(i).adresse;
                    DatagramSocket dataSockClient = new DatagramSocket();
                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, addr, port);
                    dataSockClient.send(packetOut);
                    dataSockClient.close();
                }
            }

            //Afficher La liste des groupes cote serveur
            for(int i = 0 ; i < groups.size() ; i++){
                groups.get(i).afficherGroupes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void decconnectUser(int port) {
        for(int i = 0;i<session.size();i++ ){
            if(session.get(i).port == port){
                session.remove(i);
            }
        }
        System.out.println("Les utilisateurs connectes maintenet");
        for (int i = 0 ; i<session.size();i++){
            System.out.println(session.get(i).toString());
        }
        //Envoi la liste des utilisateurs apres modification à tous les autres utilisateurs
        for(int i = 0 ; i < session.size() ; i++){
            envoiListe(session.get(i).port,session.get(i).adresse);
        }
    }


    public static synchronized void envoiMessageGroupe(String msg, int portClient) {
        String[] splitMessage = msg.split("[/]");
        String userMessage = splitMessage[1];
        String groupName = splitMessage[2];
        String username = "";
        for (int i = 0; i < session.size(); i++) {
            if (session.get(i).port == portClient) {
                username = session.get(i).username;
            }
        }
        String message = "messageGroupe/" + groupName + "/" +username + "/" + userMessage+"/";
        byte[] tampon = message.getBytes();
        int port = 0;
        try {
            //On va voir tous les groupes pour trouver le groupe concernee par le message
            for (int i = 0; i < groups.size(); i++) {
                //maintnenet nous sommes dans le groupe
                if(groups.get(i).name.equals(groupName)){
                    //On va boucler sur les utilisateurs du groupe, et les envoyer le message
                    for(int j = 0; j < groups.get(i).users.size() ; j++){
                        //On va envoyer le message au autres utilisateur est pas l'utilisateur qui la envoyee
                        if(!groups.get(i).users.get(j).equals(username)){
                            for (int k = 0; k < session.size(); k++) {
                                if (session.get(k).username.equals(groups.get(i).users.get(j))) {
                                    port = session.get(k).port;
                                    InetAddress adresse = session.get(k).adresse;
                                    DatagramSocket dataSockClient = new DatagramSocket();
                                    DatagramPacket packetOut = new DatagramPacket(tampon, tampon.length, adresse, port);
                                    dataSockClient.send(packetOut);
                                    dataSockClient.close();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // à modifier...
    public static synchronized void supprimerGroupe() {

    }
}
