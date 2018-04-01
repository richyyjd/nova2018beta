package ai.city.cochabamba.nova;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

//---------------
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
/*import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;*/
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeakerLabel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;
//---

/**
 * Class MainActivity
 * Created by Richard on 31-Mar-2018.
 */

public class MainActivity extends AppCompatActivity {

    private static final String APP_TAG_NOVABOT = "NOVA-BOT:::::: ";

    private static final String CONVERSATION_SERVICE_WORKSPACE = "WORKSPACEID";
    private static final String CONVERSATION_SERVICE_USERNAME = "USERNAME";
    private static final String CONVERSATION_SERVICE_PASSWORD = "PASSWORD";

    private static final String TTS_SERVICE_USERNAME = "USERNAME";
    private static final String TTS_SERVICE_PASSWORD = "PASSWORD";

    private TextView msg_respuesta, consola;
    private EditText inputMessage;
    private Button btnSend;
    private Map<String,Object> context = new HashMap<>();

    //BLOQUE TTSNOVABOT
    private String textoParaHablar="";
    StreamPlayer streamPlayer;
    //FIN BLOQUE TTSNOVABOT

    //////-----------
    //private com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg_respuesta = findViewById(R.id.txtrespuesta);
        inputMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        consola = findViewById(R.id.consolatxt);

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkInternetConnection()) {
                    Log.d(APP_TAG_NOVABOT, "Antes de enviar mensaje");
                    sendMessage();
                    Log.d(APP_TAG_NOVABOT, "DESPUES de enviar mensaje");

                    //ocultar teclado
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputMessage.getWindowToken(), 0);
                    // fin ocultar teclado
                }
            }
        });

    }

    /*
     * BOT con Conversation Service
     * y luego TTS con hablaWatsonTTS();
     */
    private void sendMessage(){
        final String inputmessage = this.inputMessage.getText().toString().trim();

        Message inputMessage = new Message();
        inputMessage.setMessage(inputmessage);
        Log.d(APP_TAG_NOVABOT, "MSG ENVIADO: "+inputmessage);
        this.inputMessage.setText("");

        Thread thread = new Thread(new Runnable(){
            public void run() {
                try {

                    //NUEVO 2018
                    com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;

                    Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                    service.setUsernameAndPassword(CONVERSATION_SERVICE_USERNAME, CONVERSATION_SERVICE_PASSWORD);
                    InputData input = new InputData.Builder(inputmessage).build();
                    MessageOptions options = new MessageOptions.Builder(CONVERSATION_SERVICE_WORKSPACE).input(input).context(context).build();
                    MessageResponse response = service.message(options).execute();
                    Log.d(APP_TAG_NOVABOT, "=============================: "+response.getOutput().get("text").toString().replace("[","").replace("]",""));

                    /*if(response.getContext() !=null){
                        context.clear();
                        context = response.getContext();
                    }
*/
                    //Message outMessage=new Message();

                    if(response!=null){
                        if(response.getOutput()!=null && response.getOutput().containsKey("text")){

                            final String outputmessage = response.getOutput().get("text").toString().replace("[","").replace("]","");

                            //Mensaje recibido desde WATSON
                            Log.d(APP_TAG_NOVABOT, "MENSAJE RECIBIDO: "+outputmessage.toString());
                            textoParaHablar = outputmessage.toString();
                            Log.d(APP_TAG_NOVABOT, "MENSAJE RECIBIDO: "+textoParaHablar);
                            Log.d(APP_TAG_NOVABOT, "======== Antes de hablaWatsonTTS ");
                            //executeWatsonTTS();
                            hablaWatsonTTS(textoParaHablar);
                            Log.d(APP_TAG_NOVABOT, "======== Despues de hablaWatsonTTS ");
                            msg_respuesta.setText(outputmessage.toString());

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    private void hablaWatsonTTS(String textoParaHablar){
        TextToSpeech service = new TextToSpeech();
        service.setUsernameAndPassword(TTS_SERVICE_USERNAME, TTS_SERVICE_PASSWORD);

        try {
            Log.d(APP_TAG_NOVABOT, "======== 1");
            streamPlayer = new StreamPlayer();

            Log.d(APP_TAG_NOVABOT, "======== 2");
            streamPlayer.playStream(service.synthesize(textoParaHablar, Voice.ES_SOFIA).execute());

            Log.d(APP_TAG_NOVABOT, "======== 3");

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * BOT
     */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected){
            return true;
        } else {
            Toast.makeText(this, " No hay Internet!!! [o_O] ", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
