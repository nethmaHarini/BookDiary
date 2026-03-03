package me.nethma.bookdiary.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

/**
 * Helper that wraps the Credential Manager + Firebase Auth Google Sign-In flow.
 */
public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";

    // ⚠️ Replace with your actual Web Client ID from Firebase Console
    public static final String WEB_CLIENT_ID =
            "557838479717-c8t7k7i9u74bkdivr3mqja57o1ibrfjp.apps.googleusercontent.com";

    public interface Callback {
        void onSuccess(String email, String displayName, String photoUrl, String googleId);
        void onError(String message);
    }

    private final Activity activity;
    private final CredentialManager credentialManager;
    private final FirebaseAuth firebaseAuth;

    public GoogleSignInHelper(Activity activity) {
        this.activity          = activity;
        this.credentialManager = CredentialManager.create(activity);
        this.firebaseAuth      = FirebaseAuth.getInstance();
    }

    public void signIn(Callback callback) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        handleCredential(result.getCredential(), callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Credential error: " + e.getMessage(), e);
                        activity.runOnUiThread(() ->
                                callback.onError("Google Sign-In failed: " + e.getMessage()));
                    }
                });
    }

    private void handleCredential(Credential credential, Callback callback) {
        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        .equals(credential.getType())) {
            try {
                GoogleIdTokenCredential googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.getData());

                String idToken     = googleCredential.getIdToken();
                String email       = googleCredential.getId();
                String displayName = googleCredential.getDisplayName() != null
                        ? googleCredential.getDisplayName() : email;
                String photoUrl    = googleCredential.getProfilePictureUri() != null
                        ? googleCredential.getProfilePictureUri().toString() : null;
                String googleId    = googleCredential.getId();

                AuthCredential firebaseCredential =
                        GoogleAuthProvider.getCredential(idToken, null);

                firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener(activity, authResult -> {
                            FirebaseUser firebaseUser = authResult.getUser();
                            String uid = firebaseUser != null
                                    ? firebaseUser.getUid() : googleId;
                            callback.onSuccess(email, displayName, photoUrl, uid);
                        })
                        .addOnFailureListener(activity, e -> {
                            Log.e(TAG, "Firebase auth failed", e);
                            callback.onError("Firebase authentication failed: " + e.getMessage());
                        });

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Google credential", e);
                activity.runOnUiThread(() ->
                        callback.onError("Failed to parse Google credential."));
            }
        } else {
            activity.runOnUiThread(() ->
                    callback.onError("Unexpected credential type."));
        }
    }
}

