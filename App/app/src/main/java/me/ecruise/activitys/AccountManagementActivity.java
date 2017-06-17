package me.ecruise.activitys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;

import me.ecruise.data.Customer;

public class AccountManagementActivity extends AppCompatActivity{

    LinearLayout login;
    LinearLayout person;
    LinearLayout address;

    private EditText mNameText;
    private EditText mLastnameText;
    private EditText mEmailText;
    private EditText mEmailText2;
    private EditText mPhoneNumberText;
    private EditText mPasswordText;
    private EditText mPasswordText2;
    private EditText mStreetText;
    private EditText mHouseNumberText;
    private EditText mExtraAddressLineText;
    private EditText mZipCodeText;
    private EditText mCityText;
    private EditText mCountryText;

    public AccountManagementActivity(){
    }

    /**
     * Methon for initialisation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mNameText = (EditText) findViewById(R.id.nameText);
        mNameText.setKeyListener(null);
        mLastnameText = (EditText) findViewById(R.id.lastnameText);;
        mLastnameText.setKeyListener(null);
        mNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameText.setError("Zum Ändern des Namens kontaktieren sie den Support!");
            }
        });
        mLastnameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastnameText.setError("Zum Ändern des Namens kontaktieren sie den Support!");
            }
        });
        mEmailText = (EditText) findViewById(R.id.emailText);
        mEmailText2 = (EditText) findViewById(R.id.emailText2);
        mPhoneNumberText = (EditText) findViewById(R.id.phoneNumberText);
        mPasswordText = (EditText) findViewById(R.id.passwordText);
        mPasswordText2 = (EditText) findViewById(R.id.passwordText2);
        mStreetText = (EditText) findViewById(R.id.streetText);
        mHouseNumberText = (EditText) findViewById(R.id.houseNumberText);
        mExtraAddressLineText = (EditText) findViewById(R.id.extraAddressLineText);
        mZipCodeText = (EditText) findViewById(R.id.zipCodeText);
        mCityText = (EditText) findViewById(R.id.cityText);
        mCountryText = (EditText) findViewById(R.id.countryText);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button mConfirmButton = (Button) findViewById(R.id.confirmButton);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();
        login = (LinearLayout) findViewById(R.id.loginTab);
        person = (LinearLayout) findViewById(R.id.personTab);
        address = (LinearLayout) findViewById(R.id.addressTab);
        host.clearAllTabs();
        host.addTab(host.newTabSpec("Person").setIndicator("Person").setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String arg0) {
                return person;
            }
        }));
        host.addTab(host.newTabSpec("Login").setIndicator("Login").setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String arg0) {
                return login;
            }
        }));
        host.addTab(host.newTabSpec("Adresse").setIndicator("Adresse").setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String arg0) {
                return address;
            }
        }));


        if(mEmailText==null)
        {
            Log.d("Error", "email not found");
        }
        Log.d("SERVER", "get User Data");
        Customer.getInstance(this.getApplicationContext()).getUserDataFromServer(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    initializeTextEdits();
                }
                @Override
                public void onFailure() {

                }
        });
    }

    /**
     *  Called when the confirm button is pressed
     */
    private void confirm(){
        if (validateTextEdits())
        {
            final Customer newCustomerData = readUserData();
            Customer.getInstance(this.getApplicationContext()).getUserDataFromServer(new Customer.DataCallback() {
                @Override
                public void onSuccess() {
                    Log.d("PATCH", "Start");
                    patchUserData(newCustomerData);
                }
                @Override
                public void onFailure() {

                }
            });
        }
    }

