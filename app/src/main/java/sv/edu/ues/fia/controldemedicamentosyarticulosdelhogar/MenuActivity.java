package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private static final String [] ACTIVIDADES = {
            "DireccionActivity",
            "SucursalFarmaciaActivity",
            "DetalleExistenciaActivity",
            "DetalleVentaActivity",
            "FacturaVentaActivity",
            "ClienteActivity",
            "DetalleCompraActivity",
            "FacturaCompraActivity",
            "ProveedorActivity",
            "FormaFarmaceuticaActivity",
            "MarcaActivity",
            "ViaAdministracionActivity",
            "RecetaActivity",
            "DoctorActivity",
            "ArticuloActivity",
            "CategoriaActivity",
            "SubCategoriaActivity",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferencias = getSharedPreferences("PERMISOS_APP", Context.MODE_PRIVATE);

        List<String> opcionesFiltradas = new ArrayList<>();
        List<Integer> iconosFiltrados = new ArrayList<>();
        List<String> idMenuFiltrado = new ArrayList<>();

        String[] todasOpciones = getResources().getStringArray(R.array.options_menu);
        String[] idMenu = getResources().getStringArray(R.array.id_menu); // Lista de todas las opciones
        int[] todosIconos = getIconos(getResources().getStringArray(R.array.main_icons));

        for (int i = 0; i < idMenu.length; i++) {
            boolean tienePermiso = preferencias.getBoolean(idMenu[i], false);
            Log.d("MenuActivity", "Opción: " + idMenu[i] + ", Tiene permiso: " + tienePermiso);
            if (tienePermiso) {
                opcionesFiltradas.add(todasOpciones[i]);
                iconosFiltrados.add(todosIconos[i]);
                idMenuFiltrado.add(idMenu[i]);
            }
        }
        Log.d("MenuActivity", "Opciones filtradas: " + opcionesFiltradas);
        Log.d("MenuActivity", "Iconos filtrados: " + iconosFiltrados);

        setContentView(R.layout.activity_menu);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new MenuAdapter(this, opcionesFiltradas.toArray(new String[0]), iconosFiltrados.stream().mapToInt(i -> i).toArray()));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                SharedPreferences.Editor editor = preferencias.edit();
                String idOpcionSeleccionada = idMenuFiltrado.get(position);
                editor.putString("id_opcion", idOpcionSeleccionada);
                editor.apply();
                String nombreClase = ACTIVIDADES[getPosicion(idOpcionSeleccionada)];
                Class<?> clase = Class.forName("sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar." + nombreClase);
                Intent inte = new Intent(this, clase);
                this.startActivity(inte);
            }
            catch (ClassNotFoundException exp) {
                exp.printStackTrace();
            }
        });

        TextView username = (TextView) findViewById(R.id.tvUsername);
        username.setText(preferencias.getString("user_name", null));

        Button llenarBD = (Button) findViewById(R.id.llenarDB);
        llenarBD.setOnClickListener(v -> {
            llenarBD.setEnabled(false);
            ControlBD controlBD = new ControlBD(this);
            controlBD.llenarDB();
        });
    }

    private int[] getIconos(String [] nombreRecurso) {
        int[] iconos = new int[nombreRecurso.length];
        for (int i = 0; i < iconos.length; i++) {
            iconos[i] = getResources().getIdentifier(nombreRecurso[i], "drawable", getPackageName());
        }
        return iconos;
    }

    private int getPosicion(String id_menu) {
        String [] ids = getResources().getStringArray(R.array.id_menu);
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(id_menu)) {
                Log.d("MENU - POSITION", "getPosicion: " + i);
                return i;
            }
        }
        return -1;
    }
}
