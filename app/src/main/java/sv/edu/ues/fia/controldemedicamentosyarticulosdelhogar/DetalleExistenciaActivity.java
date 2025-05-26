package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DetalleExistenciaActivity extends AppCompatActivity {

    private DetalleExistenciaDAO detalleExistenciaDAO;
    private SucursalFarmaciaDAO sucursalFarmaciaDAO;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    private ArrayAdapter<DetalleExistencia> adaptadorDetalleExistencia;
    private List<DetalleExistencia> listaDetalleExistencia;
    private ListView listViewDetalleExistencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_existencia);

        detalleExistenciaDAO = new DetalleExistenciaDAO(this);
        sucursalFarmaciaDAO = new SucursalFarmaciaDAO(this);

        Button btnAgregar = findViewById(R.id.btnAgregarExistence);
        Button btnBuscar = findViewById(R.id.btnBuscarExistence);

        btnAgregar.setOnClickListener(v -> showAddDialog());
        btnBuscar.setOnClickListener(v -> showSearchDialog());

        btnAgregar.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.GONE);

        listViewDetalleExistencia = findViewById(R.id.lvExistenceDetail);

        listViewDetalleExistencia.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.GONE);

        loadAllDetallesExistencia();

        btnBuscar.setVisibility(vac.validarAcceso(2) ? View.VISIBLE : View.GONE);

        listViewDetalleExistencia.setOnItemClickListener((parent, view, position, id) -> {
            if (vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)) {
                DetalleExistencia detalleExistencia = (DetalleExistencia) parent.getItemAtPosition(position);
                showOptionsDialog(detalleExistencia);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAllDetallesExistencia() {
        detalleExistenciaDAO.getAllDetallesExistencia(detalles -> runOnUiThread(() -> {
            listaDetalleExistencia = detalles;
            adaptadorDetalleExistencia = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleExistencia);
            listViewDetalleExistencia.setAdapter(adaptadorDetalleExistencia);
        }));
    }

    public void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_existencia, null);
        builder.setView(dialogView);

        EditText editTextIdExistenceDetail = dialogView.findViewById(R.id.editTextIdExistenceDetail);
        EditText editTextExpirationDate = dialogView.findViewById(R.id.editTextExpirationDate);
        EditText editTextExistenceAmount = dialogView.findViewById(R.id.editTextExistenceAmount);
        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerIdFarmacia);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerIdArticulo);

            sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> runOnUiThread(() -> {
            detalleExistenciaDAO.getAllArticulos(articulos -> runOnUiThread(() -> {

                List<SucursalFarmacia> sucursalesList = new ArrayList<>(sucursales);
                List<Articulo> articulosList = new ArrayList<>(articulos);

                // Agregar los elementos seleccionables al principio de las listas
                sucursalesList.add(0, new SucursalFarmacia(-1, getString(R.string.select_sucursal)));
                articulosList.add(0, new Articulo(-1, getString(R.string.select_articulo), this));


                // Configurar adaptadores para los spinners
                ArrayAdapter<SucursalFarmacia> adapterSucursales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sucursalesList);
                adapterSucursales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdFarmacia.setAdapter(adapterSucursales);

                ArrayAdapter<Articulo> adapterArticulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulosList);
                adapterArticulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdArticulo.setAdapter(adapterArticulos);
            }));
        }));

        editTextExpirationDate.setInputType(InputType.TYPE_NULL);
        editTextExpirationDate.setFocusable(false);
        editTextExpirationDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextExpirationDate.setText(selectedDate);
            }, year, month, day).show();
        });

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this,
                Collections.singletonList(editTextExpirationDate),
                Collections.singletonList("\\d{4}-\\d{2}-\\d{2}"),
                Collections.singletonList(R.string.invalid_date));

        final AlertDialog dialog = builder.create();

        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        btnGuardar.setOnClickListener(v -> saveExistence(spinnerIdArticulo, spinnerIdFarmacia, editTextExistenceAmount, editTextExpirationDate, editTextIdExistenceDetail, dialog));
        btnLimpiar.setOnClickListener(v -> limpiarCampos(spinnerIdArticulo, spinnerIdFarmacia, editTextExistenceAmount, editTextExpirationDate, editTextIdExistenceDetail));

        dialog.show();
    }


    public void saveExistence(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
                              EditText editTextExistenceAmount, EditText editTextExpirationDate,
                              EditText editTextIdExistenceDetail, AlertDialog dialog) {

        if (spinnerIdArticulo.getSelectedItemPosition() == 0 ||
                spinnerIdFarmacia.getSelectedItemPosition() == 0 ||
                editTextExistenceAmount.getText().toString().trim().isEmpty() ||
                editTextExpirationDate.getText().toString().trim().isEmpty() ||
                editTextIdExistenceDetail.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, getString(R.string.datos_validos), Toast.LENGTH_LONG).show();
            return;
        }

        SucursalFarmacia sucursal = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
        Articulo articulo = (Articulo) spinnerIdArticulo.getSelectedItem();
        int idDetalleExistencia = Integer.parseInt(editTextIdExistenceDetail.getText().toString());
        int cantidadExistencia = Integer.parseInt(editTextExistenceAmount.getText().toString());
        String fechaDeVencimiento = editTextExpirationDate.getText().toString().trim();

        DetalleExistencia detalleExistencia = new DetalleExistencia(
                articulo.getIdArticulo(),
                idDetalleExistencia,
                sucursal.getIdFarmacia(),
                cantidadExistencia,
                fechaDeVencimiento,
                this
        );

        detalleExistenciaDAO.addExistencia(detalleExistencia, response -> runOnUiThread(() -> {
            loadAllDetallesExistencia();
            dialog.dismiss();
        }));
    }

    public void limpiarCampos(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
                              EditText editTextExistenceAmount, EditText editTextExpirationDate,
                              EditText editTextIdExistenceDetail) {
        editTextExistenceAmount.setText("");
        editTextExpirationDate.setText("");
        editTextIdExistenceDetail.setText("");
        spinnerIdArticulo.setSelection(0);
        spinnerIdFarmacia.setSelection(0);
    }

    public void showOptionsDialog(DetalleExistencia detalleExistencia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        if (!vac.validarAcceso(2))
            dialogView.findViewById(R.id.buttonView).setVisibility(View.GONE);

        if (!vac.validarAcceso(3))
            dialogView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);

        if (!vac.validarAcceso(4))
            dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            viewExistence(detalleExistencia);
        });
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            editExistence(detalleExistencia);
        });
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            deleteExistence(detalleExistencia.getIdDetalleExistencia(), dialog);
        });

        dialog.show();
    }

    private void viewExistence(DetalleExistencia detalleExistencia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_existencia, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText editTextIdExistenceDetail = dialogView.findViewById(R.id.editTextIdExistenceDetail);
        EditText editTextExpirationDate = dialogView.findViewById(R.id.editTextExpirationDate);
        EditText editTextExistenceAmount = dialogView.findViewById(R.id.editTextExistenceAmount);
        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerIdFarmacia);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerIdArticulo);
        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        editTextIdExistenceDetail.setText(String.valueOf(detalleExistencia.getIdDetalleExistencia()));
        editTextExpirationDate.setText(detalleExistencia.getFechaDeVencimiento());
        editTextExistenceAmount.setText(String.valueOf(detalleExistencia.getCantidadExistencia()));

        editTextIdExistenceDetail.setEnabled(false);
        editTextExpirationDate.setEnabled(false);
        editTextExistenceAmount.setEnabled(false);
        editTextIdExistenceDetail.setTextColor(Color.BLACK);
        editTextExpirationDate.setTextColor(Color.BLACK);
        editTextExistenceAmount.setTextColor(Color.BLACK);

        spinnerIdFarmacia.setEnabled(false);
        spinnerIdArticulo.setEnabled(false);

        // Cargar los datos de Farmacia y Articulo
        sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> runOnUiThread(() -> {
            detalleExistenciaDAO.getAllArticulos(articulos -> runOnUiThread(() -> {

                // Crear las listas
                List<SucursalFarmacia> sucursalesList = new ArrayList<>(sucursales);
                List<Articulo> articulosList = new ArrayList<>(articulos);

                // Crear los adaptadores
                ArrayAdapter<SucursalFarmacia> adapterSucursales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sucursalesList);
                adapterSucursales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdFarmacia.setAdapter(adapterSucursales);

                ArrayAdapter<Articulo> adapterArticulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulosList);
                adapterArticulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdArticulo.setAdapter(adapterArticulos);

                // Seleccionar el elemento correspondiente por ID
                int farmaciaIndex = getIndexById(sucursalesList, detalleExistencia.getIdFarmacia());
                int articuloIndex = getIndexById(articulosList, detalleExistencia.getIdArticulo());

                spinnerIdFarmacia.setSelection(farmaciaIndex);
                spinnerIdArticulo.setSelection(articuloIndex);
            }));
        }));

        // Configurar visibilidad de botones
        btnGuardar.setVisibility(View.GONE);
        btnLimpiar.setVisibility(View.GONE);

        dialog.show();
    }

    private void editExistence(DetalleExistencia detalleExistencia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_existencia, null);
        builder.setView(dialogView);
        final AlertDialog dialog2 = builder.create();

        EditText editTextIdExistenceDetail = dialogView.findViewById(R.id.editTextIdExistenceDetail);
        EditText editTextExpirationDate = dialogView.findViewById(R.id.editTextExpirationDate);
        EditText editTextExistenceAmount = dialogView.findViewById(R.id.editTextExistenceAmount);
        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerIdFarmacia);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerIdArticulo);

        // Cargar datos existentes
        editTextIdExistenceDetail.setText(String.valueOf(detalleExistencia.getIdDetalleExistencia()));
        editTextExpirationDate.setText(detalleExistencia.getFechaDeVencimiento());
        editTextExistenceAmount.setText(String.valueOf(detalleExistencia.getCantidadExistencia()));
        spinnerIdFarmacia.setEnabled(false);
        spinnerIdArticulo.setEnabled(false);
        editTextIdExistenceDetail.setEnabled(false);
        editTextIdExistenceDetail.setTextColor(Color.BLACK);

        // Cargar los datos de Farmacia y Articulo
        sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> runOnUiThread(() -> {
            detalleExistenciaDAO.getAllArticulos(articulos -> runOnUiThread(() -> {

                // Crear las listas
                List<SucursalFarmacia> sucursalesList = new ArrayList<>(sucursales);
                List<Articulo> articulosList = new ArrayList<>(articulos);

                // Crear los adaptadores
                ArrayAdapter<SucursalFarmacia> adapterSucursales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sucursalesList);
                adapterSucursales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdFarmacia.setAdapter(adapterSucursales);

                ArrayAdapter<Articulo> adapterArticulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulosList);
                adapterArticulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdArticulo.setAdapter(adapterArticulos);

                // Seleccionar el elemento correspondiente por ID
                int farmaciaIndex = getIndexById(sucursalesList, detalleExistencia.getIdFarmacia());
                int articuloIndex = getIndexById(articulosList, detalleExistencia.getIdArticulo());

                spinnerIdFarmacia.setSelection(farmaciaIndex);
                spinnerIdArticulo.setSelection(articuloIndex);
            }));
        }));

        // Configuración de fecha
        editTextExpirationDate.setInputType(InputType.TYPE_NULL);
        editTextExpirationDate.setFocusable(false);
        editTextExpirationDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextExpirationDate.setText(selectedDate);
            }, year, month, day).show();
        });

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this,
                Collections.singletonList(editTextExpirationDate),
                Collections.singletonList("\\d{4}-\\d{2}-\\d{2}"),
                Collections.singletonList(R.string.invalid_date));

        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        // Guardar cambios
        btnGuardar.setOnClickListener(v -> {
            updateExistence(spinnerIdArticulo, spinnerIdFarmacia, editTextExistenceAmount, editTextExpirationDate, editTextIdExistenceDetail, dialog2);
        });

        // Cancelar edición
        btnLimpiar.setOnClickListener(v -> dialog2.dismiss());

        dialog2.show();
    }

    public void updateExistence(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
                                EditText editTextExistenceAmount, EditText editTextExpirationDate,
                                EditText editTextIdExistenceDetail, AlertDialog dialog) {

        // Validación de campos
        if (editTextExistenceAmount.getText().toString().trim().isEmpty() ||
                editTextExpirationDate.getText().toString().trim().isEmpty() ||
                editTextIdExistenceDetail.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getText(R.string.datos_validos), Toast.LENGTH_LONG).show();
            return;
        }

        SucursalFarmacia sucursal = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
        Articulo articulo = (Articulo) spinnerIdArticulo.getSelectedItem();
        int idDetalleExistencia = Integer.parseInt(editTextIdExistenceDetail.getText().toString());
        int cantidadExistencia = Integer.parseInt(editTextExistenceAmount.getText().toString());
        String fechaDeVencimiento = editTextExpirationDate.getText().toString().trim();

        DetalleExistencia detalleExistencia = new DetalleExistencia(
                articulo.getIdArticulo(),
                idDetalleExistencia,
                sucursal.getIdFarmacia(),
                cantidadExistencia,
                fechaDeVencimiento,
                this
        );

        // Actualización de la existencia
        detalleExistenciaDAO.updateExistencia(detalleExistencia, response -> runOnUiThread(() -> {
            // Actualizar la lista después de la edición
            loadAllDetallesExistencia();
            // Cerrar el diálogo después de la actualización
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }));
    }

    private <T> int getIndexById(List<T> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (item instanceof SucursalFarmacia && ((SucursalFarmacia) item).getIdFarmacia() == id) {
                return i;
            } else if (item instanceof Articulo && ((Articulo) item).getIdArticulo() == id) {
                return i;
            }
        }
        return 0;
    }


    public void deleteExistence(int id, AlertDialog dialog_init) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            detalleExistenciaDAO.deleteExistencia(id, response -> runOnUiThread(() -> {
                loadAllDetallesExistencia();
            }));
            dialog_init.dismiss();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.search);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_buscar, null);
        builder.setView(dialogView);

        Button botonMostrarFarmacia = dialogView.findViewById(R.id.btnMostrarFarmacia);
        Button botonMostrarArticulo = dialogView.findViewById(R.id.btnMostrarArticulo);
        Button btnSearchDetailExistence = dialogView.findViewById(R.id.btnSearchDetailExistence);

        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerFarmaciaB);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerArticuloB);

        spinnerIdFarmacia.setVisibility(View.GONE);
        spinnerIdArticulo.setVisibility(View.GONE);

        botonMostrarFarmacia.setOnClickListener(v -> {
            spinnerIdFarmacia.setVisibility(View.VISIBLE);
            spinnerIdArticulo.setVisibility(View.GONE);
        });

        botonMostrarArticulo.setOnClickListener(v -> {
            spinnerIdArticulo.setVisibility(View.VISIBLE);
            spinnerIdFarmacia.setVisibility(View.GONE);
        });

        sucursalFarmaciaDAO.getAllSucursalFarmacia(sucursales -> runOnUiThread(() -> {
            detalleExistenciaDAO.getAllArticulos(articulos -> runOnUiThread(() -> {
                SucursalFarmacia seleccionSucursalFarmacia = new SucursalFarmacia(-1, getString(R.string.select_sucursal));
                Articulo seleccionArticulo = new Articulo(-1, getString(R.string.select_articulo), this);
                sucursales.add(0, seleccionSucursalFarmacia);
                articulos.add(0, seleccionArticulo);

                ArrayAdapter<SucursalFarmacia> adapterSucursales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sucursales);
                adapterSucursales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdFarmacia.setAdapter(adapterSucursales);

                ArrayAdapter<Articulo> adapterArticulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulos);
                adapterArticulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIdArticulo.setAdapter(adapterArticulos);

                final AlertDialog dialog = builder.create();
                dialog.show();

                btnSearchDetailExistence.setOnClickListener(v -> {
                    if (spinnerIdFarmacia.getVisibility() == View.GONE) {
                        Articulo articuloSeleccionado = (Articulo) spinnerIdArticulo.getSelectedItem();
                        detalleExistenciaDAO.getAllDetallesExistenciaByIdArticulo(articuloSeleccionado.getIdArticulo(), detalles -> runOnUiThread(() -> {
                            dialog.dismiss();
                            fillList(detalles);
                        }));
                    } else {
                        SucursalFarmacia farmaciaSeleccionada = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
                        detalleExistenciaDAO.getAllDetallesExistenciaByIdFarm(farmaciaSeleccionada.getIdFarmacia(), detalles -> runOnUiThread(() -> {
                            dialog.dismiss();
                            fillList(detalles);
                        }));
                    }
                });
            }));
        }));
    }

    private void fillList(List<DetalleExistencia> detalles) {
        listViewDetalleExistencia.setVisibility(View.VISIBLE);
        listaDetalleExistencia = detalles;
        adaptadorDetalleExistencia = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleExistencia);
        listViewDetalleExistencia.setAdapter(adaptadorDetalleExistencia);
    }
}