    /**
     * checks if the user-input is valid
     * also sets error-infos for the user
     * @return true if everything is ok
     */
    public boolean validateTextEdits()
    {
        if(!(mEmailText.getText().toString().isEmpty() || mEmailText.getText().toString().equals(Customer.getInstance(this.getApplicationContext()).getEmail())))
        {
            if(!mEmailText.getText().toString().contains("@"))
            {
                mEmailText.setError("Keine gültige Email-Adresse");
                mEmailText.requestFocus();
                return false;
            }
            if(!mEmailText2.getText().toString().equals(mEmailText.getText().toString()))
            {
                mEmailText2.setError("Stimmt nicht überein");
                mEmailText2.requestFocus();
                return false;
            }
        }
        if(!mPasswordText.getText().toString().isEmpty())
        {
            if(mPasswordText.getText().toString().length()<4)
            {
                mPasswordText.setError("Muss mindestens 4 Zeichen lang sein");
                mPasswordText.requestFocus();
                return false;
            }
            if(!mPasswordText2.getText().toString().equals(mPasswordText.getText().toString()))
            {
                mPasswordText2.setError("Stimmt nicht überein");
                mPasswordText2.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * communicates with the Customer Object to update the data
     * @param newCustomerData
     */
    private void patchUserData(Customer newCustomerData) {
        Customer.getInstance(this.getApplicationContext()).updateUserData(newCustomerData, new Customer.DataAnswerCallback() {
            @Override
            public void onSuccess(String answer) {
                successAlert(answer);
            }
            @Override
            public void onFailure() {
                failureAlert();
            }
        });
    }

    /**
     * Creates a positiv Info-Alert
     * @param answer the Answer from the Servercall, contains the name of the changed field
     */
    private void successAlert(String answer)
    {
        Log.d("Alert", "Success");
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        String message = "Folgende Daten wurden erfolgreich geändert: ";
        if(answer.contains("address"))
            message = message + "Adresse\n";
        if(answer.contains("phone"))
            message = message + "Telefonnummer\n";
        if(answer.contains("email"))
            message = message + "Email\n";

        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        if(answer.contains("password"))
        {
            message = "Passwort wurde geändert, Sie werden ausgeloggt.\n Bitte mit dem neuen Passwort einloggen.";
            dlgAlert.setMessage(message);
            dlgAlert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss the dialog
                            logout();
                        }
                    });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
    }

    private void logout()
    {
        SharedPreferences savedLogin = PreferenceManager.getDefaultSharedPreferences((this.getApplicationContext()));
        SharedPreferences.Editor editor = savedLogin.edit();
        Log.d("Logout", "AccMan");
        editor.putInt("userId", 0);
        editor.putString("token", "");
        // Commit the edits!
        editor.commit();
        Intent intent = new Intent(this , LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Creates an error Alert
     */
    private void failureAlert()
    {
        Log.d("Alert", "Failure");
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Keine Daten geändert.");
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
     * Reads the Data from the User-Input and stores it in an Costumer Object
     * @return the new Customer
     */
    private Customer readUserData(){
        Customer customer = new Customer(this.getApplicationContext());
        customer.setCity(mCityText.getText().toString());
        customer.setCountry(mCountryText.getText().toString());
        customer.setEmail(mEmailText.getText().toString());
        customer.setExtraAddressLine(mExtraAddressLineText.getText().toString());
        customer.setHouseNumber(mHouseNumberText.getText().toString());
        customer.setLastname(mLastnameText.getText().toString());
        customer.setName(mNameText.getText().toString());
        customer.setPassword(mPasswordText.getText().toString());
        customer.setPhoneNumber(mPhoneNumberText.getText().toString());
        customer.setStreet(mStreetText.getText().toString());
        customer.setZipCode(Integer.parseInt(mZipCodeText.getText().toString()));
        return customer;
    }

    /**
     * Initializes the Text in the input fields.
     * Uses the Instance of the Singleton Customer
     */
    private void initializeTextEdits(){
        mNameText.setText(Customer.getInstance(this.getApplicationContext()).getName());
        mLastnameText.setText(Customer.getInstance(this.getApplicationContext()).getLastname());
        mEmailText.setText(Customer.getInstance(this.getApplicationContext()).getEmail());
        mPhoneNumberText.setText(Customer.getInstance(this.getApplicationContext()).getPhoneNumber());
        mHouseNumberText.setText(Customer.getInstance(this.getApplicationContext()).getHouseNumber());
        mStreetText.setText(Customer.getInstance(this.getApplicationContext()).getStreet());
        mExtraAddressLineText.setText(Customer.getInstance(this.getApplicationContext()).getExtraAddressLine());
        mZipCodeText.setText(Integer.toString(Customer.getInstance(this.getApplicationContext()).getZipCode()));
        mCityText.setText(Customer.getInstance(this.getApplicationContext()).getCity());
        mCountryText.setText(Customer.getInstance(this.getApplicationContext()).getCountry());
    }
}