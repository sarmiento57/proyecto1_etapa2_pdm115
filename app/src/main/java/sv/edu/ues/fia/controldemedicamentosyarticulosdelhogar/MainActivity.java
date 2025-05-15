package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static boolean habilitarVerDetalles = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferencias = getSharedPreferences("PREFERENCIAS_APP", Context.MODE_PRIVATE);
        boolean primeraVez = preferencias.getBoolean("primera_vez", true);

        if (primeraVez) {
            WebServiceHelper ws = new WebServiceHelper(this);

            // Ejecutar los scripts solo una vez
            ws.ejecutarScript("creation_db_script.sql");
            ws.ejecutarScript("user_table_filling_script.sql");
            ws.ejecutarScript("districts_filling_script.sql");
            ws.ejecutarScript("triggers.sql");

            Toast.makeText(this, "Base de datos MySQL inicializada", Toast.LENGTH_SHORT).show();

            // Marcar como ya ejecutado
            preferencias.edit().putBoolean("primera_vez", false).apply();
        }

        // Redirigir al LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
