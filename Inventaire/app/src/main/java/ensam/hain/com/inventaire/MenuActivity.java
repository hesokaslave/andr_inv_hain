package ensam.hain.com.inventaire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MenuActivity extends AppCompatActivity {

    ImageView btn_manuel, btn_rfid, btn_rfidwriter;
    ImageButton btn_bars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn_manuel = (ImageView) findViewById(R.id.btn_manuel);
        btn_manuel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(ManuelActivity.class);
            }
        });

        btn_bars = (ImageButton) findViewById(R.id.btn_bars);
        btn_bars.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(BarsActivity.class);
            }
        });

        btn_rfid = (ImageView) findViewById(R.id.btn_rfid);
        btn_rfid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(RFIDActivity.class);
            }
        });

        btn_rfidwriter = (ImageView) findViewById(R.id.btn_rfidwriter);
        btn_rfidwriter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(RFIDWriterActivity.class);
            }
        });

    }

    public void startActivity (Class target) {
        Intent INT=new Intent(this,target);
        INT.putExtra("server", getIntent().getStringExtra("server"));
        startActivity(INT);
    }
}
