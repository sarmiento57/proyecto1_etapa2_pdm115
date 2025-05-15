package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;

public class FormaFarmaceuticaActivity extends AppCompatActivity {

    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private FormaFarmaceuticaDAO formaFarmaceuticaDAO;
    private ArrayAdapter<FormaFarmaceutica> adaptadorFormaFarmaceutica;
    private List<FormaFarmaceutica> listaFormaFarmaceutica;
    private ListView listViewFormaFaramceutica;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forma_farmaceutica);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        formaFarmaceuticaDAO = new FormaFarmaceuticaDAO(conexionDB, this);

        TextView txtBusqueda = (TextView) findViewById(R.id.busquedaFormaFarmaceutica);

        // Comprobacion Inicial de Permisos de Consulta
        Button btnAgregarFormaFarmaceutica = findViewById(R.id.btnAgregarFormaFarmaceutica);
        btnAgregarFormaFarmaceutica.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarFormaFarmaceutica.setOnClickListener(v -> {showAddDialog();});

        Button btnbuscarFormaFarmaceuticaPorId = findViewById(R.id.btnBuscarFormaFarmaceutica);
        btnbuscarFormaFarmaceuticaPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnbuscarFormaFarmaceuticaPorId.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarFormaFarmaceuticaPorId(id);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewFormaFaramceutica = findViewById(R.id.lvFormaFarmaceutica);
        listViewFormaFaramceutica.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        llenarLista();
        listViewFormaFaramceutica.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FormaFarmaceutica formaFarmaceutica = (FormaFarmaceutica) parent.getItemAtPosition(position);
                showOptionsDialog(formaFarmaceutica);
            }
        });
    }

    private void llenarLista() {
        listaFormaFarmaceutica = formaFarmaceuticaDAO.getAllFormaFarmaceutica();
        adaptadorFormaFarmaceutica = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFormaFarmaceutica);
        listViewFormaFaramceutica.setAdapter(adaptadorFormaFarmaceutica);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forma_farmaceutica, null);
        builder.setView(dialogView);

        EditText editTextIdFormaFarmaceutica = dialogView.findViewById(R.id.editTextIdFormaFarmaceutica);
        EditText editTextTipoFormaFarmaceutica = dialogView.findViewById(R.id.editTextTipoFormaFarmaceutica);
        Button btnGuardarFormaFarmaceutica = dialogView.findViewById(R.id.btnGuardarFormaFarmaceutica);
        Button btnLimpiarFormaFarmaceutica = dialogView.findViewById(R.id.btnLimpiarFormaFarmaceutica);

        final AlertDialog dialog = builder.create();

        btnGuardarFormaFarmaceutica.setOnClickListener(v -> {

            List<View> vistas = Arrays.asList(editTextIdFormaFarmaceutica, editTextTipoFormaFarmaceutica);
            List<String> listaRegex = Arrays.asList("^\\d+$", "^[a-zA-Z\\s]+$"); // Example regex for number and text validation
            List<Integer> mensajesDeError = Arrays.asList(R.string.only_numbers, R.string.only_letters);

            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

            if (validador.validarCampos()) {
                guardarFormaFarmaceutica(editTextIdFormaFarmaceutica, editTextTipoFormaFarmaceutica);
                dialog.dismiss();
            }
        });

        btnLimpiarFormaFarmaceutica.setOnClickListener(v -> clearFieldsFormaFarmaceutica(editTextIdFormaFarmaceutica, editTextTipoFormaFarmaceutica));
        dialog.show();
    }


    private void showOptionsDialog(final FormaFarmaceutica formaFarmaceutica) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    viewFormaFarmaceutica(formaFarmaceutica);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(3))
                    editFormaFarmaceutica(formaFarmaceutica);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(4))
                    eliminarFormaFarmaceutica(formaFarmaceutica.getIdFormaFarmaceutica());
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void guardarFormaFarmaceutica(EditText editTextIdFormaFarmaceutica, EditText editTextTipoFormaFarmaceutica) {

        int id = Integer.parseInt(editTextIdFormaFarmaceutica.getText().toString());
        String tipo = editTextTipoFormaFarmaceutica.getText().toString().trim();

        FormaFarmaceutica formaFarmaceutica = new FormaFarmaceutica(id, tipo, this);
        formaFarmaceuticaDAO.addFormaFarmaceutica(formaFarmaceutica);
        llenarLista();
        clearFieldsFormaFarmaceutica(editTextIdFormaFarmaceutica, editTextIdFormaFarmaceutica);
    }

    private void clearFieldsFormaFarmaceutica(EditText editTextIdFormaFarmaceutica, EditText editTextTipoFormaFarmaceutica) {
        editTextIdFormaFarmaceutica.setText("");
        editTextTipoFormaFarmaceutica.setText("");
    }

    private void viewFormaFarmaceutica(FormaFarmaceutica formaFarmaceutica) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forma_farmaceutica, null);
        builder.setView(dialogView);

        EditText editTextIdFormaFarmaceutica = dialogView.findViewById(R.id.editTextIdFormaFarmaceutica);
        EditText editTextTipoFormaFarmaceutica = dialogView.findViewById(R.id.editTextTipoFormaFarmaceutica);

        editTextIdFormaFarmaceutica.setText(String.valueOf(formaFarmaceutica.getIdFormaFarmaceutica()));
        editTextTipoFormaFarmaceutica.setText(formaFarmaceutica.getTipoFormaFarmaceutica());

        editTextIdFormaFarmaceutica.setEnabled(false);
        editTextTipoFormaFarmaceutica.setEnabled(false);

        Button btnGuardarFormaFarmaceutica = dialogView.findViewById(R.id.btnGuardarFormaFarmaceutica);
        Button btnLimpiarFormaFarmaceutica = dialogView.findViewById(R.id.btnLimpiarFormaFarmaceutica);

        if (btnGuardarFormaFarmaceutica != null) {
            btnGuardarFormaFarmaceutica.setVisibility(View.GONE);
        }
        if (btnLimpiarFormaFarmaceutica != null) {
            btnLimpiarFormaFarmaceutica.setVisibility(View.GONE);
        }

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editFormaFarmaceutica(FormaFarmaceutica formaFarmaceutica) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forma_farmaceutica, null);
        builder.setView(dialogView);

        EditText editTextIdFormaFarmaceutica = dialogView.findViewById(R.id.editTextIdFormaFarmaceutica);
        EditText editTextTipoFormaFarmaceutica = dialogView.findViewById(R.id.editTextTipoFormaFarmaceutica);

        editTextIdFormaFarmaceutica.setText(String.valueOf(formaFarmaceutica.getIdFormaFarmaceutica()));
        editTextTipoFormaFarmaceutica.setText(formaFarmaceutica.getTipoFormaFarmaceutica());

        editTextIdFormaFarmaceutica.setEnabled(false);

        Button btnGuardarFormaFarmaceutica = dialogView.findViewById(R.id.btnGuardarFormaFarmaceutica);
        Button btnLimpiarFormaFarmaceutica = dialogView.findViewById(R.id.btnLimpiarFormaFarmaceutica);

        btnLimpiarFormaFarmaceutica.setEnabled(false);

        final AlertDialog dialog = builder.create();

        btnGuardarFormaFarmaceutica.setOnClickListener(v -> {
            List<View> vistas = Arrays.asList(editTextTipoFormaFarmaceutica);
            List<String> listaRegex = Arrays.asList("^[a-zA-Z\\s]+$"); // Example regex for text validation
            List<Integer> mensajesDeError = Arrays.asList(R.string.only_letters);

            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

            if (validador.validarCampos()) {
                formaFarmaceutica.setTipoFormaFarmaceutica(editTextTipoFormaFarmaceutica.getText().toString().trim());
                formaFarmaceuticaDAO.updateFormaFarmaceutica(formaFarmaceutica);
                llenarLista(); // Refresh the ListView
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void eliminarFormaFarmaceutica(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            formaFarmaceuticaDAO.deleteFormaFarmaceutica(id);
            llenarLista();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void buscarFormaFarmaceuticaPorId(int id) {
        FormaFarmaceutica formaFarmaceutica = formaFarmaceuticaDAO.getFormaFarmaceutica(id);
        if(formaFarmaceutica != null) {
            viewFormaFarmaceutica(formaFarmaceutica);
        } 
        else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }
}
