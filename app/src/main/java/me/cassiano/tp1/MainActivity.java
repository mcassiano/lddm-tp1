package me.cassiano.tp1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity {

    private static final int CONTACT_PICKER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent contactPicker = new Intent(Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(contactPicker, CONTACT_PICKER_REQUEST_CODE);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == CONTACT_PICKER_REQUEST_CODE) {
                Uri result = data.getData();
                processContact(result);

            }
        }

        else {
            String msg = getString(R.string.msg_no_contact_selected);
            showToast(msg);
        }
    }

    private void processContact(Uri contactUri) {

        String contactName;
        String hasPhoneNumber;
        Cursor c = getContentResolver().query(contactUri, null, null, null, null);

        if (c.moveToFirst()) {
            contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            hasPhoneNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            updateContactNameLabel(contactName);
            updatePhoneNumber(contactUri, hasPhoneNumber);
            updateEmail(contactUri);
            updateAddress(contactUri);
        }

        c.close();

    }

    private void showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    private void updateContactNameLabel(String name) {

        TextView cName = (TextView) findViewById(R.id.contactName);
        cName.setText(name);

    }

    private void updatePhoneNumber(Uri contactUri, String hasPhoneNumber) {

        Button callButton = (Button) findViewById(R.id.callButton);

        if (!hasPhoneNumber.equals("1")) {
            callButton.setText(getString(R.string.msg_no_phone_number));
            callButton.setEnabled(false);
        }

        else {

            final String phone = getDataForContactId(contactUri.getLastPathSegment(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

            callButton.setText(phone);
            callButton.setEnabled(true);

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            });

        }

        callButton.setVisibility(View.VISIBLE);

    }

    private void updateEmail(Uri contactUri) {

        Button emailButton = (Button) findViewById(R.id.emailButton);

        final String email = getDataForContactId(contactUri.getLastPathSegment(),
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID);

        if (email == null) {
            emailButton.setText(getString(R.string.msg_no_email_address));
            emailButton.setEnabled(false);
        }

        else {
            emailButton.setText(email);
            emailButton.setEnabled(true);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", email, null));

                    startActivity(Intent.createChooser(emailIntent,
                            getString(R.string.msg_select_email_client)));
                }
            });
        }

        emailButton.setVisibility(View.VISIBLE);


    }

    private void updateAddress(Uri contactUri) {

        final String address = getDataForContactId(contactUri.getLastPathSegment(),
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID);

        TextView addressTV = (TextView) findViewById(R.id.address);
        Button directionsButton = (Button) findViewById(R.id.directions);

        if (address == null) {
            addressTV.setText(getString(R.string.msg_address_not_found));
            directionsButton.setEnabled(false);
        }

        else {

            addressTV.setText(address);

            directionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url;

                    try {
                        url = "http://maps.google.com/maps?q=" +
                                URLEncoder.encode(address, "utf-8");

                        Intent maps = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                        startActivity(maps);

                    } catch (UnsupportedEncodingException e) {
                        showToast(getString(R.string.msg_failed_opening_map));
                    }
                }
            });

            directionsButton.setEnabled(true);
        }

        addressTV.setVisibility(View.VISIBLE);
        directionsButton.setVisibility(View.VISIBLE);


    }

    private String getDataForContactId(String contactId, Uri contentUri,
                                     String columnName, String selection) {

        Cursor c = getContentResolver().query(
                contentUri,
                new String[] {columnName},
                selection + " = ?",
                new String[] {contactId},
                null
        );

        String data = null;

        if (c.moveToFirst())
            data = c.getString(c.getColumnIndex(columnName));

        c.close();

        return data;
    }
}
