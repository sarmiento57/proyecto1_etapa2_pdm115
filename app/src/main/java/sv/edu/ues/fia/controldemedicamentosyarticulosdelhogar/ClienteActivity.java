package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    private ListView listViewClientes;

    private ClienteDAO clienteDAO;
    private ArrayAdapter<Cliente> adapterClientes;
    private List<Cliente> listaClientes;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        clienteDAO = new ClienteDAO(conexionDB, this);


        TextView txtBusquedaCliente = findViewById(R.id.txtBusquedaCliente);
        Button btnBuscarCliente = findViewById(R.id.btnBuscarCliente);
        btnBuscarCliente.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        if (btnBuscarCliente != null) {
            btnBuscarCliente.setOnClickListener(v -> {
                try {
                    if (txtBusquedaCliente.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, R.string.field_empty, Toast.LENGTH_LONG).show();
                        return;
                    }
                    int id = Integer.parseInt(txtBusquedaCliente.getText().toString().trim());
                    buscarClientePorId(id);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
                }
            });
        }

        listViewClientes = findViewById(R.id.listViewClientes);
        listViewClientes.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        listViewClientes.setOnItemClickListener((parent, view, position, id) -> {
            Cliente clienteSeleccionado = (Cliente) parent.getItemAtPosition(position);
            showOptionsDialog(clienteSeleccionado);
        });

        Button btnAddCliente = findViewById(R.id.btnAddCliente);
        btnAddCliente.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAddCliente.setOnClickListener(v -> showAddDialog());
    }

    private void fillList() {
        listaClientes = clienteDAO.getAllClientes();
        adapterClientes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaClientes);
        listViewClientes.setAdapter(adapterClientes);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_client_dialog_title);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_cliente, null);
        builder.setView(dialogView);

        final EditText edtIdCliente = dialogView.findViewById(R.id.edtIdCliente);
        final EditText edtNombreCliente = dialogView.findViewById(R.id.edtNombreCliente);
        final EditText edtTelefonoCliente = dialogView.findViewById(R.id.edtTelefonoCliente);
        final EditText edtCorreoCliente = dialogView.findViewById(R.id.edtCorreoCliente);

        edtIdCliente.setEnabled(true);

        Button btnGuardarCliente = dialogView.findViewById(R.id.btnGuardarCliente);
        Button btnLimpiarCliente = dialogView.findViewById(R.id.btnLimpiarCliente);


        List<View> vistas = Arrays.asList(edtIdCliente, edtNombreCliente, edtTelefonoCliente, edtCorreoCliente);
        List<String> regex = Arrays.asList(
                "\\d+", // ID
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+", // Name
                "\\d{8}", // Phone
                Patterns.EMAIL_ADDRESS.pattern()
        );
        List<Integer> errores = Arrays.asList(
                R.string.only_numbers,
                R.string.only_letters,
                R.string.invalid_phone_format,
                R.string.invalid_email_format
        );
        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, regex, errores);

        final AlertDialog dialog = builder.create();


        if (btnGuardarCliente != null) {
            btnGuardarCliente.setOnClickListener(v -> {
                if (validadorDeCampos.validarCampos()) {
                    saveCliente(edtIdCliente, edtNombreCliente, edtTelefonoCliente, edtCorreoCliente);
                    dialog.dismiss();
                }
            });
        }

        if (btnLimpiarCliente != null) {
            btnLimpiarCliente.setOnClickListener(v -> clearFieldsCliente(edtIdCliente, edtNombreCliente, edtTelefonoCliente, edtCorreoCliente));
        }
        dialog.show();
    }

    private void saveCliente(EditText edtIdCliente, EditText edtNombreCliente, EditText edtTelefonoCliente, EditText edtCorreoCliente) {
        try {
            int id = Integer.parseInt(edtIdCliente.getText().toString().trim());
            String nombre = edtNombreCliente.getText().toString().trim();
            String telefono = edtTelefonoCliente.getText().toString().trim();
            String correo = edtCorreoCliente.getText().toString().trim();

            Cliente nuevoCliente = new Cliente(id, nombre, telefono, correo, this);
            clienteDAO.addCliente(nuevoCliente);
            fillList();

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_id_format, Toast.LENGTH_SHORT).show();
        }
    }


    private void showOptionsDialog(final Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);


        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2)) viewCliente(cliente);
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3)) editCliente(cliente);
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4)) deleteCliente(cliente.getIdCliente());
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void viewCliente(Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view_client_dialog_title);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_cliente, null);
        builder.setView(dialogView);

        final EditText edtIdCliente = dialogView.findViewById(R.id.edtIdCliente);
        final EditText edtNombreCliente = dialogView.findViewById(R.id.edtNombreCliente);
        final EditText edtTelefonoCliente = dialogView.findViewById(R.id.edtTelefonoCliente);
        final EditText edtCorreoCliente = dialogView.findViewById(R.id.edtCorreoCliente);

        edtIdCliente.setText(String.valueOf(cliente.getIdCliente()));
        edtNombreCliente.setText(cliente.getNombreCliente());
        edtTelefonoCliente.setText(cliente.getTelefonoCliente());
        edtCorreoCliente.setText(cliente.getCorreoCliente());

        for (EditText field : Arrays.asList(edtIdCliente, edtNombreCliente, edtTelefonoCliente, edtCorreoCliente)) {
            field.setEnabled(false);
        }


        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarCliente);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarCliente);
        if (btnGuardar != null) btnGuardar.setVisibility(View.GONE);
        if (btnLimpiar != null) btnLimpiar.setVisibility(View.GONE);

        builder.setNeutralButton(R.string.close, (d,w) -> d.dismiss());
        builder.create().show();
    }

    private void editCliente(final Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_client_dialog_title);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_cliente, null);
        builder.setView(dialogView);

        final EditText edtIdCliente = dialogView.findViewById(R.id.edtIdCliente);
        final EditText edtNombreCliente = dialogView.findViewById(R.id.edtNombreCliente);
        final EditText edtTelefonoCliente = dialogView.findViewById(R.id.edtTelefonoCliente);
        final EditText edtCorreoCliente = dialogView.findViewById(R.id.edtCorreoCliente);

        edtIdCliente.setText(String.valueOf(cliente.getIdCliente()));
        edtIdCliente.setEnabled(false);
        edtNombreCliente.setText(cliente.getNombreCliente());
        edtTelefonoCliente.setText(cliente.getTelefonoCliente());
        edtCorreoCliente.setText(cliente.getCorreoCliente());

        Button btnGuardarCliente = dialogView.findViewById(R.id.btnGuardarCliente);
        Button btnLimpiarCliente = dialogView.findViewById(R.id.btnLimpiarCliente);
        if(btnLimpiarCliente != null) btnLimpiarCliente.setEnabled(false);

        List<View> vistas = Arrays.asList(edtNombreCliente, edtTelefonoCliente, edtCorreoCliente);
        List<String> regex = Arrays.asList(
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+",
                "\\d{8}", // Phone
                Patterns.EMAIL_ADDRESS.pattern()
        );
        List<Integer> errores = Arrays.asList(
                R.string.only_letters,
                R.string.invalid_phone_format,
                R.string.invalid_email_format
        );
        ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, errores);

        final AlertDialog dialog = builder.create();

        if (btnGuardarCliente != null) {
            btnGuardarCliente.setOnClickListener(v -> {
                if (validador.validarCampos()) {
                    cliente.setNombreCliente(edtNombreCliente.getText().toString().trim());
                    cliente.setTelefonoCliente(edtTelefonoCliente.getText().toString().trim());
                    cliente.setCorreoCliente(edtCorreoCliente.getText().toString().trim());

                    clienteDAO.updateCliente(cliente);
                    fillList();
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void deleteCliente(int idCliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        Cliente clienteToDelete = clienteDAO.getClienteById(idCliente);
        String clientName = clienteToDelete != null ? clienteToDelete.getNombreCliente() : String.valueOf(idCliente);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + clientName + "?");


        builder.setPositiveButton(R.string.yes, (d, which) -> {
            clienteDAO.deleteCliente(idCliente);
            fillList();
            d.dismiss();
        });
        builder.setNegativeButton(R.string.no, ((d, which) -> d.dismiss()));
        builder.create().show();
    }

    private void buscarClientePorId(int id) {
        Cliente cliente = clienteDAO.getClienteById(id);
        if (cliente != null) {
            viewCliente(cliente);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFieldsCliente(EditText edtId, EditText edtNombre, EditText edtTelefono, EditText edtCorreo) {
        if(edtId!=null) edtId.setText("");
        if(edtNombre!=null) edtNombre.setText("");
        if(edtTelefono!=null) edtTelefono.setText("");
        if(edtCorreo!=null) edtCorreo.setText("");
        if(edtId!=null) edtId.requestFocus();
    }
}
