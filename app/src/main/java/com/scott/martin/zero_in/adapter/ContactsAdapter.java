package com.scott.martin.zero_in.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.scott.martin.zero_in.R;
import com.scott.martin.zero_in.model.Contact;

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

        return contactsRow;
    }
}
