package ensam.hain.com.inventaire;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class BarsActivity extends AppCompatActivity {

    // Spinner element
    Spinner spinner;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    String serverAdress;
    RequestQueue queue;
    Button send, plus, moins;
    TextView actor;
    EditText txt_quantite,txt_barcode;
    String[] product_values = {"Not Connected"} ;
    String[] fours_values = {"Not Connected"} ;
    String[] clients_values = {"Not Connected"} ;
    Button btn_reception, btn_livraison, btn_invent;

    public static int RECEPTION_MODE = 0;
    public static int LIVRAISON_MODE = 1;
    public static int INVENTAIRE_MODE = 2;

    int mode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bars);
        serverAdress = getIntent().getStringExtra("server");
        spinner = (Spinner) findViewById(R.id.spinner);
        actor = (TextView)  findViewById(R.id.txt_actor);
        txt_quantite = (EditText) findViewById(R.id.txt_quantity);
        txt_quantite.setText("1");
        txt_quantite.setEnabled(false);

        btn_livraison = (Button) findViewById(R.id.btn_livraison);
        btn_invent = (Button) findViewById(R.id.btn_invent);
        btn_reception = (Button) findViewById(R.id.btn_reception);


        txt_barcode = (EditText) findViewById(R.id.txt_barcode);
        plus = (Button) findViewById(R.id.plus);
        plus.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                txt_quantite.setText((Integer.parseInt(txt_quantite.getText().toString()) + 1)+"");
            }
        });

        moins = (Button) findViewById(R.id.moins);
        moins.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                txt_quantite.setText((Integer.parseInt(txt_quantite.getText().toString()) - 1)+"");
            }
        });

        txt_barcode.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(s.toString().length() == 13 ) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            execute();
                        }
                    }, 600);

                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


        queue = Volley.newRequestQueue(this);


        String url =serverAdress+"/connect";
        sendRequest(url, Request.Method.GET);
        setSpinnerAdapter(spinner, adapter, fours_values);

        btn_reception.setBackgroundColor(Color.CYAN);
        btn_reception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_reception.setBackgroundColor(Color.CYAN);
                btn_livraison.setBackgroundColor(Color.LTGRAY);
                btn_invent.setBackgroundColor(Color.LTGRAY);
                mode = ManuelActivity.RECEPTION_MODE;
                setSpinnerAdapter(spinner,adapter,fours_values);
                actor.setText("Fournisseur");

            }
        });

        btn_livraison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_reception.setBackgroundColor(Color.LTGRAY);
                btn_livraison.setBackgroundColor(Color.CYAN);
                btn_invent.setBackgroundColor(Color.LTGRAY);
                mode = ManuelActivity.LIVRAISON_MODE;
                actor.setText("Client");
                setSpinnerAdapter(spinner, adapter, clients_values);
            }
        });

        btn_invent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_reception.setBackgroundColor(Color.LTGRAY);
                btn_livraison.setBackgroundColor(Color.LTGRAY);
                btn_invent.setBackgroundColor(Color.CYAN);
                mode = ManuelActivity.INVENTAIRE_MODE;
            }
        });


    }

    public void setSpinnerAdapter (Spinner sp, ArrayAdapter adap, String[] values) {
        adap = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adap.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sp.setAdapter(adap);
    }

    public void displayMsg (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void sendRequest(String url, int method) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray ar2 =  jsonObject.getJSONArray("fournisseurs");
                            JSONArray ar3 =  jsonObject.getJSONArray("clients");
                            fours_values = new String[ar2.length()];
                            clients_values = new String[ar3.length()];

                            for(int i=0; i<ar2.length(); i++) {
                                JSONObject json_data = ar2.getJSONObject(i);
                                fours_values[i] = json_data.getString("nom");
                            }

                            for(int i=0; i<ar3.length(); i++) {
                                JSONObject json_data = ar3.getJSONObject(i);
                                clients_values[i] = json_data.getString("nom");
                            }

                            setSpinnerAdapter(spinner, adapter, fours_values);
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

    public void newOp(String url, String barCode, String actor, String quantite) {

        JSONObject params = new JSONObject();
        try {
            params.put("barCode", barCode);
            params.put("actor", actor);
            params.put("quantite", quantite);
        } catch (JSONException e) {e.printStackTrace();}

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String err = response.getString("error");
                            if(err != null) displayMsg(err);
                        } catch(JSONException e) {
                            String stock ="N/A";
                            try { stock = response.getString("stock");} catch (JSONException ex){ ex.printStackTrace();}
                            displayMsg("Opération Réussie ! Nouveau Stock : "+stock);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg("Une Erreur est survenue ! ");
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);
    }


    public void execute() {
        if (txt_barcode.getText().toString().isEmpty()) {
            displayMsg("Scannez un Code à barres d'abord ! !");
            return;
        }
        if (txt_quantite.getText().toString() == "0" || txt_quantite.getText().toString().isEmpty()) {
            displayMsg("Saisissez une Quantité Valide !");
            return;
        }
        if (mode == LIVRAISON_MODE) {
            newOp(serverAdress + "/bars/livraison", txt_barcode.getText().toString(), spinner.getSelectedItem().toString(), txt_quantite.getText().toString());
        }
        if(mode == RECEPTION_MODE) {
            newOp(serverAdress + "/bars/reception", txt_barcode.getText().toString(), spinner.getSelectedItem().toString(), txt_quantite.getText().toString());
        }

        if (mode == INVENTAIRE_MODE) {
            getBarStock(txt_barcode.getText().toString());
        }
        txt_barcode.setText("");
    }

    public void getBarStock(final String code) {
        String url = serverAdress+"/product/bar/api/"+code;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            System.out.println(jsonObject);
                            JSONArray products = jsonObject.getJSONArray("product");
                            String codePr = ((JSONObject)products.get(0)).getString("code");
                            int stock = ((JSONObject)products.get(0)).getInt("stock");
                            String nom = ((JSONObject)products.get(0)).getString("nom");
                            long date = ((JSONObject)products.get(0)).getLong("updatedAt");
                            showAlert("Date : "+new Date(date).toString()+" \n\nLe Stock Actuel du Produit "+nom+" est: "+stock);
                        } catch (JSONException e) { e.printStackTrace(); showAlert("Produit Inexistant ou une Erreur s'est produite !");}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg("Une Erreur est survenue ! "+error);
            }
        });
        queue.add(stringRequest);
    }

    public void showAlert (String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(msg);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK !",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


}