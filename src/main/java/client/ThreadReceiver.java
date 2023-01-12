package client;

import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThreadReceiver extends Client implements Runnable{

    public String monNomUtilisateur;
    public VBox vBox;
    public ListView<String> listeAfficheUtilisateurs;
    public ThreadReceiver(String monNomUtilisateur,VBox vBox, ListView<String> listeAfficheUtilisateurs) throws SocketException {
        super();
        this.monNomUtilisateur = monNomUtilisateur;
        this.vBox = vBox;
        this.listeAfficheUtilisateurs = listeAfficheUtilisateurs;
    }

    public void run(){
        System.out.println("I am a thread, waiting for new packets");
        while(true){
            System.out.println("Listening...");
            byte[] tampon = new byte[BUFSIZE];
            String msg = "";
            try{
                DatagramPacket packetIn = new DatagramPacket(tampon, tampon.length);
                dataSockClient.receive(packetIn);
                System.out.println("Now i received a packet");
                msg = new String(packetIn.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] splitMessage = msg.split("[/]");

            if(splitMessage[0].equals("liste")){
                updateUtilisateursListe(splitMessage);
            }else if(splitMessage[0].equals("envoiMessage")){
                messageRecuTraitement(splitMessage);
            }else if(splitMessage[0].equals("liste_groupes")){
                updateGroupsListe(splitMessage);
            }else if(splitMessage[0].equals("messageGroupe")){
                //receptGroupMessageFromUser(msg);
            }
        }
    }

    public void updateUtilisateursListe(String[] splitMessage){
        System.out.println("Changement de la liste...");
        int nbr = 0;
        nbr = Integer.valueOf(splitMessage[1]);
        String[] listUtilisateursToShow = new String[nbr];
        int index = 0 ;
        for(int j = 2; j <= 1+nbr;j++ ){
            if(!splitMessage[j].equals(monNomUtilisateur)){
                listUtilisateursToShow[index] = splitMessage[j];
                index++;
            }
        }
        //Filter la liste pour eleiminer null
        String[] listUtilisateursFiltered = Stream.of(listUtilisateursToShow).filter(str -> str != null)
                .collect(Collectors.toSet()).toArray(new String[0]);

        listeUtilisateurs = listUtilisateursFiltered;
        System.out.println("This is the list i sent to u : ");
        for(int i = 0 ; i < listeUtilisateurs.length ; i++){
            System.out.println(listeUtilisateurs[i]);
        }
        ClientChatController.showListe(listeAfficheUtilisateurs);
    }


    public void messageRecuTraitement(String[] splitMessage){
        System.out.println("Message arrivee...");
        String username = splitMessage[1];
        String message = splitMessage[2];
        System.out.println(username + " : " + message);

        ClientChatController.showMessage(splitMessage[2],vBox);
    }


    public void updateGroupsListe(String[] splitMessage) {
        System.out.println("Changement de la liste des groupes...");
        int nombreGroupes = Integer.valueOf(splitMessage[1]);
        String[] listGroupsToShow = new String[nombreGroupes];
        int index = 0 ;
        for(int j = 2; j <= 1+nombreGroupes;j++ ){
            listGroupsToShow[index] = splitMessage[j];
            index++;
        }
        //Filter la liste pour eleiminer null
        String[] listGroupesFiltered = Stream.of(listGroupsToShow).filter(str -> str != null)
                .collect(Collectors.toSet()).toArray(new String[0]);

        listeGroups = listGroupesFiltered;
        System.out.println("This is the list of groupes arrived : ");
        for(int i = 0 ; i < listGroupesFiltered.length ; i++){
            System.out.println(listGroupesFiltered[i]);
        }

        ClientChatController.showGroupeListe(listeAfficheUtilisateurs);
    }
}
