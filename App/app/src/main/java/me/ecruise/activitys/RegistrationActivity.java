package me.ecruise.activitys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import me.ecruise.data.Customer;

public class RegistrationActivity extends AppCompatActivity {

    private EditText mNameText;
    private EditText mLastnameText;
    private EditText mEmailText;
    private EditText mEmailText2;
    private EditText mPasswordText;
    private EditText mPasswordText2;
    private EditText mPhoneNumber;
    private EditText mCountry;
    private EditText mCity;
    private EditText mZipCode;
    private EditText mStreet;
    private EditText mHousenumber;
    private EditText mExtraAddressline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameText = (EditText) findViewById(R.id.nameText);
        mLastnameText = (EditText) findViewById(R.id.lastnameText);
        mEmailText = (EditText) findViewById(R.id.emailText);
        mEmailText2 = (EditText) findViewById(R.id.emailText2);
        mPasswordText = (EditText) findViewById(R.id.passwordText);
        mPasswordText2 = (EditText) findViewById(R.id.passwordText2);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumberText);
        mCountry = (EditText) findViewById(R.id.countryText);
        mCity = (EditText) findViewById(R.id.cityText);
        mZipCode = (EditText) findViewById(R.id.zipCodeText);
        mStreet = (EditText) findViewById(R.id.streetText);
        mHousenumber = (EditText) findViewById(R.id.houseNumberText);
        mExtraAddressline = (EditText) findViewById(R.id.extraAddressLineText);

        Button mRegistrationButton = (Button) findViewById(R.id.registrationButton);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    public void confirm() {
        if (validateTextEdits()) {
            Customer.getInstance(this.getApplicationContext()).setName(mNameText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setLastname(mLastnameText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setEmail(mEmailText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setPassword(mPasswordText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setPhoneNumber(mPhoneNumber.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setCountry(mCountry.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setCity(mCity.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setZipCode(Integer.parseInt(mZipCode.getText().toString()));
            Customer.getInstance(this.getApplicationContext()).setStreet(mStreet.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setHouseNumber(mHousenumber.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setExtraAddressLine(mExtraAddressline.getText().toString());
            Customer.getInstance(this.getApplicationContext()).registerUser(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    successAlert();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure() {
                    failureAlert();
                }
            });

        }
    }

    private void successAlert() {
        Log.d("Alert", "Success");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Registrierung erfolgreich");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void failureAlert() {
        Log.d("Alert", "Failure");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Fehler bei der Registrierung");
        dlgAlert.setTitle("Hinweis");
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public boolean validateTextEdits() {
        if (mNameText.getText().toString().isEmpty()) {
            mNameText.setError("Pflichtfeld");
            mNameText.requestFocus();
            return false;
        }
        if (mLastnameText.getText().toString().isEmpty()) {
            mLastnameText.setError("Pflichtfeld");
            mLastnameText.requestFocus();
            return false;
        }
        if (mEmailText.getText().toString().isEmpty()) {
            mEmailText.setError("Pflichtfeld");
            mEmailText.requestFocus();
            return false;
        }
        if (!mEmailText.getText().toString().contains("@")) {
            mEmailText.setError("Keine gültige Email-Adresse");
            mEmailText.requestFocus();
            return false;
        }
        if (!mEmailText2.getText().toString().equals(mEmailText.getText().toString())) {
            mEmailText2.setError("Stimmt nicht überein");
            mEmailText2.requestFocus();
            return false;
        }
        if (mPasswordText.getText().toString().isEmpty()) {
            mPasswordText.setError("Pflichtfeld");
            mPasswordText.requestFocus();
            return false;
        }
        if (mPasswordText.getText().toString().length() < 4) {
            mPasswordText.setError("Muss mindestens 4 Zeichen lang sein");
            mPasswordText.requestFocus();
            return false;
        }
        if (!mPasswordText2.getText().toString().equals(mPasswordText.getText().toString())) {
            mPasswordText2.setError("Stimmt nicht überein");
            mPasswordText2.requestFocus();
            return false;
        }
        if (mPhoneNumber.getText().toString().isEmpty()) {
            mPhoneNumber.setError("Pflichtfeld");
            mPhoneNumber.requestFocus();
            return false;
        }
        if (mCountry.getText().toString().isEmpty()) {
            mCountry.setError("Pflichtfeld");
            mCountry.requestFocus();
            return false;
        }
        if (mCity.getText().toString().isEmpty()) {
            mCity.setError("Pflichtfeld");
            mCity.requestFocus();
            return false;
        }
        if (mZipCode.getText().toString().isEmpty()) {
            mZipCode.setError("Pflichtfeld");
            mZipCode.requestFocus();
            return false;
        }
        if (mStreet.getText().toString().isEmpty()) {
            mStreet.setError("Pflichtfeld");
            mStreet.requestFocus();
            return false;
        }
        if (mHousenumber.getText().toString().isEmpty()) {
            mHousenumber.setError("Pflichtfeld");
            mHousenumber.requestFocus();
            return false;
        }
        return true;
    }

}
