package com.transportapp.lazar.transportapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.transportapp.lazar.transportapp.R;

import java.util.HashMap;
import java.util.Map;

import helpers.InternetHelper;
import helpers.NavigationHelper;
import services.UserService;

import static utils.Constants.LOGIN;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private NavigationHelper navigationHelper;

    /* UI references */
    private EditText emailTextView;
    private EditText passwordTextView;
    private Button loginButon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationHelper = new NavigationHelper(this);

        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        loginButon = findViewById(R.id.login_button);

        EditText[] editTexts = {emailTextView, passwordTextView};

        for (EditText editText : editTexts) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(!loginButon.isEnabled()) {
                        /* button disabled */
                        if(!TextUtils.isEmpty(emailTextView.getText().toString()) && !TextUtils.isEmpty(passwordTextView.getText().toString())) {
                            loginButon.setEnabled(true);
                        }
                    } else {
                        /* button enabled */
                        if(TextUtils.isEmpty(emailTextView.getText().toString()) || TextUtils.isEmpty(passwordTextView.getText().toString())) {
                            loginButon.setEnabled(false);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        InternetHelper.checkIfConnected(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.login_button) {

            InternetHelper.checkIfConnected(this);

            if(InternetHelper.internet) {
                Map<String, String> body = new HashMap<String, String>();
                body.put("email", emailTextView.getText().toString());
                body.put("password", passwordTextView.getText().toString());
                new UserService(LOGIN, body, null,this, this).execute();
            }

        } else if (view.getId() == R.id.register_button){
            navigationHelper.navigateTo(RegisterActivity.class, this);
        }
    }

}
