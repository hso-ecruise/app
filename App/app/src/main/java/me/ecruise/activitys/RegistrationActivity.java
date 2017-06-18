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

    /**
     * initializes the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameText = (EditText) findViewById(R.id.nameText);
        if (mNameText == null)
            Log.d("mNameText", "null");
        mLastnameText = (EditText) findViewById(R.id.lastnameText);
        if (mLastnameText == null)
            Log.d("mLastnameText", "null");
        mEmailText = (EditText) findViewById(R.id.emailText);
        if (mEmailText == null)
            Log.d("mEmailText", "null");
        mEmailText2 = (EditText) findViewById(R.id.emailText2);
        if (mEmailText2 == null)
            Log.d("mEmailText2", "null");
        mPasswordText = (EditText) findViewById(R.id.passwordText);
        if (mPasswordText == null)
            Log.d("mPasswordText", "null");
        mPasswordText2 = (EditText) findViewById(R.id.passwordText2);
        if (mPasswordText2 == null)
            Log.d("mPasswordText2", "null");
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumberText);
        if (mPhoneNumber == null)
            Log.d("mPhoneNumber", "null");
        mCountry = (EditText) findViewById(R.id.countryText);
        if (mCountry == null)
            Log.d("mCountry", "null");
        mCity = (EditText) findViewById(R.id.cityText);
        if (mCity == null)
            Log.d("mCity", "null");
        mZipCode = (EditText) findViewById(R.id.zipCodeText);
        if (mZipCode == null)
            Log.d("mZipCode", "null");
        mStreet = (EditText) findViewById(R.id.streetText);
        if (mStreet == null)
            Log.d("mStreet", "null");
        mHousenumber = (EditText) findViewById(R.id.housenumberText);
        if (mHousenumber == null)
            Log.d("mHousenumber", "null");
        mExtraAddressline = (EditText) findViewById(R.id.extraAddressLineText);
        if (mExtraAddressline == null)
            Log.d("mExtraAddressline", "null");



        Button mRegistrationButton = (Button) findViewById(R.id.registrationButton);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    /**
     * Creates a new Customer with the User-Input and posts it to the server
     */
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

    /**
     * Creates a positiv Info-Alert
     */
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

    /**
     * Creates an Error-Alert
     */
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

    /**
     * checks if the user-input is valid
     * also sets error-infos for the user
     * @return true if everything is ok
     */
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
