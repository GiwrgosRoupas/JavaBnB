package it21988.User;

import it21988.House;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static it21988.User.User.usersMap;

public class Owner extends User{

    public static Map<Integer, ArrayList<House>> housesOwned=new HashMap<>();
    public static boolean ownerExists(int taxNumber){
        if (userExists(taxNumber))
            return !usersMap.get(taxNumber)[2].equals("-");
        return false;
    }

    public static void createOwner(int taxNumber){
        usersMap.put(taxNumber, new String[]{User.inputName(), User.inputIdNum(), User.inputAddress(), null});
        System.out.println("Account created with Owner access.");
    }

    public static void createOwnerRenterExists(int taxNumber){
        String[] temp=usersMap.get(taxNumber);
        temp[2]=inputAddress();
        usersMap.put(taxNumber, temp);
        System.out.println("Account upgraded to full access");
    }

}
