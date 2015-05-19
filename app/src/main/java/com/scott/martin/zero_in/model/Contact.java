package com.scott.martin.zero_in.model;

/**
 * Created by ameya on 4/27/15.
 */
public class Contact {
    private String name;
    private String phone;

    public Contact(){}
    public Contact(String name, String phone){
        this.name = name;
        this.phone = phone;
    }

    public void setName(String name){ this.name = name; }
    public String getName(){ return this.name; }

    public void setPhone(String phone){ this.phone = phone; }
    public String getPhone(){ return this.phone; }
}
