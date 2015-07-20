package com.scott.martin.zero_in.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.model.Contact;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by ameya on 4/27/15.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {
    private ArrayList<Contact> contacts;
    private Context context;

    public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, R.layout.row_contacts, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contactsRow = inflater.inflate(R.layout.row_contacts, parent, false);
        TextView contactName = (TextView) contactsRow.findViewById(R.id.contact_name);
        contactName.setText(contacts.get(position).getName());

        TextView contactPhone = (TextView) contactsRow.findViewById(R.id.contact_phone);
        contactPhone.setText(contacts.get(position).getPhone());

        ArrayList<String> types = contacts.get(position).getTypes();
        for(int i = 0; i < types.size(); i++){
            if(types.get(i).equals(context.getString(R.string.send_via_message))){
                contactsRow.findViewById(R.id.messaging).setVisibility(View.VISIBLE);
            }else if(types.get(i).equals(context.getString(R.string.send_via_whatsapp))){
                contactsRow.findViewById(R.id.whatsapp).setVisibility(View.VISIBLE);
            }
        }

        return contactsRow;
    }
}
