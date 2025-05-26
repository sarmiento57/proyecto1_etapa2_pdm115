package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;



import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DireccionActivity extends AppCompatActivity {

    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private DireccionDAO direccionDAO;
    private ArrayAdapter<Direccion> adaptadorDireccion;
    private List<Direccion> listaDireccion;
    private ListView listViewDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direccion);

        direccionDAO = new DireccionDAO(this);
        Button btnAgregar = findViewById(R.id.btnAgregarDireccion);

        btnAgregar.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregar.setOnClickListener(v -> showAddDialog());

        TextView txtBusqueda = findViewById(R.id.txtBusquedaDireccion);
        Button botonBuscar = findViewById(R.id.btnBuscarDireccion);
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

        listViewDireccion = findViewById(R.id.lvDireccion);
        listViewDireccion.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        listViewDireccion.setOnItemClickListener((parent, view, position, id) -> {
            if (vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)) {
                Direccion direccion = (Direccion) parent.getItemAtPosition(position);
                showOptionsDialog(direccion);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
        });

        fillList();
    }

    private void fillList() {
        direccionDAO.getAllDireccion(direcciones -> runOnUiThread(() -> {
            listaDireccion = direcciones;
            adaptadorDireccion = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_list_item_1, listaDireccion);
            listViewDireccion.setAdapter(adaptadorDireccion);
        }));
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);

        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);

        spinnerMunicipio.setEnabled(false);
        spinnerDistrito.setEnabled(false);

        // Cargar los departamentos de la base de datos
        direccionDAO.getAllDepartamentos(departamentos -> runOnUiThread(() -> {
            // Asegurándonos de que la lista de departamentos sea un ArrayList para poder agregar elementos
            List<Departamento> departamentosModificables = new ArrayList<>(departamentos);
            Departamento seleccionDepartamento = new Departamento(-1, getString(R.string.select_departamento));
            departamentosModificables.add(0, seleccionDepartamento); // Agregamos el valor inicial al spinner

            ArrayAdapter<Departamento> adapterDepartamentos = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, departamentosModificables);
            adapterDepartamentos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDepartamento.setAdapter(adapterDepartamentos);
        }));

        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Departamento itemSeleccionado = (Departamento) parent.getItemAtPosition(position);
                // Cuando seleccionamos un departamento, cargamos los municipios
                DepartamentoSelected(spinnerMunicipio, itemSeleccionado);
                spinnerDistrito.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMunicipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Municipio itemSeleccionado = (Municipio) parent.getItemAtPosition(position);
                municipioSelected(spinnerDistrito, itemSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (editTextIdDireccion.getText().toString().isEmpty() || editTextDireccionExacta.getText().toString().isEmpty()) {
                Toast.makeText(DireccionActivity.this, getString(R.string.datos_validos), Toast.LENGTH_LONG).show();
                return;
            }
            if (spinnerDepartamento.getSelectedItemPosition() == 0 ||
                    spinnerMunicipio.getSelectedItemPosition() == 0 ||
                    spinnerDistrito.getSelectedItemPosition() == 0) {
                Toast.makeText(DireccionActivity.this, getString(R.string.select_valid_dep_mun_dis), Toast.LENGTH_LONG).show();
                return;
            }
            int id = Integer.parseInt(editTextIdDireccion.getText().toString());
            String direccionExacta = editTextDireccionExacta.getText().toString().trim();
            Distrito distrito = (Distrito) spinnerDistrito.getSelectedItem();

            Direccion direccion = new Direccion(id, distrito.getIdDistrito(), direccionExacta, DireccionActivity.this);
            direccionDAO.addDireccion(direccion, response -> runOnUiThread(() -> {
                    fillList();
                    dialog.dismiss();

            }));
        });

        btnLimpiar.setOnClickListener(v -> {
            editTextIdDireccion.setText("");
            editTextDireccionExacta.setText("");
            spinnerDepartamento.setSelection(0);
            spinnerMunicipio.setSelection(0);
            spinnerMunicipio.setEnabled(false);
            spinnerDistrito.setSelection(0);
            spinnerDistrito.setEnabled(false);
        });

        dialog.show();
    }

    private void DepartamentoSelected(Spinner spinnerMunicipio, Departamento departamento) {
        if (departamento.getIdDepartamento() == -1) {
            spinnerMunicipio.setEnabled(false);
            spinnerMunicipio.setAdapter(null);
            return;
        }
        spinnerMunicipio.setEnabled(true);
        direccionDAO.getAllMunicipios(departamento.getIdDepartamento(), municipios -> runOnUiThread(() -> {
            List<Municipio> municipiosModificables = new ArrayList<>(municipios);
            Municipio seleccionMunicipio = new Municipio(-1, -1, getString(R.string.select_municipio));
            municipiosModificables.add(0, seleccionMunicipio); // Agregamos el valor inicial al spinner

            ArrayAdapter<Municipio> adapterMunicipios = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, municipiosModificables);
            adapterMunicipios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMunicipio.setAdapter(adapterMunicipios);
        }));
    }

    private void municipioSelected(Spinner spinnerDistrito, Municipio municipio) {
        if (municipio.getIdMunicipio() == -1) {
            spinnerDistrito.setEnabled(false);
            spinnerDistrito.setAdapter(null);
            return;
        }
        spinnerDistrito.setEnabled(true);
        direccionDAO.getAllDistritos(municipio.getIdMunicipio(), distritos -> runOnUiThread(() -> {
            List<Distrito> distritosModificables = new ArrayList<>(distritos);
            Distrito seleccionDistrito = new Distrito(-1, -1, getString(R.string.select_distrito));
            distritosModificables.add(0, seleccionDistrito); // Agregamos el valor inicial al spinner

            ArrayAdapter<Distrito> adapterDistritos = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, distritosModificables);
            adapterDistritos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrito.setAdapter(adapterDistritos);
        }));
    }


    private void showOptionsDialog(Direccion direccion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (!vac.validarAcceso(2)) dialogView.findViewById(R.id.buttonView).setVisibility(View.GONE);
        if (!vac.validarAcceso(3)) dialogView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);
        if (!vac.validarAcceso(4)) dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            viewDireccion(direccion);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            dialog.dismiss();
            editDireccion(direccion);
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            dialog.dismiss();
            deleteDireccion(direccion.getIdDireccion());
        });

        dialog.show();
    }

    private void deleteDireccion(int idDireccion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idDireccion);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            direccionDAO.deleteDireccion(idDireccion, response -> runOnUiThread(() -> {
                    fillList();
            }));
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void viewDireccion(Direccion direccion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);
        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);

        editTextIdDireccion.setText(String.valueOf(direccion.getIdDireccion()));
        editTextDireccionExacta.setText(direccion.getDireccionExacta());

        direccionDAO.getDistrito(direccion.getIdDistrito(), dis -> {
            direccionDAO.getMunicipio(dis.getIdMunicipio(), mun -> {
                direccionDAO.getDepartamento(mun.getIdDepartamento(), dep -> runOnUiThread(() -> {
                    editTextIdDireccion.setEnabled(false);
                    editTextIdDireccion.setTextColor(Color.BLACK);
                    editTextDireccionExacta.setEnabled(false);
                    editTextDireccionExacta.setTextColor(Color.BLACK);

                    spinnerDepartamento.setEnabled(false);
                    spinnerMunicipio.setEnabled(false);
                    spinnerDistrito.setEnabled(false);

                    spinnerDepartamento.setAdapter(new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, new Departamento[]{dep}));
                    spinnerMunicipio.setAdapter(new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, new Municipio[]{mun}));
                    spinnerDistrito.setAdapter(new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, new Distrito[]{dis}));

                    btnGuardar.setVisibility(View.GONE);
                    btnLimpiar.setVisibility(View.GONE);

                    dialog.show();
                }));
            });
        });
    }

    private void editDireccion(Direccion direccion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);
        btnLimpiar.setText(getString(R.string.cancel));

        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);

        // Cargar la dirección existente
        editTextIdDireccion.setText(String.valueOf(direccion.getIdDireccion()));
        editTextDireccionExacta.setText(direccion.getDireccionExacta());
        editTextIdDireccion.setEnabled(false);
        editTextIdDireccion.setTextColor(Color.BLACK);
        editTextDireccionExacta.setEnabled(true);

        // Obtener y cargar los valores del distrito, municipio y departamento
        direccionDAO.getDistrito(direccion.getIdDistrito(), distrito -> {
            if (distrito == null) return;

            direccionDAO.getMunicipio(distrito.getIdMunicipio(), municipio -> {
                if (municipio == null) return;

                direccionDAO.getAllDepartamentos(departamentos -> {
                    List<Departamento> depList = new ArrayList<>(departamentos);

                    runOnUiThread(() -> {
                        ArrayAdapter<Departamento> adapterDep = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, depList);
                        adapterDep.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDepartamento.setAdapter(adapterDep);

                        // Preseleccionar el departamento correspondiente
                        spinnerDepartamento.setSelection(getIndexById(depList, municipio.getIdDepartamento()));

                        // Cargar municipios al seleccionar un departamento
                        direccionDAO.getAllMunicipios(municipio.getIdDepartamento(), municipios -> {
                            List<Municipio> munList = new ArrayList<>(municipios);

                            runOnUiThread(() -> {
                                ArrayAdapter<Municipio> adapterMun = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, munList);
                                adapterMun.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerMunicipio.setAdapter(adapterMun);

                                // Preseleccionar el municipio correspondiente
                                spinnerMunicipio.setSelection(getIndexById(munList, municipio.getIdMunicipio()));

                                // Cargar los distritos según el municipio
                                direccionDAO.getAllDistritos(municipio.getIdMunicipio(), distritos -> {
                                    List<Distrito> disList = new ArrayList<>(distritos);

                                    runOnUiThread(() -> {
                                        ArrayAdapter<Distrito> adapterDis = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, disList);
                                        adapterDis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerDistrito.setAdapter(adapterDis);

                                        // Seleccionar el distrito correcto en el Spinner
                                        spinnerDistrito.setSelection(getIndexById(disList, direccion.getIdDistrito()));
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });

        // Función para manejar el cambio en el Spinner de Departamento
        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Departamento seleccionado = (Departamento) parent.getItemAtPosition(position);
                if (seleccionado.getIdDepartamento() != -1) {
                    // Cargar municipios de este departamento
                    DepartamentoSelected(spinnerMunicipio, seleccionado);
                    spinnerDistrito.setEnabled(false); // Deshabilitar el distrito hasta que se seleccione el municipio
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Función para manejar el cambio en el Spinner de Municipio
        spinnerMunicipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Municipio seleccionado = (Municipio) parent.getItemAtPosition(position);
                if (seleccionado.getIdMunicipio() != -1) {
                    // Cargar distritos de este municipio
                    municipioSelected(spinnerDistrito, seleccionado);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Botón para guardar los cambios
        btnGuardar.setOnClickListener(v -> {
            int idDireccion = Integer.parseInt(editTextIdDireccion.getText().toString());
            String direccionExacta = editTextDireccionExacta.getText().toString().trim();
            Departamento departamento = (Departamento) spinnerDepartamento.getSelectedItem();
            Municipio municipio = (Municipio) spinnerMunicipio.getSelectedItem();
            Distrito distrito = (Distrito) spinnerDistrito.getSelectedItem();

            // Verificar que los campos sean correctos antes de guardar
            if (departamento.getIdDepartamento() == -1 || municipio.getIdMunicipio() == -1 || distrito.getIdDistrito() == -1 || direccionExacta.isEmpty()) {
                Toast.makeText(this, getString(R.string.completar_Campos), Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear la nueva dirección y actualizarla
            Direccion nuevaDireccion = new Direccion(idDireccion, distrito.getIdDistrito(), direccionExacta, this);
            direccionDAO.updateDireccion(nuevaDireccion, response -> runOnUiThread(() -> {
                fillList();  // Actualizar la lista después de la edición
                dialog.dismiss();
            }));
        });

        // Botón para cancelar y cerrar el diálogo
        btnLimpiar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private <T> int getIndexById(List<T> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (item instanceof Departamento && ((Departamento) item).getIdDepartamento() == id) {
                return i;
            } else if (item instanceof Municipio && ((Municipio) item).getIdMunicipio() == id) {
                return i;
            } else if (item instanceof Distrito && ((Distrito) item).getIdDistrito() == id) {
                return i;
            }
        }
        return 0;
    }



    private void buscarPorId(int id) {
        direccionDAO.getDireccion(id, direccion -> runOnUiThread(() -> {
            if (direccion != null) {
                viewDireccion(direccion);
            } else {
                Toast.makeText(DireccionActivity.this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            }
        }));
    }
}


