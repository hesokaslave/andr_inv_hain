package ensam.hain.com.inventaire;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HomeActivity extends AppCompatActivity {
    EditText input_adress;
    String hostAdress ="";
    RequestQueue queue;
    ImageButton connect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        input_adress = findViewById(R.id.editText2);
        input_adress.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        connect = findViewById(R.id.button2);
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendRequest();
            }
        });
        queue = Volley.newRequestQueue(this);
    }

    public void sendRequest () {
        hostAdress = input_adress.getText().toString();
        String url = hostAdress+"/connect";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                            displayMsg("Connected Successfully ! ");
                            startActivity();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayMsg("Failed to Connect !");
            }
        });
        queue.add(stringRequest);
    }

    public void displayMsg (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void startActivity () {
        Intent INT=new Intent(this,MenuActivity.class);
        INT.putExtra("server", hostAdress);
        startActivity(INT);
    }
}
