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

public class MarcaActivity extends AppCompatActivity {
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private MarcaDAO marcaDAO;
    private ArrayAdapter<Marca> adaptadorMarcas;
    private List<Marca> listaMarcas;
    private ListView listViewMarcas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marca);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        marcaDAO = new MarcaDAO(conexionDB, this);

        TextView txtBusqueda = findViewById(R.id.busquedahMarca);

        Button btnAgregarMarca = findViewById(R.id.btnAgregarMarca);
        btnAgregarMarca.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarMarca.setOnClickListener(v -> showAddDialog());

        Button btnBuscarMarca = findViewById(R.id.btnBuscarMarca);
        btnBuscarMarca.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarMarca.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarMarcaPorId(id);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewMarcas = findViewById(R.id.lvMarca);
        listViewMarcas.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);

        llenarLista();

        listViewMarcas.setOnItemClickListener((parent, view, position, id) -> {
            Marca marca = (Marca) parent.getItemAtPosition(position);
            showOptionsDialog(marca);
        });
    }

    private void llenarLista() {
        listaMarcas = marcaDAO.getAllMarcas();
        adaptadorMarcas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMarcas);
        listViewMarcas.setAdapter(adaptadorMarcas);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_marca, null);
        builder.setView(dialogView);

        EditText editTextIdMarca = dialogView.findViewById(R.id.edtIdMarca);
        EditText editTextNombreMarca = dialogView.findViewById(R.id.edtNombreMarca);
        Button btnGuardarMarca = dialogView.findViewById(R.id.btnGuardarMarca);
        Button btnLimpiarMarca = dialogView.findViewById(R.id.btnLimpiarMarca);

        List<View> vistas = Arrays.asList(editTextIdMarca, editTextNombreMarca);
        List<String> listaRegex = Arrays.asList("\\d+", "[a-zA-Z ]+");
        List<Integer> mensajesDeError = Arrays.asList(R.string.only_numbers, R.string.only_letters);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();
        btnGuardarMarca.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                guardarMarca(editTextIdMarca, editTextNombreMarca);
                dialog.dismiss();
            }
        });
        btnLimpiarMarca.setOnClickListener(v -> limpiarCampos(editTextIdMarca, editTextNombreMarca));
        dialog.show();
    }

    private void showOptionsDialog(final Marca marca) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2)) viewMarca(marca);
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3)) editarMarca(marca);
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4)) eliminarMarca(marca.getIdMarca());
            else Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void guardarMarca(EditText editTextIdMarca, EditText editTextNombreMarca) {
        int id = Integer.parseInt(editTextIdMarca.getText().toString());
        String nombre = editTextNombreMarca.getText().toString().trim();

        Marca marca = new Marca(id, nombre, this);
        marcaDAO.addMarca(marca);
        llenarLista();
        limpiarCampos(editTextIdMarca, editTextNombreMarca);
    }

    private void limpiarCampos(EditText... fields) {
        for (EditText field : fields) {
            field.setText("");
        }
    }

    private void viewMarca(Marca marca) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_marca, null);
        builder.setView(dialogView);

        EditText editTextIdMarca = dialogView.findViewById(R.id.edtIdMarca);
        EditText editTextNombreMarca = dialogView.findViewById(R.id.edtNombreMarca);

        editTextIdMarca.setText(String.valueOf(marca.getIdMarca()));
        editTextNombreMarca.setText(marca.getNombreMarca());

        editTextIdMarca.setEnabled(false);
        editTextNombreMarca.setEnabled(false);

        Button btnGuardarMarca = dialogView.findViewById(R.id.btnGuardarMarca);
        Button btnLimpiarMarca = dialogView.findViewById(R.id.btnLimpiarMarca);
        if (btnGuardarMarca != null) btnGuardarMarca.setVisibility(View.GONE);
        if (btnLimpiarMarca != null) btnLimpiarMarca.setVisibility(View.GONE);

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editarMarca(Marca marca) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_marca, null);
        builder.setView(dialogView);

        EditText editTextIdMarca = dialogView.findViewById(R.id.edtIdMarca);
        EditText editTextNombreMarca = dialogView.findViewById(R.id.edtNombreMarca);

        editTextIdMarca.setText(String.valueOf(marca.getIdMarca()));
        editTextNombreMarca.setText(marca.getNombreMarca());
        editTextIdMarca.setEnabled(false);

        Button btnGuardarMarca = dialogView.findViewById(R.id.btnGuardarMarca);
        Button btnLimpiarMarca = dialogView.findViewById(R.id.btnLimpiarMarca);
        btnLimpiarMarca.setEnabled(false);

        List<View> vistas = Arrays.asList(editTextNombreMarca);
        List<String> listaRegex = Arrays.asList("[a-zA-Z ]+");
        List<Integer> mensajesDeError = Arrays.asList(R.string.only_letters);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();
        btnGuardarMarca.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                marca.setNombreMarca(editTextNombreMarca.getText().toString().trim());
                marcaDAO.updateMarca(marca);
                llenarLista();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void eliminarMarca(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            marcaDAO.deleteMarca(id);
            llenarLista();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void buscarMarcaPorId(int id) {
        Marca marca = marcaDAO.getMarca(id);
        if (marca != null) viewMarca(marca);
        else Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
    }
}
