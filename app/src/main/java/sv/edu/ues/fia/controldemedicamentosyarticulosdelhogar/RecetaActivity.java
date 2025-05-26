package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class RecetaActivity extends AppCompatActivity {

    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private RecetaDAO recetaDAO;
    private ArrayAdapter<Receta> adaptadorReceta;
    private List<Receta> listaReceta;
    private ListView listverReceta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta);
        recetaDAO = new RecetaDAO(this);

        TextView txtBusqueda = findViewById(R.id.busquedaReceta);

        Button btnAgregarReceta = findViewById(R.id.btnAgregarReceta);
        btnAgregarReceta.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarReceta.setOnClickListener(v -> showAddDialog());

        Button btnBuscarRecetaPorId = findViewById(R.id.btnBuscarReceta);
        btnBuscarRecetaPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        btnBuscarRecetaPorId.setOnClickListener(v -> {
            try {
                String id = txtBusqueda.getText().toString().trim();
                int idReceta = Integer.parseInt(id);
                buscarRecetaPorId(idReceta);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listverReceta = findViewById(R.id.lvReceta);
        listverReceta.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        llenarLista();
        listverReceta.setOnItemClickListener((parent, view, position, id) -> {
            Receta receta = (Receta) parent.getItemAtPosition(position);
            showOptionsDialog(receta);
        });
    }

    private void llenarLista() {
        recetaDAO.getAllRecetas(recetas -> runOnUiThread(() -> {
            listaReceta = recetas;
            adaptadorReceta = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaReceta);
            listverReceta.setAdapter(adaptadorReceta);
        }));
    }

    private void showOptionsDialog(final Receta receta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2))
                verReceta(receta);
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3))
                editarReceta(receta);
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4))
                eliminarReceta(receta.getIdDoctor(), receta.getIdCliente(), receta.getIdReceta());
            else
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        View view = getLayoutInflater().inflate(R.layout.dialog_receta, null);
        builder.setView(view);

        Spinner spinnerIdDoctor = view.findViewById(R.id.spinnerIdDoctor);
        Spinner spinnerIdCliente = view.findViewById(R.id.spinnerIdCliente);
        EditText etIdReceta = view.findViewById(R.id.editTextIdReceta);
        TextView tvFecha = view.findViewById(R.id.textViewFechaExpedida);
        EditText etDescripcion = view.findViewById(R.id.editTextDescripcion);
        Button btnGuardar = view.findViewById(R.id.btnGuardarReceta);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarReceta);

        tvFecha.setOnClickListener(v -> mostrarDatePicker(tvFecha));

        AlertDialog dialog = builder.create();

        // Cargar Doctores con estilo similar al ejemplo de SucursalFarmacia
        recetaDAO.getAllDoctores(doctors -> {
            doctors.add(0, new Doctor(-1, getString(R.string.select_doctor), this)); // Agregar opción "Seleccionar Doctor"

            ArrayAdapter<Doctor> adapterDoctor = new ArrayAdapter<Doctor>(this, android.R.layout.simple_spinner_item, doctors) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Doctor doctor = getItem(position);
                    if (doctor.getIdDoctor() == -1) {
                        view.setText(getString(R.string.select_doctor));
                    } else {
                        view.setText(doctor.getNombreDoctor());
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Doctor doctor = getItem(position);
                    if (doctor.getIdDoctor() == -1) {
                        view.setText(getString(R.string.select_doctor));
                    } else {
                        view.setText(doctor.getNombreDoctor() + " (ID: " + doctor.getIdDoctor() + ")");
                    }
                    return view;
                }
            };
            adapterDoctor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerIdDoctor.setAdapter(adapterDoctor);
        });

        // Cargar Clientes con estilo similar al ejemplo de SucursalFarmacia
        recetaDAO.getAllClientes(clientes -> {
            clientes.add(0, new Cliente(-1, getString(R.string.select_cliente), this)); // Agregar opción "Seleccionar Cliente"

            ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, clientes) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Cliente cliente = getItem(position);
                    if (cliente.getIdCliente() == -1) {
                        view.setText(getString(R.string.select_cliente));
                    } else {
                        view.setText(cliente.getNombreCliente());
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Cliente cliente = getItem(position);
                    if (cliente.getIdCliente() == -1) {
                        view.setText(getString(R.string.select_cliente));
                    } else {
                        view.setText(cliente.getNombreCliente() + " (ID: " + cliente.getIdCliente() + ")");
                    }
                    return view;
                }
            };
            adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerIdCliente.setAdapter(adapterCliente);
        });

        btnGuardar.setOnClickListener(v -> {
            List<View> vistas = Arrays.asList(spinnerIdDoctor, spinnerIdCliente, etIdReceta, tvFecha, etDescripcion);
            List<String> regex = Arrays.asList("^\\d+$", "^\\d+$", "^\\d+$", "^\\d{4}-\\d{2}-\\d{2}$", "^[\\s\\S]{1,}$");
            List<Integer> mensajesError = Arrays.asList(
                    R.string.select_doctor,
                    R.string.select_client,
                    R.string.only_numbers,
                    R.string.invalid_date_format,
                    R.string.required_field
            );

            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, mensajesError);

            if (validador.validarCampos()) {
                Doctor doctorSeleccionado = (Doctor) spinnerIdDoctor.getSelectedItem();
                Cliente clienteSeleccionado = (Cliente) spinnerIdCliente.getSelectedItem();

                int idDoctor = doctorSeleccionado.getIdDoctor();
                int idCliente = clienteSeleccionado.getIdCliente();
                int idReceta = Integer.parseInt(etIdReceta.getText().toString().trim());
                String fecha = tvFecha.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();

                Receta receta = new Receta(idDoctor, idCliente, idReceta, fecha, descripcion, this);
                recetaDAO.addReceta(receta, response -> {
                    llenarLista();
                    dialog.dismiss();
                });
            }
        });

        btnLimpiar.setOnClickListener(v -> {
            spinnerIdDoctor.setSelection(0);
            spinnerIdCliente.setSelection(0);
            etIdReceta.setText("");
            tvFecha.setText("");
            etDescripcion.setText("");
        });

        dialog.show();
    }


    private void mostrarDatePicker(TextView target) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR), month = c.get(Calendar.MONTH), day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            String fecha = y + "-" + String.format("%02d", (m + 1)) + "-" + String.format("%02d", d);
            target.setText(fecha);
        }, year, month, day).show();
    }

    private void verReceta(Receta receta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prescription);

        View view = getLayoutInflater().inflate(R.layout.dialog_receta, null);
        builder.setView(view);

        Spinner spinnerIdDoctor = view.findViewById(R.id.spinnerIdDoctor);
        Spinner spinnerIdCliente = view.findViewById(R.id.spinnerIdCliente);
        EditText etIdReceta = view.findViewById(R.id.editTextIdReceta);
        TextView tvFecha = view.findViewById(R.id.textViewFechaExpedida);
        EditText etDescripcion = view.findViewById(R.id.editTextDescripcion);

        // Cargar Doctores con estilo similar al ejemplo de SucursalFarmacia
        recetaDAO.getAllDoctores(doctors -> {
            doctors.add(0, new Doctor(-1, getString(R.string.select_doctor), this)); // "Seleccionar Doctor"

            runOnUiThread(() -> {
                ArrayAdapter<Doctor> adapterDoctor = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctors) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Doctor doctor = getItem(position);
                        if (doctor.getIdDoctor() == -1) {
                            view.setText(getString(R.string.select_doctor));
                        } else {
                            view.setText(doctor.getNombreDoctor());
                        }
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        Doctor doctor = getItem(position);
                        if (doctor.getIdDoctor() == -1) {
                            view.setText(getString(R.string.select_doctor));
                        } else {
                            view.setText(doctor.getNombreDoctor() + " (ID: " + doctor.getIdDoctor() + ")");
                        }
                        return view;
                    }
                };
                adapterDoctor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdDoctor.setAdapter(adapterDoctor);

                // Seleccionar el doctor correspondiente
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).getIdDoctor() == receta.getIdDoctor()) {
                        spinnerIdDoctor.setSelection(i);
                        break;
                    }
                }
            });
        });

        // Cargar Clientes con estilo similar al ejemplo de SucursalFarmacia
        recetaDAO.getAllClientes(clientes -> {
            clientes.add(0, new Cliente(-1, getString(R.string.select_cliente), this)); // "Seleccionar Cliente"

            runOnUiThread(() -> {
                ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientes) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Cliente cliente = getItem(position);
                        if (cliente.getIdCliente() == -1) {
                            view.setText(getString(R.string.select_cliente));
                        } else {
                            view.setText(cliente.getNombreCliente());
                        }
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        Cliente cliente = getItem(position);
                        if (cliente.getIdCliente() == -1) {
                            view.setText(getString(R.string.select_cliente));
                        } else {
                            view.setText(cliente.getNombreCliente() + " (ID: " + cliente.getIdCliente() + ")");
                        }
                        return view;
                    }
                };
                adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdCliente.setAdapter(adapterCliente);

                // Seleccionar el cliente correspondiente
                for (int i = 0; i < clientes.size(); i++) {
                    if (clientes.get(i).getIdCliente() == receta.getIdCliente()) {
                        spinnerIdCliente.setSelection(i);
                        break;
                    }
                }
            });
        });

        // Configurar los campos de texto con la información de la receta
        etIdReceta.setText(String.valueOf(receta.getIdReceta()));
        tvFecha.setText(receta.getFechaExpedida());
        etDescripcion.setText(receta.getDescripcion());

        // Deshabilitar los campos de los Spinners y los EditText (solo visualización)
        spinnerIdDoctor.setEnabled(false);
        spinnerIdCliente.setEnabled(false);
        etIdReceta.setEnabled(false);
        tvFecha.setEnabled(false);
        etDescripcion.setEnabled(false);

        // Ocultar los botones de guardar y limpiar
        view.findViewById(R.id.btnGuardarReceta).setVisibility(View.GONE);
        view.findViewById(R.id.btnLimpiarReceta).setVisibility(View.GONE);

        // Mostrar el diálogo
        builder.create().show();
    }



    private void editarReceta(Receta receta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);

        View view = getLayoutInflater().inflate(R.layout.dialog_receta, null);
        builder.setView(view);

        Spinner spinnerIdDoctor = view.findViewById(R.id.spinnerIdDoctor);
        Spinner spinnerIdCliente = view.findViewById(R.id.spinnerIdCliente);
        EditText etIdReceta = view.findViewById(R.id.editTextIdReceta);
        TextView tvFecha = view.findViewById(R.id.textViewFechaExpedida);
        EditText etDescripcion = view.findViewById(R.id.editTextDescripcion);

        // Rellenar los campos básicos de la receta
        etIdReceta.setText(String.valueOf(receta.getIdReceta()));
        tvFecha.setText(receta.getFechaExpedida());
        etDescripcion.setText(receta.getDescripcion());

        // Cargar Doctores de forma asincrónica
        recetaDAO.getAllDoctores(doctors -> {
            // Agregar "Seleccionar" al inicio de la lista
            doctors.add(0, new Doctor(-1, getString(R.string.select_doctor), this));

            runOnUiThread(() -> {
                // Adaptador personalizado para mostrar el nombre y el ID
                ArrayAdapter<Doctor> adapterDoctor = new ArrayAdapter<Doctor>(this, android.R.layout.simple_spinner_item, doctors) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Doctor doctor = getItem(position);
                        if (doctor.getIdDoctor() == -1) {
                            view.setText(getString(R.string.select_doctor));
                        } else {
                            view.setText(doctor.getNombreDoctor()); // Mostrar solo el nombre en el Spinner
                        }
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        Doctor doctor = getItem(position);
                        if (doctor.getIdDoctor() == -1) {
                            view.setText(getString(R.string.select_doctor));
                        } else {
                            view.setText(doctor.getNombreDoctor() + " (ID: " + doctor.getIdDoctor() + ")");
                        }
                        return view;
                    }
                };
                adapterDoctor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdDoctor.setAdapter(adapterDoctor);

                // Seleccionar el doctor correspondiente
                for (int i = 0; i < doctors.size(); i++) {
                    if (doctors.get(i).getIdDoctor() == receta.getIdDoctor()) {
                        spinnerIdDoctor.setSelection(i);
                        break;
                    }
                }
            });
        });

        // Cargar Clientes de forma asincrónica
        recetaDAO.getAllClientes(clientes -> {
            // Agregar "Seleccionar" al inicio de la lista
            clientes.add(0, new Cliente(-1, getString(R.string.select_cliente), this));

            runOnUiThread(() -> {
                // Adaptador personalizado para mostrar el nombre y el ID
                ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, clientes) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        Cliente cliente = getItem(position);
                        if (cliente.getIdCliente() == -1) {
                            view.setText(getString(R.string.select_cliente));
                        } else {
                            view.setText(cliente.getNombreCliente()); // Mostrar solo el nombre en el Spinner
                        }
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        Cliente cliente = getItem(position);
                        if (cliente.getIdCliente() == -1) {
                            view.setText(getString(R.string.select_cliente));
                        } else {
                            view.setText(cliente.getNombreCliente() + " (ID: " + cliente.getIdCliente() + ")");
                        }
                        return view;
                    }
                };
                adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdCliente.setAdapter(adapterCliente);

                // Seleccionar el cliente correspondiente
                for (int i = 0; i < clientes.size(); i++) {
                    if (clientes.get(i).getIdCliente() == receta.getIdCliente()) {
                        spinnerIdCliente.setSelection(i);
                        break;
                    }
                }
            });
        });

        // Configurar los Spinners como no editables
        spinnerIdDoctor.setEnabled(false);
        spinnerIdCliente.setEnabled(false);
        etIdReceta.setEnabled(false);
        tvFecha.setEnabled(false);
        etDescripcion.setEnabled(false);

        // Mostrar el DatePicker para la fecha
        tvFecha.setOnClickListener(v -> mostrarDatePicker(tvFecha));

        Button btnGuardar = view.findViewById(R.id.btnGuardarReceta);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarReceta);
        btnLimpiar.setVisibility(View.GONE);

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            List<View> vistas = Arrays.asList(tvFecha, etDescripcion);
            List<String> regex = Arrays.asList("^\\d{4}-\\d{2}-\\d{2}$", "^[\\s\\S]{1,}$");
            List<Integer> errores = Arrays.asList(R.string.invalid_date_format, R.string.required_field);

            ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, errores);

            if (validador.validarCampos()) {
                receta.setFechaExpedida(tvFecha.getText().toString().trim());
                receta.setDescripcion(etDescripcion.getText().toString().trim());

                // Llamar al método updateReceta con un Response.Listener
                recetaDAO.updateReceta(receta, response -> {
                    llenarLista(); // Recargar la lista después de la actualización
                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    private void eliminarReceta(int idDoctor, int idCliente, int idReceta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idDoctor + ", " + idCliente + ", " + idReceta);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            recetaDAO.deleteReceta(idReceta, response -> llenarLista()); // Recargar la lista después de eliminar
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void buscarRecetaPorId(int idReceta) {
        recetaDAO.getReceta(idReceta, receta -> {
            if (receta != null) {
                verReceta(receta);
            } else {
                Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

