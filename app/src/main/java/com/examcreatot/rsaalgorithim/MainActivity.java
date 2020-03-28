package com.examcreatot.rsaalgorithim;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import static java.util.Base64.getDecoder;

public class MainActivity extends AppCompatActivity {

    EditText editText ;
    TextView textView;
    ServerThread serverThread = new ServerThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.EMFPC);
        textView = (TextView)findViewById(R.id.plaintText);

        Thread thread = new Thread(serverThread);
        thread.start();

    }

    public void clear(View view) {
        editText.setText("");
    }

    class ServerThread implements Runnable{

        Socket socket;
        ServerSocket serverSocket;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        Handler handler = new Handler();

         public String getMessage() {
             return message;
         }

         String message;

         public byte[] getMessageb() {
             return messageb;
         }

         byte[] messageb;

        @Override
        public void run() {

            try {

                serverSocket = new ServerSocket(7301);
                while (true){
                    socket = serverSocket.accept();
                    DataInputStream dIn = new DataInputStream(socket.getInputStream());
                    message = dIn.readUTF();
                    int length = dIn.readInt();
                    if(length>0) {
                        messageb = new byte[length];
                        dIn.readFully(messageb, 0, messageb.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                    editText.setText("Encrypted Message:\n\n"+message);
                            }
                        });
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //editText.setText("Encrypted Message\n"+message);
                        }
                    });

                }

            }catch (final Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String cipherText, PublicKey publicKey) throws Exception {

        byte[] bytes = getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, publicKey);

        return new String(decriptCipher.doFinal(bytes));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void decrypt(View view) {


        try {
            String plain = decrypt(serverThread.getMessage() , getPublicKey(serverThread.getMessageb()));
            String newString = getString(plain);
            textView.setText("Plain Text:\n"+newString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getString(String plain) {

        return plain.replace("�","");
    }

    static PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
        return factory.generatePublic(encodedKeySpec);
    }

}
