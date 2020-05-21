package com.example.nidar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignedIn extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button btnSaveDetails;
    TextInputEditText contactPhoneNumber1, contactPhoneNumber2, contactPhoneNumber3, contactPhoneNumber4;
    String myPhoneNumber, contactNumber1, contactNumber2, contactNumber3, contactNumber4;
    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);

        findViews();
        mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference("contacts");
        pref = getSharedPreferences("NIDAR", MODE_PRIVATE);
        editor = pref.edit();

        // Fetch user's phone number
        myPhoneNumber = pref.getString("phoneNumber", null);

        fetchOldData();

        btnSaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAllFields()) {
                    addContacts();
                    Toast.makeText(SignedIn.this,"Details Saved", Toast.LENGTH_SHORT).show();

                    editor.putBoolean("isDetailsSaved", true);
                    editor.commit();
                    startActivity(new Intent(SignedIn.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        overridePendingTransition(0, 0);
        startActivity(intent);
        finish();

    }

    // Initialise Different View Items
    private void findViews() {
        contactPhoneNumber1 = findViewById(R.id.et_contact_phone_number1);
        contactPhoneNumber2 = findViewById(R.id.et_contact_phone_number2);
        contactPhoneNumber3 = findViewById(R.id.et_contact_phone_number3);
        contactPhoneNumber4 = findViewById(R.id.et_contact_phone_number4);
        btnSaveDetails = findViewById(R.id.btn_save_details);
    }

    // Fetch Old Saved Contacts
    private void fetchOldData() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(myPhoneNumber);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())) {
                    String contact1 = dataSnapshot.child("1").getValue().toString();
                    if (contact1.matches("[0-9]{10}"))
                        contactPhoneNumber1.setText(contact1);
                    String contact2 = dataSnapshot.child("2").getValue().toString();
                    if (contact2.matches("[0-9]{10}"))
                        contactPhoneNumber2.setText(contact2);
                    String contact3 = dataSnapshot.child("3").getValue().toString();
                    if (contact3.matches("[0-9]{10}"))
                        contactPhoneNumber3.setText(contact3);
                    String contact4 = dataSnapshot.child("4").getValue().toString();
                    if (contact4.matches("[0-9]{10}"))
                        contactPhoneNumber4.setText(contact4);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignedIn.this, "DATABASE ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Write Contact Numbers in Firebase Realtime Database
    private void addContacts() {
        databaseReference.child(myPhoneNumber).child("1").setValue(contactNumber1);
        databaseReference.child(myPhoneNumber).child("2").setValue(contactNumber2);
        databaseReference.child(myPhoneNumber).child("3").setValue(contactNumber3);
        databaseReference.child(myPhoneNumber).child("4").setValue(contactNumber4);
    }

    // Checks whether all Contact Numbers are valid or not
    private boolean checkAllFields() {
        contactNumber1 = contactPhoneNumber1.getText().toString();
        contactNumber2 = contactPhoneNumber2.getText().toString();
        contactNumber3 = contactPhoneNumber3.getText().toString();
        contactNumber4 = contactPhoneNumber4.getText().toString();

        // Status used to state if all Contact Numbers are valid then only allow saving of details
        boolean status = true;

        // Adding First Contact is mandatory
        if ( TextUtils.isEmpty(contactPhoneNumber1.getText()) ) {
            contactPhoneNumber1.setError( "First Contact number is required!" );
            status = false;
        }
        else {
            String MobilePattern = "[4-9][0-9]{9}";
            // Check Contact Number 1
            if ( contactNumber1.matches(MobilePattern) ) {
                if ( contactNumber1.equals(myPhoneNumber) ) {
                    contactPhoneNumber1.setError( "Contact Number 1 should be different from Your Number" );
                    status = false;
                }
            }
            else {
                contactPhoneNumber1.setError( "Enter valid Contact Phone Number 1" );
                status = false;
            }


            // Check Contact Number 2
            if ( contactNumber2.matches(MobilePattern) ) {
                if ( contactNumber2.equals(myPhoneNumber) ) {
                    contactPhoneNumber2.setError( "Contact Number 2 should be different from Your Number" );
                    status = false;
                }
                // Check if this Phone Number matches other Contact Numbers
                else if ( contactNumber2.matches(contactNumber1 + "|" + contactNumber3 + "|" + contactNumber4) ) {
                    contactPhoneNumber2.setError( "Two Contacts are same" );
                    status = false;
                }
            }
            else {
                // Check whether this Phone Number should be empty or valid
                if ( ! TextUtils.isEmpty(contactPhoneNumber2.getText()) ) {
                    contactPhoneNumber2.setError( "Either Leave it Blank or Enter valid Contact number!" );
                    status = false;
                }
            }


            // Check Contact Number 3
            if ( contactNumber3.matches(MobilePattern) ) {
                if ( contactNumber3.equals(myPhoneNumber) ) {
                    contactPhoneNumber3.setError( "Contact Number 3 should be different from Your Number");
                    status = false;
                }
                // Check if this Phone Number matches other Contact Numbers
                else if ( contactNumber3.matches(contactNumber1 + "|" + contactNumber2 + "|" + contactNumber4) ) {
                    contactPhoneNumber3.setError( "Two Contacts are same" );
                    status = false;
                }
            }
            else {
                // Check whether this Phone Number should be empty or valid
                if ( ! TextUtils.isEmpty(contactPhoneNumber3.getText()) ) {
                    contactPhoneNumber3.setError( "Either Leave it Blank or Enter valid Contact number!" );
                    status = false;
                }
            }


            // Check Contact Number 4
            if ( contactNumber4.matches(MobilePattern) ) {
                if ( contactNumber4.equals(myPhoneNumber) ) {
                    contactPhoneNumber4.setError( "Contact Number 4 should be different from Your Number");
                    status = false;
                }
                // Check if this Phone Number matches other Contact Numbers
                else if ( contactNumber4.matches(contactNumber1 + "|" + contactNumber2 + "|" + contactNumber3) ) {
                    contactPhoneNumber4.setError( "Two Contacts are same" );
                    status = false;
                }
            }
            else {
                // Check whether this Phone Number should be empty or valid
                if ( ! TextUtils.isEmpty(contactPhoneNumber4.getText()) ) {
                    contactPhoneNumber4.setError( "Either Leave it Blank or Enter valid Contact number!" );
                    status = false;
                }
            }
        }
        return status;
    }
}