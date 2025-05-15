package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;


public class ProveedorActivity extends AppCompatActivity {

    private ProveedorDAO proveedorDAO;
    private ArrayAdapter<Proveedor> adapterProveedores;
    private List<Proveedor> listaProveedores;
    private ListView listViewProveedores;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_proveedor);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        proveedorDAO = new ProveedorDAO(conexionDB, this);

        TextView txtBusqueda = findViewById(R.id.txtBusquedaProveedor);

        Button btnBuscarProveedor = findViewById(R.id.btnBuscarProveedor);
        btnBuscarProveedor.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarProveedor.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarProveedorPorId(id);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewProveedores = findViewById(R.id.listViewProveedores);
        listViewProveedores.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        listViewProveedores.setOnItemClickListener((parent, view, position, id) -> {
            Proveedor proveedor = (Proveedor) parent.getItemAtPosition(position);
            showOptionsDialog(proveedor);
        });

        Button btnAgregarProveedor = findViewById(R.id.btnAddProveedor);
        btnAgregarProveedor.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarProveedor.setOnClickListener(v -> showAddDialog());
    }

    private void fillList() {
        listaProveedores = proveedorDAO.getAllProveedores();
        adapterProveedores = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaProveedores);
        listViewProveedores.setAdapter(adapterProveedores);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_proveedor, null);
        builder.setView(dialogView);

        EditText edtId = dialogView.findViewById(R.id.edtId);
        EditText edtNombre = dialogView.findViewById(R.id.edtNombre);
        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefono);
        EditText edtDireccion = dialogView.findViewById(R.id.edtDireccion);
        EditText edtRubro = dialogView.findViewById(R.id.edtRubro);
        EditText edtNumReg = dialogView.findViewById(R.id.edtNumReg);
        EditText edtNIT = dialogView.findViewById(R.id.edtNIT);
        EditText edtGiro = dialogView.findViewById(R.id.edtGiro);

        Button btnGuardarProveedor = dialogView.findViewById(R.id.btnGuardarProveedor);
        Button btnLimpiarProveedor = dialogView.findViewById(R.id.btnLimpiarProveedor);


        List<View> vistas = Arrays.asList(edtId, edtNombre, edtTelefono, edtDireccion, edtRubro, edtNumReg, edtNIT, edtGiro);
        List<String> regex = Arrays.asList(
                "\\d+","[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", "\\d+", "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$",
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$",
                "\\d{4}-\\d{6}-\\d{3}-\\d", "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"
        );
        List<Integer> errores = Arrays.asList(
                R.string.only_letters, R.string.only_letters, R.string.only_numbers, R.string.only_letters_and_numbers,
                R.string.only_letters, R.string.only_letters_and_numbers,
                R.string.nit_format, R.string.only_letters
        );

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, regex, errores);

        final AlertDialog dialog = builder.create();

        btnGuardarProveedor.setOnClickListener(v -> {
            if(validadorDeCampos.validarCampos()){
                saveProveedor(edtId, edtNombre, edtTelefono, edtDireccion, edtRubro, edtNumReg, edtNIT, edtGiro);
                dialog.dismiss();
            }
        });
        btnLimpiarProveedor.setOnClickListener(v -> clearFieldsProveedor(edtId, edtNombre, edtTelefono, edtDireccion,
                edtRubro, edtNumReg, edtNIT, edtGiro));
        dialog.show();
    }

    private void saveProveedor(EditText edtId, EditText edtNombre, EditText edtTelefono, EditText edtDireccion, EditText edtRubro,
                               EditText edtNumReg, EditText edtNIT, EditText edtGiro) {
        int id = Integer.parseInt(edtId.getText().toString());
        String nombre = edtNombre.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String direccion = edtDireccion.getText().toString().trim();
        String rubro = edtRubro.getText().toString().trim();
        String numRegistro = edtNumReg.getText().toString().trim();
        String nit = edtNIT.getText().toString().trim();
        String giro = edtGiro.getText().toString().trim();

        Proveedor proveedor = new Proveedor(id, nombre, telefono, direccion, rubro, numRegistro, nit, giro, this);
        proveedorDAO.addProveedor(proveedor);
        fillList();

        clearFieldsProveedor(edtId, edtNombre, edtTelefono, edtDireccion, edtRubro, edtNumReg, edtNIT, edtGiro);
    }

    private void showOptionsDialog(Proveedor proveedor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    viewProveedor(proveedor);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(3)) {
                    dialog.dismiss();
                    editProveedor(proveedor);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vac.validarAcceso(4)) {
                    deleteProveedor(proveedor.getIdProveedor());
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void viewProveedor(Proveedor proveedor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);

        View view = getLayoutInflater().inflate(R.layout.dialogo_proveedor, null);
        builder.setView(view);

        EditText edtId = view.findViewById(R.id.edtId);
        EditText edtNombre = view.findViewById(R.id.edtNombre);
        EditText edtTelefono = view.findViewById(R.id.edtTelefono);
        EditText edtDireccion = view.findViewById(R.id.edtDireccion);
        EditText edtRubro = view.findViewById(R.id.edtRubro);
        EditText edtNumReg = view.findViewById(R.id.edtNumReg);
        EditText edtNIT = view.findViewById(R.id.edtNIT);
        EditText edtGiro = view.findViewById(R.id.edtGiro);

        edtId.setText(String.valueOf(proveedor.getIdProveedor()));
        edtNombre.setText(proveedor.getNombreProveedor());
        edtTelefono.setText(proveedor.getTelefonoProveedor());
        edtDireccion.setText(proveedor.getDireccionProveedor());
        edtRubro.setText(proveedor.getRubroProveedor());
        edtNumReg.setText(proveedor.getNumRegProveedor());
        edtNIT.setText(proveedor.getNitProveedor());
        edtGiro.setText(proveedor.getGiroProveedor());

        for (EditText field : Arrays.asList(edtId, edtNombre, edtTelefono, edtDireccion, edtRubro, edtNumReg, edtNIT, edtGiro)) {
            field.setEnabled(false);
        }

        view.findViewById(R.id.btnGuardarProveedor).setVisibility(View.GONE);
        view.findViewById(R.id.btnLimpiarProveedor).setVisibility(View.GONE);

        builder.show();
    }

    private void editProveedor(Proveedor proveedor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);

        View view = getLayoutInflater().inflate(R.layout.dialogo_proveedor, null);
        builder.setView(view);

        EditText edtId = view.findViewById(R.id.edtId);
        EditText edtNombre = view.findViewById(R.id.edtNombre);
        EditText edtTelefono = view.findViewById(R.id.edtTelefono);
        EditText edtDireccion = view.findViewById(R.id.edtDireccion);
        EditText edtRubro = view.findViewById(R.id.edtRubro);
        EditText edtNumReg = view.findViewById(R.id.edtNumReg);
        EditText edtNIT = view.findViewById(R.id.edtNIT);
        EditText edtGiro = view.findViewById(R.id.edtGiro);

        edtId.setText(String.valueOf(proveedor.getIdProveedor()));
        edtId.setEnabled(false);
        edtNombre.setText(proveedor.getNombreProveedor());
        edtTelefono.setText(proveedor.getTelefonoProveedor());
        edtDireccion.setText(proveedor.getDireccionProveedor());
        edtRubro.setText(proveedor.getRubroProveedor());
        edtNumReg.setText(proveedor.getNumRegProveedor());
        edtNIT.setText(proveedor.getNitProveedor());
        edtGiro.setText(proveedor.getGiroProveedor());

        Button btnGuardar = view.findViewById(R.id.btnGuardarProveedor);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarProveedor);
        btnLimpiar.setEnabled(false);

        List<View> vistas = Arrays.asList(edtNombre, edtTelefono, edtDireccion, edtRubro, edtNumReg, edtNIT, edtGiro);
        List<String> regex = Arrays.asList(
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", "\\d+", "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$",
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$",
                "\\d{4}-\\d{6}-\\d{3}-\\d", "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"
        );
        List<Integer> errores = Arrays.asList(
                R.string.only_letters, R.string.only_numbers, R.string.only_letters_and_numbers,
                R.string.only_letters, R.string.only_letters_and_numbers,
                R.string.nit_format, R.string.only_letters
        );

        ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, errores);

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (validador.validarCampos()) {
                proveedor.setNombreProveedor(edtNombre.getText().toString());
                proveedor.setTelefonoProveedor(edtTelefono.getText().toString());
                proveedor.setDireccionProveedor(edtDireccion.getText().toString());
                proveedor.setRubroProveedor(edtRubro.getText().toString());
                proveedor.setNumRegProveedor(edtNumReg.getText().toString());
                proveedor.setNitProveedor(edtNIT.getText().toString());
                proveedor.setGiroProveedor(edtGiro.getText().toString());
                proveedorDAO.updateProveedor(proveedor);
                fillList();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteProveedor(int idProveedor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idProveedor);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            proveedorDAO.deleteProveedor(idProveedor);
            fillList();
        });

        builder.setNegativeButton(R.string.no, ((dialog, which) -> dialog.dismiss()));

        builder.create().show();
    }

    private void buscarProveedorPorId(int id) {
        Proveedor proveedor = proveedorDAO.getProveedorById(id);
        if(proveedor != null) {
            viewProveedor(proveedor);
        }
        else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFieldsProveedor(EditText edtId, EditText edtNombre, EditText edtTelefono, EditText edtDireccion, EditText edtRubro,
                                      EditText edtNumReg, EditText edtNIT, EditText edtGiro) {
        edtId.setText("");
        edtNombre.setText("");
        edtTelefono.setText("");
        edtDireccion.setText("");
        edtRubro.setText("");
        edtNumReg.setText("");
        edtNIT.setText("");
        edtGiro.setText("");
    }
}
