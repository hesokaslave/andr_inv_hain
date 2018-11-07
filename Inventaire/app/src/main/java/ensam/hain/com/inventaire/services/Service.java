package ensam.hain.com.inventaire.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Service {

    public static void showAlert (Context context, String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
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
