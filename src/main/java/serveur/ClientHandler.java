package serveur;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientHandler implements Runnable{
    private DatagramPacket datagramPacket;
    private int port;
    private InetAddress address;

    public ClientHandler(DatagramPacket packetIn) throws SocketException {
        this.datagramPacket = packetIn;
        this.port = this.datagramPacket.getPort();
        this.address = this.datagramPacket.getAddress();
    }

    public void run(){
        String msg = new String(this.datagramPacket.getData()); //On va traite le packet
        String[] msgSplit = msg.split("[/]");
        String arg = msgSplit[0];

        if(arg.equals("register")){
            Reception.createAccount(msg,this.port,this.address);
        }else if(arg.equals("login")){
            Reception.login(msg,this.port,this.address);
        }else if(arg.equals("usernameDemande")){
            try {
                Reception.demandeUserName(this.port,this.address);
            } catch (SocketException e) {}
        }else if(arg.equals("envoiMessage")){
            Reception.envoiMessage(msg,this.port);
        }else if(arg.equals("liste")){
            Reception.envoiListe(this.port,this.address);
        }else if(arg.equals("liste_groupes")){
            Reception.envoiGroupesListe(this.port,this.address);
        }else if(arg.equals("group_creation")){
            Reception.envoiCreateGroup(msg);
        }else if(arg.equals("messageGroupe")){
            Reception.envoiMessageGroupe(msg,this.port);
        }else if(arg.equals("supprimerGroupe")){
            Reception.supprimerGroupe();
        }else if(arg.equals("decconnectUser")){
            Reception.decconnectUser(this.port);
        }
    }
}
