package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class ViaAdministracionActivity extends AppCompatActivity {
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private ViaAdministracionDAO viaAdministracionDAO;
    private ArrayAdapter<ViaAdministracion> adaptadorViaAdministracion;
    private List<ViaAdministracion> listaViaAdministracion;
    private ListView listViewViaAdministracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_via_administracion);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        viaAdministracionDAO = new ViaAdministracionDAO(conexionDB, this);

        TextView txtBusqueda = findViewById(R.id.busquedaViaDeAdministracion);
        Button btnAgregarViaAdministracion = findViewById(R.id.btnAgregarViaAdministracion);
        btnAgregarViaAdministracion.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarViaAdministracion.setOnClickListener(v -> showAddDialog());

        Button btnBuscarViaAdministracionPorId = findViewById(R.id.btnBuscarViaAdministracion);
        btnBuscarViaAdministracionPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarViaAdministracionPorId.setOnClickListener(v -> {
            try {
                String id = txtBusqueda.getText().toString().trim();
                int idViaAdministracion = Integer.parseInt(id);
                buscarViaAdministracionPorId(idViaAdministracion);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewViaAdministracion = findViewById(R.id.lvViaAdministracion);
        listViewViaAdministracion.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        llenarLista();
        listViewViaAdministracion.setOnItemClickListener((parent, view, position, id) -> {
            ViaAdministracion viaAdministracion = (ViaAdministracion) parent.getItemAtPosition(position);
            showOptionsDialog(viaAdministracion);
        });
    }

    private void llenarLista() {
        listaViaAdministracion = viaAdministracionDAO.getAllViaAdministraciones();
        adaptadorViaAdministracion = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaViaAdministracion);
        listViewViaAdministracion.setAdapter(adaptadorViaAdministracion);
    }

    private void showOptionsDialog(final ViaAdministracion viaAdministracion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2))
                verViaAdministracion(viaAdministracion);
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3))
                editarViaAdministracion(viaAdministracion);
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4))
                eliminarViaAdministracion(viaAdministracion.getIdViaAdministracion());
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View view = getLayoutInflater().inflate(R.layout.dialog_via_administracion, null);
        builder.setView(view);

        EditText etIdViaAdministracion = view.findViewById(R.id.editTextIdViaAdministracion);
        EditText etTipoAdministracion = view.findViewById(R.id.editTextTipoAdministracion);
        Button btnGuardar = view.findViewById(R.id.btnGuardarViaAdministracion);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarViaAdministracion);

        AlertDialog dialog = builder.create();
        btnGuardar.setOnClickListener(v -> {
            List<View> vistas = Arrays.asList(etIdViaAdministracion, etTipoAdministracion);
            List<String> regex = Arrays.asList("^\\d+$", "^[\\s\\S]{1,}$");
            List<Integer> mensajesError = Arrays.asList(R.string.only_numbers, R.string.required_field);
            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, mensajesError);
            if (validador.validarCampos()) {
                int idViaAdministracion = Integer.parseInt(etIdViaAdministracion.getText().toString().trim());
                String tipoAdministracion = etTipoAdministracion.getText().toString().trim();
                ViaAdministracion viaAdministracion = new ViaAdministracion(idViaAdministracion, tipoAdministracion, this);
                viaAdministracionDAO.addViaAdministracion(viaAdministracion);
                llenarLista();
                dialog.dismiss();
            }
        });

        btnLimpiar.setOnClickListener(v -> {
            etIdViaAdministracion.setText("");
            etTipoAdministracion.setText("");
        });

        dialog.show();
    }

    private void verViaAdministracion(ViaAdministracion viaAdministracion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View view = getLayoutInflater().inflate(R.layout.dialog_via_administracion, null);
        builder.setView(view);

        EditText etIdViaAdministracion = view.findViewById(R.id.editTextIdViaAdministracion);
        EditText etTipoAdministracion = view.findViewById(R.id.editTextTipoAdministracion);

        etIdViaAdministracion.setText(String.valueOf(viaAdministracion.getIdViaAdministracion()));
        etTipoAdministracion.setText(viaAdministracion.getTipoAdministracion());

        etIdViaAdministracion.setEnabled(false);
        etTipoAdministracion.setEnabled(false);

        view.findViewById(R.id.btnGuardarViaAdministracion).setVisibility(View.GONE);
        view.findViewById(R.id.btnLimpiarViaAdministracion).setVisibility(View.GONE);

        builder.create().show();
    }

    private void editarViaAdministracion(ViaAdministracion viaAdministracion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View view = getLayoutInflater().inflate(R.layout.dialog_via_administracion, null);
        builder.setView(view);

        EditText etIdViaAdministracion = view.findViewById(R.id.editTextIdViaAdministracion);
        EditText etTipoAdministracion = view.findViewById(R.id.editTextTipoAdministracion);

        etIdViaAdministracion.setText(String.valueOf(viaAdministracion.getIdViaAdministracion()));
        etTipoAdministracion.setText(viaAdministracion.getTipoAdministracion());

        etIdViaAdministracion.setEnabled(false);

        Button btnGuardar = view.findViewById(R.id.btnGuardarViaAdministracion);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarViaAdministracion);
        btnLimpiar.setVisibility(View.GONE);

        AlertDialog dialog = builder.create();
        btnGuardar.setOnClickListener(v -> {
            List<View> vistas = Arrays.asList(etTipoAdministracion);
            List<String> regex = Arrays.asList("^[\\s\\S]{1,}$");
            List<Integer> mensajesError = Arrays.asList(R.string.required_field);
            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, mensajesError);
            if (validador.validarCampos()) {
                viaAdministracion.setTipoAdministracion(etTipoAdministracion.getText().toString().trim());
                viaAdministracionDAO.updateViaAdministracion(viaAdministracion);
                llenarLista();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void buscarViaAdministracionPorId(int idViaAdministracion) {
        ViaAdministracion viaAdministracion = viaAdministracionDAO.getViaAdministracion(idViaAdministracion);
        if (viaAdministracion != null) {
            verViaAdministracion(viaAdministracion);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarViaAdministracion(int idViaAdministracion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idViaAdministracion);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            viaAdministracionDAO.deleteViaAdministracion(idViaAdministracion);
            llenarLista(); // Refresh the ListView
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}