package ensam.hain.com.inventaire;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RFIDWriterActivity extends AppCompatActivity {

    EditText txt_title, txt_code, txt_codeRFID, txt_four;
    Button btn_write;
    static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Informations écrites avec succès sur le Tag !";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    String title, code, codeRFID, four, serverAdress, tagContent;
    String[] product_values ;
    String[] fours_values ;
    Spinner spinner;
    Spinner productSpinner;
    ArrayAdapter<String> adapter, adapter2;
    RequestQueue queue;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rfidwriter);

            serverAdress = getIntent().getStringExtra("server");
            String url =serverAdress+"/connect";
            context = this;
            txt_title = (EditText) findViewById(R.id.txt_title);
            txt_code = (EditText) findViewById(R.id.txt_code);
            txt_codeRFID = (EditText) findViewById(R.id.txt_codeRFID);
            txt_four = (EditText) findViewById(R.id.txt_four);

            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter == null) {
                // Stop here, we definitely need NFC
                Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
                finish();
            }
            readFromIntent(getIntent());

            queue = Volley.newRequestQueue(this);

            spinner = (Spinner) findViewById(R.id.spinner_four);
            productSpinner = (Spinner) findViewById(R.id.spinner_prod);

            btn_write = (Button) findViewById(R.id.btn_write);
                btn_write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    write();
                }
            });
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[] { tagDetected };
            sendRequest(url);

    }
        /******************************************************************************
         **********************************Read From NFC Tag***************************
         ******************************************************************************/

        private void readFromIntent(Intent intent) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage[] msgs = null;
                if (rawMsgs != null) {
                    msgs = new NdefMessage[rawMsgs.length];
                    for (int i = 0; i < rawMsgs.length; i++) {
                        msgs[i] = (NdefMessage) rawMsgs[i];
                    }
                }
                buildTagViews(msgs);
            }
        }

        private void buildTagViews(NdefMessage[] msgs) {
            if (msgs == null || msgs.length == 0) return;
            String text = "";
            //String tagId = new String(msgs[0].getRecords()[0].getType());
            byte[] payload = msgs[0].getRecords()[0].getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
            int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            try {
                // Get the Text
                text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            } catch (UnsupportedEncodingException e) { Log.e("UnsupportedEncoding", e.toString()); }

            try {
                JSONObject obj = new JSONObject(text);
                txt_title.setText(obj.getString("title"));
                txt_code.setText(obj.getString("code"));
                txt_codeRFID.setText(obj.getString("codeRFID"));
                txt_four.setText(obj.getString("fournisseur"));
            }
            catch (JSONException ex) {
                ex.printStackTrace();
                Toast.makeText(context, "Content Conversion Error", Toast.LENGTH_SHORT).show();
            }
        }

        /******************************************************************************
         ********************************** Write to NFC Tag **************************
         ******************************************************************************/

        private void write(String text, Tag tag) throws IOException, FormatException {
            NdefRecord[] records = { createRecord(text) };
            NdefMessage message = new NdefMessage(records);
            // Get an instance of Ndef for the tag.
            Ndef ndef = Ndef.get(tag);
            // Enable I/O
            ndef.connect();
            // Write the message
            ndef.writeNdefMessage(message);
            // Close the connection
            ndef.close();
        }

        private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
            String lang       = "en";
            byte[] textBytes  = text.getBytes();
            byte[] langBytes  = lang.getBytes("US-ASCII");
            int    langLength = langBytes.length;
            int    textLength = textBytes.length;
            byte[] payload    = new byte[1 + langLength + textLength];

            // set status byte (see NDEF spec for actual bits)
            payload[0] = (byte) langLength;

            // copy langbytes and textbytes into payload
            System.arraycopy(langBytes, 0, payload, 1,              langLength);
            System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

            NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

            return recordNFC;
        }

        @Override
        protected void onNewIntent(Intent intent) {
            setIntent(intent);
            readFromIntent(intent);
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
                myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            }
        }

        @Override
        public void onPause(){
            super.onPause();
            WriteModeOff();
        }

        @Override
        public void onResume(){
            super.onResume();
            WriteModeOn();
        }

        /******************************************************************************
         **********************************Enable Write********************************
         ******************************************************************************/
        private void WriteModeOn(){
            writeMode = true;
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
        }
        /******************************************************************************
         **********************************Disable Write*******************************
         ******************************************************************************/
        private void WriteModeOff(){
            writeMode = false;
            nfcAdapter.disableForegroundDispatch(this);
        }

        public void sendRequest(String url) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray ar =  jsonObject.getJSONArray("products");
                            JSONArray ar2 =  jsonObject.getJSONArray("fournisseurs");
                            product_values = new String[ar.length()];
                            fours_values = new String[ar2.length()];
                            for(int i=0; i<ar.length(); i++) {
                                JSONObject json_data = ar.getJSONObject(i);
                                product_values[i] = json_data.getString("code");
                            }
                            for(int i=0; i<ar2.length(); i++) {
                                JSONObject json_data = ar2.getJSONObject(i);
                                fours_values[i] = json_data.getString("nom");
                            }
                            setSpinnerAdapter(spinner, adapter, fours_values);
                            setSpinnerAdapter(productSpinner, adapter2, product_values);
                        } catch (JSONException e) { e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg("Une Erreur est survenue ! ");
            }
        });
        queue.add(stringRequest);
    }
    public void setSpinnerAdapter (Spinner sp, ArrayAdapter adap, String[] values) {
        adap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sp.setAdapter(adap);
    }

    public void displayMsg (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void write() {
        String url = serverAdress+"/product/api/"+productSpinner.getSelectedItem().toString();
        System.out.println(url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray products = jsonObject.getJSONArray("product");
                            code = ((JSONObject)products.get(0)).getString("code");
                            title = ((JSONObject)products.get(0)).getString("nom");
                            codeRFID = ((JSONObject)products.get(0)).getString("codeRFID");
                            four = spinner.getSelectedItem().toString();

                            tagContent = "{\"title\":\""+title+"\",\"code\":\""
                                    +code+"\", \"codeRFID\":\""+codeRFID+"\",\"fournisseur\":\""
                                    +four+"\"}";
                            try {
                                if (myTag == null) {
                                    Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                                } else {
                                    write(tagContent, myTag);
                                    Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        } catch (JSONException e) { e.printStackTrace();  Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg("Une Erreur est survenue ! "+error);
            }
        });
        queue.add(stringRequest);
    }


}
