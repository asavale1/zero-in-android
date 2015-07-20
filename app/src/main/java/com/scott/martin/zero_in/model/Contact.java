package com.scott.martin.zero_in.model;

import java.util.ArrayList;

/**
 * Created by ameya on 4/27/15.
 */
public class Contact {
    private String name;
    private String phone;
    private ArrayList<String> types;


    public Contact(){
        types = new ArrayList<>();
    }


    public void setName(String name){ this.name = name; }
    public String getName(){ return this.name; }

    public void setPhone(String phone){ this.phone = phone; }
    public String getPhone(){ return this.phone; }

    public void addType(String type){
        types.add(type);
    }
    public void setTypes(ArrayList<String> types){ this.types = types; }
    public ArrayList<String> getTypes(){ return this.types; }


    public void printVals(){
        System.out.println("Name: " + name);
        System.out.println("Phone: " + phone);
        for(int i = 0; i < types.size(); i++){
            System.out.println("Type: " + types.get(i));
        }
    }
}
