package com.scott.martin.zero_in.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.model.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ameya on 6/26/15.
 */
public class ContactsHelper {
    private HashMap<String, String> namePhoneMap;
    private HashMap<String, ArrayList<String>> nameTypeMap;
    private ContentResolver cr;
    private Context context;

    public ContactsHelper(Context context){
        this.context = context;
        cr = context.getContentResolver();
    }

    public ArrayList<Contact> getContacts(){
        namePhoneMap = new HashMap<>();
        nameTypeMap = new HashMap<>();

        mapPhoneContacts();
        mapWhatsAppContacts();

        ArrayList<Contact> contacts = new ArrayList<>();


        Set<String> keys = namePhoneMap.keySet();
        for(String key: keys){
            Contact contact = new Contact();
            contact.setName(key);
            contact.setPhone(namePhoneMap.get(key));
            contact.setTypes(nameTypeMap.get(key));
            contacts.add(contact);
        }

        Collections.sort(contacts, new ContactsComparator());
        return contacts;
    }

    public class ContactsComparator implements Comparator<Contact>{
        @Override
        public int compare(Contact c1, Contact c2){
            return c1.getName().compareToIgnoreCase(c2.getName());
        }
    }

    private void mapPhoneContacts(){
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                String id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0){
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        namePhoneMap.put(name, phoneNumber);

                        updateNameTypeMap(name, context.getString(R.string.send_via_message));
                        System.out.println("Name: " + name + "\tPhone: " + phoneNumber);
                        //break;
                    }
                    pCur.close();
                }
            }
        }
    }

    private void mapWhatsAppContacts(){
        Cursor c = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);

        ArrayList<String> myWhatsappContacts = new ArrayList<String>();
        int contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
        while (c.moveToNext())
        {
            // You can also read RawContacts.CONTACT_ID to read the
            // ContactsContract.Contacts table or any of the other related ones.
            //myWhatsappContacts.add(c.getString(contactNameColumn));
            //System.out.println(c.getString(contactNameColumn));
            updateNameTypeMap(c.getString(contactNameColumn), context.getString(R.string.send_via_whatsapp));
        }
    }

    private void updateNameTypeMap(String name, String type){

        System.out.println("TYpe: " + type);
        if(nameTypeMap.containsKey(name)){
            ArrayList<String> types = nameTypeMap.get(name);
            types.add(type);
            nameTypeMap.put(name, types);
        }else{
            ArrayList<String> types = new ArrayList<>();
            types.add(type);
            nameTypeMap.put(name, types);
        }

    }
}
