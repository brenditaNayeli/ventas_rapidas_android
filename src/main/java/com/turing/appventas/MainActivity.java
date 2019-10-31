package com.turing.appventas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView tvUser,tvEmail,tvProveedor;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthStateListener;
    private static final int CONST = 1;
    private static final String PROVEEDOR_DESCONOCIDO="proveedor desconocido";
    //Devuelve el password de la conexion
    private static final String PASSWORD_FIREBASE="password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvUser=findViewById(R.id.tvUser);
        tvEmail=findViewById(R.id.tvEmail);
        tvProveedor=findViewById(R.id.tvProveedor);

        fbAuth=FirebaseAuth.getInstance();
        fbAuthStateListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Autenticacion del usuario actual
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if (user!=null){
                    onSetDataUser(user.getDisplayName(),user.getEmail(),user.getProviders()!=null?
                            user.getProviders().get(0):PROVEEDOR_DESCONOCIDO);

                }else{
                    //Limpiar iniciales de cuenta
                    onSignedOutCleanup();

                    AuthUI.IdpConfig facebookIdp= new AuthUI.IdpConfig.FacebookBuilder()
                            .setPermissions(Arrays.asList("user_friends","user_gender"))
                            .build();

                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTosUrl("www.google.com")
                            .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                    facebookIdp))
                            // .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build()))
                            .build(),CONST);
                }

            }
        };

        try {
            //Codigo para generar la clave hash
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.itz.auth3",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        }catch (PackageManager.NameNotFoundException e){

        }catch (NoSuchAlgorithmException e){

        }






    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == CONST){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Alfo Fallo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSetDataUser(String userName,String email, String provider){
        tvUser.setText(userName);
        tvEmail.setText(email);

        int drawableRes;
        switch (provider){
            case PASSWORD_FIREBASE:
                drawableRes= R.drawable.ic_firebase;
                break;
            default:
                drawableRes= R.drawable.ic_block_helper;
                provider=PROVEEDOR_DESCONOCIDO;
                break;
        }
        tvProveedor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawableRes,0,0,0);
        tvProveedor.setText(provider);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       switch (item.getItemId()) {
            case R.id.action_signOut:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);




        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        fbAuth.addAuthStateListener(fbAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fbAuthStateListener!=null){
            fbAuth.removeAuthStateListener(fbAuthStateListener);
        }
    }





    private  void onSignedOutCleanup(){
        onSetDataUser("","","");
    }

}
