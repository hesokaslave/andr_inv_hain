package ensam.hain.com.inventaire;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class ManuelActivity extends AppCompatActivity {

    public static int RECEPTION_MODE = 0;
    public static int LIVRAISON_MODE = 1;
    public static int INVENTAIRE_MODE = 2;

    int mode = 0;

    Spinner spinner;
    Spinner productSpinner;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    String serverAdress;
    RequestQueue queue;
    Button send, btn_reception, btn_livraison, btn_invent;
    TextView actor,quant;
    EditText txt_quantite;
    String[] product_values ;
    String[] fours_values ;
    String[] clients_values ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manuel);
         serverAdress = getIntent().getStringExtra("server");
        spinner = (Spinner) findViewById(R.id.spinner2);
        productSpinner = (Spinner) findViewById(R.id.spinner3);
        actor = (TextView)  findViewById(R.id.textView4);
        txt_quantite = (EditText) findViewById(R.id.txt_quantity);
        txt_quantite.setText("1");
        queue = Volley.newRequestQueue(this);
        send = (Button) findViewById(R.id.button);
        btn_livraison = (Button) findViewById(R.id.btn_livraison);
        btn_invent = (Button) findViewById(R.id.btn_invent);
        btn_reception = (Button) findViewById(R.id.btn_reception);
        quant = (TextView) findViewById(R.id.textView6);

        btn_reception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_quantite.setVisibility(View.VISIBLE);
                quant.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
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
                txt_quantite.setVisibility(View.VISIBLE);
                quant.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                btn_reception.setBackgroundColor(Color.LTGRAY);
                btn_livraison.setBackgroundColor(Color.CYAN);
                btn_invent.setBackgroundColor(Color.LTGRAY);
                mode = ManuelActivity.LIVRAISON_MODE;
                setSpinnerAdapter(spinner,adapter,clients_values);
                actor.setText("Client");
            }
        });

        btn_invent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_quantite.setVisibility(View.INVISIBLE);
                quant.setVisibility(View.INVISIBLE);
                send.setVisibility(View.INVISIBLE);
                btn_reception.setBackgroundColor(Color.LTGRAY);
                btn_livraison.setBackgroundColor(Color.LTGRAY);
                btn_invent.setBackgroundColor(Color.CYAN);
                mode = ManuelActivity.INVENTAIRE_MODE;
                getStock(productSpinner.getSelectedItem().toString());
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(txt_quantite.getText().toString().isEmpty() || Integer.parseInt(txt_quantite.getText().toString()) == 0) {
                    displayMsg("Saisissez une Quantité Valide !");
                    return;
                }
                System.out.println("Mode "+mode);
                if(mode == LIVRAISON_MODE) {
                    System.out.println("Livraison");
                    newOp(serverAdress+"/manuel/livraison", productSpinner.getSelectedItem().toString(), spinner.getSelectedItem().toString(),txt_quantite.getText().toString());
                }
                if(mode == RECEPTION_MODE) {
                    newOp(serverAdress+"/manuel/reception", productSpinner.getSelectedItem().toString(), spinner.getSelectedItem().toString(),txt_quantite.getText().toString());
                }
            }
        });

        String url =serverAdress+"/connect";
        sendRequest(url, Request.Method.GET);
        String[] product_values = {"Not Connected"};
        String[] fours_values = {"Not Connected"};

        setSpinnerAdapter(spinner, adapter, product_values);
        setSpinnerAdapter(productSpinner, adapter2, fours_values);
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
                            JSONArray ar =  jsonObject.getJSONArray("products");
                            JSONArray ar2 =  jsonObject.getJSONArray("fournisseurs");
                            JSONArray ar3 =  jsonObject.getJSONArray("clients");
                            product_values = new String[ar.length()];
                            fours_values = new String[ar2.length()];
                            clients_values = new String[ar3.length()];

                            for(int i=0; i<ar.length(); i++) {
                                JSONObject json_data = ar.getJSONObject(i);
                                product_values[i] = json_data.getString("code");
                            }

                            for(int i=0; i<ar2.length(); i++) {
                                JSONObject json_data = ar2.getJSONObject(i);
                                fours_values[i] = json_data.getString("nom");
                            }

                            for(int i=0; i<ar3.length(); i++) {
                                JSONObject json_data = ar3.getJSONObject(i);
                                clients_values[i] = json_data.getString("nom");
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

    public void newOp(String url, String product, String actor, String quantite) {

        JSONObject params = new JSONObject();
        try {
            params.put("product", product);
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
                            txt_quantite.setText("");
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
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);
    }


    public void getStock(final String code) {
        String url = serverAdress+"/product/api/"+code;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            System.out.println(jsonObject);
                            JSONArray products = jsonObject.getJSONArray("product");
                            int stock = ((JSONObject)products.get(0)).getInt("stock");
                            String nom = ((JSONObject)products.get(0)).getString("nom");
                            long date = ((JSONObject)products.get(0)).getLong("updatedAt");
                            showAlert("Date : "+new Date(date).toString()+" \n\nLe Stock Actuel du Produit "+nom+" est: "+stock);
                        } catch (JSONException e) { e.printStackTrace();}
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
