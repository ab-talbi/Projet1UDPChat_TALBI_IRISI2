package serveur;

import java.util.ArrayList;

public class Groups {
    public String name;
    public ArrayList<String> users;

    public Groups(String name, ArrayList<String> group) {
        this.name = name;
        this.users = group;
    }

    public void afficherGroupes(){
        System.out.println("------------- "+this.name+" --------------");
        for (int i = 0 ; i < users.size() ; i++){
            System.out.println("==> "+users.get(i));
        }
    }
}




