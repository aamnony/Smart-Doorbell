package com.github.aamnony.smartdoorbell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChooseMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getCanonicalName();

    private EditText editUsername;
    private EditText editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> signInUser(editUsername.getText().toString()));

        // Initialize application
        AppHelper.init(getApplicationContext());
        findCurrent();
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        switch (userStateDetails.getUserState()) {
                            case SIGNED_IN:
                                Log.d(TAG, "SIGNED_IN: " + editUsername.getText().toString());
//                                startPubSubActivity();
                                break;
                            case SIGNED_OUT:
                                Log.d(TAG, "SIGNED_OUT: " + editUsername.getText().toString());
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(editUsername.getText().toString())
                                        .addOnCompleteListener(task -> {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Failed unsubscribing from FCM", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Initialization error.", e);
                    }
                }
        );

    }

    private void signInUser(String username) {
        AppHelper.setUser(username);
        AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);

        final String password = editPassword.getText().toString();
        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                    switch (signInResult.getSignInState()) {
                        case DONE:
                            startMenuActivity();
                            break;
                        case SMS_MFA:
//                          makeToast("Please confirm sign-in with SMS.");
                            break;
                        case NEW_PASSWORD_REQUIRED:
                            Toast.makeText(LoginActivity.this, "Please confirm sign-in with new password.", Toast.LENGTH_SHORT).show();
//                            AWSMobileClient.getInstance().confirmSignIn("QAZwsx311", new Callback<SignInResult>() {
//                                @Override
//                                public void onResult(SignInResult signInResult) {
//                                    Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
//                                    switch (signInResult.getSignInState()) {
//                                        case DONE:
//                                            startPubSubActivity();
//                                            break;
//                                        case SMS_MFA:
////                        makeToast("Please confirm sign-in with SMS.");
//                                            break;
//                                        default:
////                        makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
//                                            break;
//                                    }
//                                }
//                                @Override
//                                public void onError(Exception e) {
//                                    Log.e(TAG, "Sign-in error", e);
//                                }
//                            });
                            break;
                        default:
//                          makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                            break;
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-in error", e);
            }
        });

    }

    private void startMenuActivity() {
        FirebaseMessaging.getInstance().subscribeToTopic(editUsername.getText().toString())
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Failed subscribing to FCM", Toast.LENGTH_SHORT).show();
                    }
                });
        Intent MenuIntent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(MenuIntent);
    }

    private void findCurrent() {
        CognitoUser user = AppHelper.getPool().getCurrentUser();
        String username = user.getUserId();
        if (username != null) {
            AppHelper.setUser(username);
            editUsername.setText(user.getUserId());
            user.getSessionInBackground(authenticationHandler);
        }
    }

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if (username != null) {
            editUsername.setText(username);
            AppHelper.setUser(username);
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(
                editUsername.getText().toString(),
                editPassword.getText().toString(),
                null
        );
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }


    private AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
//            Log.d(TAG, " -- Auth Success");
//            AppHelper.setCurrSession(cognitoUserSession);
//            AppHelper.newDevice(device);
            startMenuActivity();
        }

        @Override
        public void onFailure(Exception e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            Locale.setDefault(Locale.US);
            getUserAuthentication(authenticationContinuation, username);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
//            mfaAuth(multiFactorAuthenticationContinuation);
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            /**
             * For Custom authentication challenge, implement your logic to present challenge to the
             * user and pass the user's responses to the continuation.
             */
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                // This is the first sign-in attempt for an admin created user
                NewPasswordContinuation newPasswordContinuation = (NewPasswordContinuation) continuation;
//                AppHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
//                        newPasswordContinuation.getRequiredAttributes());
                newPasswordContinuation.setUserAttribute("address", "unknown");
                final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                final EditText input = new EditText(LoginActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                alert.setView(input);
                alert.setTitle("Change Password");
                alert.setPositiveButton("OK", (dialog, which) -> {
                    newPasswordContinuation.setPassword(input.getText().toString());
                    newPasswordContinuation.continueTask();
                });
                alert.show();
            }
        }
    };

}
