package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import static android.view.View.GONE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CategoriaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private CategoriaDAO categoriaDAO;
    private ArrayAdapter<Categoria> adaptador;
    private List<Categoria> values = new ArrayList<>();
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria);
        //Conexion a la db
        SQLiteDatabase conection = new ControlBD(this).getConnection();
        categoriaDAO = new CategoriaDAO(conection, this);

        TextView txtBusqueda = (TextView) findViewById(R.id.busquedaCategoria);


        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        ListView listV = (ListView) findViewById(R.id.categoryListv);
        listV.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        listV.setAdapter(adaptador);
        actualizarListView();
        listV.setOnItemClickListener(this);
        Button btnInsertarCategoria = (Button) findViewById(R.id.btnAgregarCategoria);
        btnInsertarCategoria.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnInsertarCategoria.setOnClickListener(v -> {
            showAddDialog();
        });

        Button btnBuscarCategoria = findViewById(R.id.btnBuscarCategoria);
        btnBuscarCategoria.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarCategoria.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarCategoriaPorID(id);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showAddDialog() {

        int numRegistros = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_categoria, null);
        builder.setView(dialogView);

        EditText idNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaId);
        EditText nameNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaNombre);
        Button btnSaveCategoria = dialogView.findViewById(R.id.btnGuardarCategoria);
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarNewCategoria);

        EditText[] campos = {idNewCategoria, nameNewCategoria};

        AlertDialog dialog = builder.create();

        Cursor cursor = categoriaDAO.getDbConection().rawQuery("SELECT COUNT(*) FROM CATEGORIA", null);
        if (cursor.moveToFirst()) {
            numRegistros = cursor.getInt(0);
        }
        idNewCategoria.setText(Integer.toString(numRegistros + 1));


        btnClear.setOnClickListener(v -> {
            idNewCategoria.setText("");
            nameNewCategoria.setText("");
        });

        btnSaveCategoria.setOnClickListener(v -> {
            if (!areFieldsEmpty(campos)) {
                int id = Integer.parseInt(String.valueOf(idNewCategoria.getText()));
                String name = String.valueOf(nameNewCategoria.getText());
                Categoria cat = new Categoria(id, name);
                boolean exito = categoriaDAO.insertarCategoria(cat);
                if (exito) {
                    dialog.dismiss();
                }
                actualizarListView();
            }
        });
        dialog.show();


    }

    public void showOptionsDialog(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        Button btnView = dialogView.findViewById(R.id.buttonView);
        Button btnUpdate = dialogView.findViewById(R.id.buttonEdit);
        Button btnDelete = dialogView.findViewById(R.id.buttonDelete);

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    verCategoria(categoria);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(3))
                    editCategoria(categoria, dialog);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(4))
                    eliminarCategoria(categoria, dialog);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void actualizarListView() {
        if (!values.isEmpty()) {
            values.clear();
        }
        values.addAll(categoriaDAO.getAllRows());
        adaptador.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Categoria categoria = (Categoria) parent.getItemAtPosition(position);
        Log.d("Seleccionado", categoria.getNombreCategoria());
        showOptionsDialog(categoria);
    }

    public void verCategoria(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_categoria, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText idNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaId);
        EditText nameNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaNombre);
        Button btnSaveCategoria = dialogView.findViewById(R.id.btnGuardarCategoria);
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarNewCategoria);

        idNewCategoria.setText(Integer.toString(categoria.getIdCategoria()));
        idNewCategoria.setEnabled(false);
        nameNewCategoria.setText(categoria.getNombreCategoria());
        nameNewCategoria.setEnabled(false);
        btnSaveCategoria.setVisibility(GONE);
        btnClear.setVisibility(GONE);

        dialog.show();
    }

    public void editCategoria(Categoria categoria, AlertDialog dialogoPadre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_categoria, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText idNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaId);
        EditText nameNewCategoria = dialogView.findViewById(R.id.editTextNewCategoriaNombre);
        Button btnSaveCategoria = dialogView.findViewById(R.id.btnGuardarCategoria);
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarNewCategoria);

        EditText[] campos = {idNewCategoria, nameNewCategoria};

        idNewCategoria.setText(Integer.toString(categoria.getIdCategoria()));
        idNewCategoria.setEnabled(false);
        nameNewCategoria.setText(categoria.getNombreCategoria());

        btnSaveCategoria.setOnClickListener(v -> {
            if (!areFieldsEmpty(campos)) {
                String nombreEditado = String.valueOf(nameNewCategoria.getText());
                categoria.setNombreCategoria(nombreEditado);
                boolean exito = categoriaDAO.updateCategoria(categoria);
                if (exito) {
                    actualizarListView();
                    dialog.dismiss();
                    dialogoPadre.dismiss();
                } else {
                    Log.d("UPDATE_FAIL", "No se actualizo");
                }
            }
        });

        dialog.show();
    }

    public void eliminarCategoria(Categoria categoria, AlertDialog dialogoPadre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView advertencia = dialogView.findViewById(R.id.confirmationText);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirm);
        Button btnCancelar = dialogView.findViewById(R.id.btnDecline);

        advertencia.setText(getString(R.string.confirmation_message) + " " + getString(R.string.category).toLowerCase() +
                " ID: " + categoria.getIdCategoria() + "\n" + getString(R.string.confirm_delete));

        btnConfirmar.setText(getString(R.string.yes));
        btnCancelar.setText(getString(R.string.no));

        btnConfirmar.setOnClickListener(v -> {
            int filasAfectadas = categoriaDAO.deleteCategoria(categoria);
            if (filasAfectadas > 0) {
                actualizarListView();
                Toast.makeText(this, getString(R.string.delete_message) + ": " + getString(R.string.category) +
                        " ID: " + categoria.getIdCategoria(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                dialogoPadre.dismiss();
            } else {

            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    public boolean areFieldsEmpty(EditText[] campos) {
        boolean hayVacios = false;
        for (EditText campo : campos) {
            if (campo.getText().toString().trim().isEmpty()) {
                campo.setError(getString(R.string.emptyWarning));
                hayVacios = true;
            }
        }
        return hayVacios;
    }

    private void buscarCategoriaPorID(int id) {
        Categoria categoria = categoriaDAO.getCategoria(id);
        if(categoria != null) {
            verCategoria(categoria);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }
}