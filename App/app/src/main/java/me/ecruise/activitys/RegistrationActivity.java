package me.ecruise.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameText = (EditText) findViewById(R.id.nameText);
        mLastnameText = (EditText) findViewById(R.id.lastnameText);;
        mEmailText = (EditText) findViewById(R.id.emailText);
        mEmailText2 = (EditText) findViewById(R.id.emailText2);
        mPasswordText = (EditText) findViewById(R.id.passwordText);
        mPasswordText2 = (EditText) findViewById(R.id.passwordText2);

        Button mRegistrationButton = (Button) findViewById(R.id.registrationButton);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    public void confirm()
    {
        if(validateTextEdits())
        {
            Customer.getInstance(this.getApplicationContext()).setName(mNameText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setLastname(mLastnameText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setEmail(mEmailText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).setPassword(mPasswordText.getText().toString());
            Customer.getInstance(this.getApplicationContext()).registerUser();
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public boolean validateTextEdits()
    {
        if(mNameText.getText().toString().isEmpty())
        {
            mNameText.setError("Pflichtfeld");
            mNameText.requestFocus();
            return false;
        }
        if(mLastnameText.getText().toString().isEmpty())
        {
            mLastnameText.setError("Pflichtfeld");
            mLastnameText.requestFocus();
            return false;
        }
        if(mEmailText.getText().toString().isEmpty())
        {
            mEmailText.setError("Pflichtfeld");
            mEmailText.requestFocus();
            return false;
        }
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
        if(mPasswordText.getText().toString().isEmpty())
        {
            mPasswordText.setError("Pflichtfeld");
            mPasswordText.requestFocus();
            return false;
        }
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
        return true;
    }

}
