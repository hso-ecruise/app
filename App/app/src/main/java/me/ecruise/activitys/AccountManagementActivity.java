package me.ecruise.activitys;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import me.ecruise.data.CustomerData;
import me.ecruise.data.Server;

public class AccountManagementActivity extends AppCompatActivity{

    CustomerData newCustomerData;
    CustomerData actualCustomerData;
    LinearLayout login;
    LinearLayout person;
    LinearLayout address;

    public AccountManagementActivity(){
        actualCustomerData = Server.getInstance().getUserData();
        newCustomerData = new CustomerData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initializeTextEdits(actualCustomerData);


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

    }

    private void confirm(){
        updateUserData(newCustomerData);
        if(actualCustomerData.checkForChanges(newCustomerData)){
            if(Server.getInstance().updateUserData(newCustomerData)){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                dlgAlert.setMessage("Ihre Angaben wurden erfolgreich geändert.");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        }
        else{
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

    }

    private void updateUserData(CustomerData newCustomerData){
        EditText mNameText = (EditText) findViewById(R.id.nameText);
        newCustomerData.name = mNameText.getText().toString();

        EditText mLastnameText = (EditText) findViewById(R.id.lastnameText);
        newCustomerData.lastname = mLastnameText.getText().toString();

        EditText mEmailText = (EditText) findViewById(R.id.emailText);
        newCustomerData.email = mEmailText.getText().toString();

        EditText mPhoneNumberText = (EditText) findViewById(R.id.phoneNumberText);
        newCustomerData.phoneNumber = mPhoneNumberText.getText().toString();

        EditText mPasswordText = (EditText) findViewById(R.id.passwordText);
        newCustomerData.password = mPasswordText.getText().toString();

        EditText mPassword2Text = (EditText) findViewById(R.id.password2Text);
        newCustomerData.password = mPassword2Text.getText().toString();

        EditText mStreetText = (EditText) findViewById(R.id.streetText);
        newCustomerData.street = mStreetText.getText().toString();

        EditText mHouseNumberText = (EditText) findViewById(R.id.houseNumberText);
        newCustomerData.houseNumber = mHouseNumberText.getText().toString();

        EditText mExtraAddressLineText = (EditText) findViewById(R.id.extraAddressLineText);
        newCustomerData.extraAddressLine = mExtraAddressLineText.getText().toString();

        EditText mZipCodeText = (EditText) findViewById(R.id.zipCodeText);
        newCustomerData.zipCode = mZipCodeText.getText().toString();

        EditText mCityText = (EditText) findViewById(R.id.cityText);
        newCustomerData.city = mCityText.getText().toString();

        EditText mCountryText = (EditText) findViewById(R.id.countryText);
        newCustomerData.country = mCountryText.getText().toString();
    }

    private void initializeTextEdits(CustomerData customerData){
        EditText mNameText = (EditText) findViewById(R.id.nameText);
        mNameText.setText(customerData.name);

        EditText mLastnameText = (EditText) findViewById(R.id.lastnameText);
        mLastnameText.setText(customerData.lastname);

        EditText mEmailText = (EditText) findViewById(R.id.emailText);
        mEmailText.setText(customerData.email);

        EditText mPhoneNumberText = (EditText) findViewById(R.id.phoneNumberText);
        mPhoneNumberText.setText(customerData.phoneNumber);

        EditText mPasswordText = (EditText) findViewById(R.id.passwordText);
        mPasswordText.setText(customerData.password);

        EditText mPassword2Text = (EditText) findViewById(R.id.password2Text);
        mPassword2Text.setText(customerData.password);

        EditText mStreetText = (EditText) findViewById(R.id.streetText);
        mStreetText.setText(customerData.street);

        EditText mHouseNumberText = (EditText) findViewById(R.id.houseNumberText);
        mHouseNumberText.setText(customerData.houseNumber);

        EditText mExtraAddressLineText = (EditText) findViewById(R.id.extraAddressLineText);
        mExtraAddressLineText.setText(customerData.extraAddressLine);

        EditText mZipCodeText = (EditText) findViewById(R.id.zipCodeText);
        mZipCodeText.setText(customerData.zipCode);

        EditText mCityText = (EditText) findViewById(R.id.cityText);
        mCityText.setText(customerData.city);

        EditText mCountryText = (EditText) findViewById(R.id.countryText);
        mCountryText.setText(customerData.country);
    }
}
