package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class SucursalFarmaciaActivity extends AppCompatActivity {

    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private SucursalFarmaciaDAO sucursalFarmaciaDAO;
    private DireccionDAO direccionDAO;

    private ListView lista;
    private ArrayAdapter<SucursalFarmacia> adapter;
    private List<SucursalFarmacia> sucursales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sucursal_farmacia);

        sucursalFarmaciaDAO = new SucursalFarmaciaDAO(this);
        direccionDAO = new DireccionDAO(this);

        lista = findViewById(R.id.lvBranch);

        Button boton = findViewById(R.id.btnAddBranch);
        TextView txtBusqueda = findViewById(R.id.txtBusquedaSucursal);
        Button botonBuscar = findViewById(R.id.btnSearchBranch);

        botonBuscar.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        botonBuscar.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarPorId(id);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        boton.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        lista.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);

        lista.setOnItemClickListener((parent, view, position, id) -> {
            if (vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)) {
                SucursalFarmacia sucursalFarmacia = (SucursalFarmacia) parent.getItemAtPosition(position);
                showOptionsDialog(sucursalFarmacia);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
        });

        boton.setOnClickListener(v -> showAddDialog());

        cargarLista();
    }

    private void cargarLista() {
        sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursalesResponse -> {
            sucursales = sucursalesResponse;
            adapter = new ArrayAdapter<>(SucursalFarmaciaActivity.this, android.R.layout.simple_list_item_1, sucursales);
            runOnUiThread(() -> lista.setAdapter(adapter));
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
        builder.setView(dialogView);

        EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
        Spinner spinneridDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
        EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);

        AlertDialog dialog = builder.create();
        dialog.show();

        direccionDAO.getAllDireccion(direcciones -> {
            sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> {
                List<Direccion> direccionNoAsignadas = new ArrayList<>(direcciones);
                for (Direccion d : direcciones) {
                    for (SucursalFarmacia sf : sucursales) {
                        if (d.getIdDireccion() == sf.getIdDireccion()) {
                            direccionNoAsignadas.remove(d);
                            break;
                        }
                    }
                }
                Direccion seleccion = new Direccion(-1, -1, getString(R.string.select_addres), SucursalFarmaciaActivity.this);
                direccionNoAsignadas.add(0, seleccion);

                ArrayAdapter<Direccion> adapterDireccion = new ArrayAdapter<>(SucursalFarmaciaActivity.this,
                        android.R.layout.simple_spinner_item, direccionNoAsignadas);
                adapterDireccion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                runOnUiThread(() -> spinneridDireccion.setAdapter(adapterDireccion));
            });
        });

        Button botonGuardar = dialog.findViewById(R.id.buttonSave);
        botonGuardar.setOnClickListener(v -> {
            if (spinneridDireccion.getSelectedItemPosition() == 0) {
                Toast.makeText(SucursalFarmaciaActivity.this, getString(R.string.valid_addres), Toast.LENGTH_LONG).show();
                return;
            }
            if (nombreFarma.getText().toString().trim().isEmpty() || idFarmacia.getText().toString().trim().isEmpty()) {
                Toast.makeText(SucursalFarmaciaActivity.this, getString(R.string.completar_Campos), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int idFarma = Integer.parseInt(idFarmacia.getText().toString());
                Direccion idDireccion = (Direccion) spinneridDireccion.getSelectedItem();
                String nombre = nombreFarma.getText().toString();

                SucursalFarmacia sucursal = new SucursalFarmacia(idFarma, idDireccion.getIdDireccion(), nombre, SucursalFarmaciaActivity.this);
                sucursalFarmaciaDAO.addSucursalFarmacia(sucursal, response -> {
                        runOnUiThread(() -> {
                            cargarLista();
                            dialog.dismiss();
                        });
                });
            } catch (Exception e) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });

        Button botonClear = dialog.findViewById(R.id.buttonClear);
        botonClear.setOnClickListener(v -> {
            idFarmacia.setText("");
            spinneridDireccion.setSelection(0);
            nombreFarma.setText("");
        });
    }

    private void showOptionsDialog(SucursalFarmacia sucursalFarmacia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (!vac.validarAcceso(2))
            dialogView.findViewById(R.id.buttonView).setVisibility(View.GONE);
        if (!vac.validarAcceso(3))
            dialogView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
        if (!vac.validarAcceso(4))
            dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            dialog.dismiss();
            viewSucursal(sucursalFarmacia);
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            dialog.dismiss();
            editarFarmacia(sucursalFarmacia);
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            dialog.dismiss();
            deleteSucursalFarmacia(sucursalFarmacia.getIdFarmacia());
        });

        dialog.show();
    }

    private void deleteSucursalFarmacia(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            sucursalFarmaciaDAO.eliminarSucursal(id, response -> {
                    runOnUiThread(this::cargarLista);
            });
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void editarFarmacia(SucursalFarmacia sucursalFarmacia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
        Spinner spinneridDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
        EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);

        idFarmacia.setText(String.valueOf(sucursalFarmacia.getIdFarmacia()));
        nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());
        idFarmacia.setEnabled(false);

        // Declaramos adapterDireccion como arreglo final para que pueda usarse en lambdas
        final ArrayAdapter<Direccion>[] adapterDireccion = new ArrayAdapter[1];

        direccionDAO.getAllDireccion(direcciones -> {
            sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> {
                final Direccion[] actual = { null };
                for (Direccion d : direcciones) {
                    if (d.getIdDireccion() == sucursalFarmacia.getIdDireccion()) {
                        actual[0] = d;
                        break;
                    }
                }
                if (actual[0] == null) actual[0] = new Direccion(-1, -1, getString(R.string.select_addres), this);

                List<Direccion> libres = new ArrayList<>();
                for (Direccion d : direcciones) {
                    if (d.getIdDireccion() == actual[0].getIdDireccion()) continue;
                    boolean usada = false;
                    for (SucursalFarmacia sf : sucursales) {
                        if (sf.getIdDireccion() == d.getIdDireccion()) {
                            usada = true;
                            break;
                        }
                    }
                    if (!usada) libres.add(d);
                }
                libres.add(0, new Direccion(-1, -1, getString(R.string.select_addres), this));
                libres.add(actual[0]);

                adapterDireccion[0] = new ArrayAdapter<>(SucursalFarmaciaActivity.this,
                        android.R.layout.simple_spinner_item, libres);
                adapterDireccion[0].setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                runOnUiThread(() -> {
                    spinneridDireccion.setAdapter(adapterDireccion[0]);
                    spinneridDireccion.setSelection(adapterDireccion[0].getPosition(actual[0]));
                });
            });
        });

        Button botonGuardar = dialog.findViewById(R.id.buttonSave);
        botonGuardar.setOnClickListener(v -> {
            if (spinneridDireccion.getSelectedItemPosition() == 0) {
                Toast.makeText(SucursalFarmaciaActivity.this, getString(R.string.valid_addres), Toast.LENGTH_LONG).show();
                return;
            }
            try {
                int idFarma = Integer.parseInt(idFarmacia.getText().toString());
                Direccion seleccion = (Direccion) spinneridDireccion.getSelectedItem();
                String nombre = nombreFarma.getText().toString();

                SucursalFarmacia actualizado = new SucursalFarmacia(idFarma, seleccion.getIdDireccion(), nombre, this);
                sucursalFarmaciaDAO.updateSucursalFarmacia(actualizado, response -> {
                        runOnUiThread(() -> {
                            cargarLista();
                            dialog.dismiss();
                        });
                });
            } catch (Exception e) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });

        Button botonClear = dialog.findViewById(R.id.buttonClear);
        botonClear.setOnClickListener(v -> {
            if (adapterDireccion[0] != null) {
                spinneridDireccion.setSelection(adapterDireccion[0].getPosition(new Direccion(-1, -1, getString(R.string.select_addres), this)));
            }
            nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());
        });
    }


    private void viewSucursal(SucursalFarmacia sucursalFarmacia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
        Spinner spinnerDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
        EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);

        idFarmacia.setText(String.valueOf(sucursalFarmacia.getIdFarmacia()));
        nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());
        idFarmacia.setEnabled(false);
        nombreFarma.setEnabled(false);
        spinnerDireccion.setEnabled(false);

        direccionDAO.getDireccion(sucursalFarmacia.getIdDireccion(), direccion -> {
            ArrayAdapter<Direccion> adapterDireccion = new ArrayAdapter<>(SucursalFarmaciaActivity.this,
                    android.R.layout.simple_spinner_item, new Direccion[]{direccion});
            adapterDireccion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            runOnUiThread(() -> {
                spinnerDireccion.setAdapter(adapterDireccion);
                spinnerDireccion.setSelection(adapterDireccion.getPosition(direccion));
            });
        });

        dialogView.findViewById(R.id.buttonSave).setVisibility(View.GONE);
        dialogView.findViewById(R.id.buttonClear).setVisibility(View.GONE);
    }

    private void buscarPorId(int id) {
        sucursalFarmaciaDAO.getSucursalFarmacia(id, sucursalFarmacia -> {
            if (sucursalFarmacia != null) {
                runOnUiThread(() -> viewSucursal(sucursalFarmacia));
            } else {
                runOnUiThread(() -> Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
