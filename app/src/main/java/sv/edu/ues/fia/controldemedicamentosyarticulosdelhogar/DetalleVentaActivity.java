package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetalleVentaActivity extends AppCompatActivity {

    private DetalleVentaDAO detalleVentaDAO;
    private ArrayAdapter<DetalleVenta> adaptadorDetalleVenta;
    private List<DetalleVenta> listaDetalleVenta;
    private ListView listViewDetalleVenta;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_venta);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        detalleVentaDAO = new DetalleVentaDAO(conexionDB, this);

        TextView txtBusqueda = findViewById(R.id.busquedaDetalleVenta);


        Button btnBuscarDetalleVentaPorId = findViewById(R.id.btnBuscarDetalleVenta);
        btnBuscarDetalleVentaPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarDetalleVentaPorId.setOnClickListener(v -> {
            try {
                String searchText = txtBusqueda.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    int idVentaDetalle = Integer.parseInt(searchText);
                    buscarDetalleVentaPorIdVentaDetalle(idVentaDetalle);
                } else {
                    Toast.makeText(this, R.string.invalid_search_id, Toast.LENGTH_LONG).show();
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewDetalleVenta = findViewById(R.id.lvDetalleVenta);
        listViewDetalleVenta.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        listViewDetalleVenta.setOnItemClickListener((parent, view, position, id) -> {
            DetalleVenta detalleVenta = (DetalleVenta) parent.getItemAtPosition(position);
            showOptionsDialog(detalleVenta);
        });

        Button btnAgregarDetalleVenta = findViewById(R.id.btnAgregarDetalleVenta);
        btnAgregarDetalleVenta.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarDetalleVenta.setOnClickListener(v -> showAddDialog());
    }

    private void fillList() {
        listaDetalleVenta = detalleVentaDAO.getAllDetalleVenta();
        // TODO: Create a custom ArrayAdapter if DetalleVenta.toString() is not suitable for display
        adaptadorDetalleVenta = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleVenta);
        listViewDetalleVenta.setAdapter(adaptadorDetalleVenta);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_detalle_venta);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_venta, null);
        builder.setView(dialogView);

        EditText editTextIdCliente = dialogView.findViewById(R.id.editTextIdCliente);
        EditText editTextIdVenta = dialogView.findViewById(R.id.editTextIdVenta);
        EditText editTextIdArticulo = dialogView.findViewById(R.id.editTextIdArticulo);
        EditText editTextIdVentaDetalle = dialogView.findViewById(R.id.editTextIdVentaDetalle);

        EditText editTextFechaDetalleVenta = dialogView.findViewById(R.id.editTextFechaDetalleVenta);
        EditText editTextUnitarioDetalleVenta = dialogView.findViewById(R.id.editTextUnitarioDetalleVenta);
        EditText editTextCantidadDetalleVenta = dialogView.findViewById(R.id.editTextCantidadDetalleVenta);
        EditText editTextTotalDetalleVenta = dialogView.findViewById(R.id.editTextTotalDetalleVenta);

        Spinner spinnerArticuloVenta = dialogView.findViewById(R.id.spinnerArticuloVenta);
        Spinner spinnerFacturaVenta = dialogView.findViewById(R.id.spinnerFacturaVenta);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal(editTextUnitarioDetalleVenta, editTextCantidadDetalleVenta, editTextTotalDetalleVenta);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        editTextUnitarioDetalleVenta.addTextChangedListener(watcher);
        editTextCantidadDetalleVenta.addTextChangedListener(watcher);
        editTextUnitarioDetalleVenta.setFocusable(false); // Make non-editable
        editTextUnitarioDetalleVenta.setClickable(false); // Make non-editable
        editTextTotalDetalleVenta.setFocusable(false);
        editTextTotalDetalleVenta.setClickable(false);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDetalleVenta);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDetalleVenta);

        List<FacturaVenta> facturas = detalleVentaDAO.getAllFacturaVenta();
        FacturaVenta placeholderFactura = new FacturaVenta();
        placeholderFactura.setIdCliente(-1);
        placeholderFactura.setIdVenta(-1);
        facturas.add(0, placeholderFactura);

        ArrayAdapter<FacturaVenta> adapterFactura = new ArrayAdapter<FacturaVenta>(this, android.R.layout.simple_spinner_item, facturas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FacturaVenta factura = getItem(position);
                if (factura.getIdCliente() == -1) {
                    view.setText(getString(R.string.select_factura_venta));
                } else {
                    view.setText(getString(R.string.invoice_id_short) + ": " + factura.getIdVenta() + " (Cli: " + factura.getIdCliente() + ")");
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FacturaVenta factura = getItem(position);
                if (factura.getIdCliente() == -1) {
                    view.setText(getString(R.string.select_factura_venta));
                    
                } else {
                    view.setText("ID Venta: " + factura.getIdVenta() + ", Cliente: " + factura.getIdCliente() + ", Fecha: " + factura.getFechaVenta());
                    
                }
                return view;
            }
        };
        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaVenta.setAdapter(adapterFactura);


        List<Articulo> articulos = detalleVentaDAO.getAllArticulo();
        articulos.add(0, new Articulo(-1, getString(R.string.select_articulo), this));

        ArrayAdapter<Articulo> adapterArticulo = new ArrayAdapter<Articulo>(this, android.R.layout.simple_spinner_item, articulos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Articulo articulo = getItem(position);
                if (articulo.getIdArticulo() == -1) {
                    view.setText(getString(R.string.select_articulo));
                } else {
                    view.setText(articulo.getNombreArticulo());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Articulo articulo = getItem(position);
                if (articulo.getIdArticulo() == -1) {
                    view.setText(getString(R.string.select_articulo));
                    
                } else {
                    view.setText(articulo.getNombreArticulo() + " (ID: " + articulo.getIdArticulo() + ")");
                    
                }
                return view;
            }
        };
        adapterArticulo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArticuloVenta.setAdapter(adapterArticulo);

        spinnerArticuloVenta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Articulo articuloSeleccionado = (Articulo) parent.getItemAtPosition(position);
                if (articuloSeleccionado != null && articuloSeleccionado.getIdArticulo() != -1) {
                    editTextUnitarioDetalleVenta.setText(String.format(Locale.getDefault(), "%.2f", articuloSeleccionado.getPrecioArticulo()));
                } else {
                    editTextUnitarioDetalleVenta.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextUnitarioDetalleVenta.setText("");
            }
        });


        editTextFechaDetalleVenta.setInputType(InputType.TYPE_NULL);
        editTextFechaDetalleVenta.setFocusable(false);
        editTextFechaDetalleVenta.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, day) -> {
                editTextFechaDetalleVenta.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });


        List<View> vistas = Arrays.asList(
                editTextIdVentaDetalle,
                editTextFechaDetalleVenta, editTextUnitarioDetalleVenta, editTextCantidadDetalleVenta,
                editTextTotalDetalleVenta, spinnerArticuloVenta, spinnerFacturaVenta
        );
        List<String> listaRegex = Arrays.asList(
                "\\d+", // IDVENTADETALLE
                "\\d{4}-\\d{2}-\\d{2}", // FECHADEVENTA
                "\\d+(\\.\\d{1,2})?", // PRECIOUNITARIOVENTA
                "\\d+", // CANTIDADVENTA
                "\\d+(\\.\\d{1,2})?", // TOTALDETALLEVENTA
                "\\d+", // spinnerArticuloVenta
                "\\d+"  // spinnerFacturaVenta
        );
        List<Integer> mensajesDeError = Arrays.asList(
                R.string.only_numbers_id_detalle_venta,
                R.string.invalid_date, R.string.only_numbers_price,
                R.string.only_numbers_quantity,
                R.string.only_numbers_total,
                R.string.select_articulo, R.string.select_factura_venta
        );

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);
        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            boolean spinnersValid = true;
            if (((FacturaVenta)spinnerFacturaVenta.getSelectedItem()).getIdCliente() == -1) {
                Toast.makeText(this, R.string.select_factura_venta, Toast.LENGTH_SHORT).show();
                spinnersValid = false;
            }
            if (((Articulo)spinnerArticuloVenta.getSelectedItem()).getIdArticulo() == -1) {
                Toast.makeText(this, R.string.select_articulo, Toast.LENGTH_SHORT).show();
                spinnersValid = false;
            }

            if (validadorDeCampos.validarCampos() && spinnersValid) {
                saveDetalleVenta(
                        editTextIdVentaDetalle, editTextFechaDetalleVenta, editTextUnitarioDetalleVenta,
                        editTextCantidadDetalleVenta, editTextTotalDetalleVenta,
                        spinnerArticuloVenta, spinnerFacturaVenta
                );
                dialog.dismiss();
            }
        });

        btnLimpiar.setOnClickListener(v -> clearFieldsDetalleVenta(
                editTextIdVentaDetalle, editTextFechaDetalleVenta, editTextUnitarioDetalleVenta,
                editTextCantidadDetalleVenta, editTextTotalDetalleVenta,
                spinnerArticuloVenta, spinnerFacturaVenta));
        dialog.show();
    }

    private void saveDetalleVenta(EditText editTextIdVentaDetalle, EditText editTextFechaDetalleVenta,
                                  EditText editTextUnitarioDetalleVenta, EditText editTextCantidadDetalleVenta,
                                  EditText editTextTotalDetalleVenta, Spinner spinnerArticuloVenta,
                                  Spinner spinnerFacturaVenta) {
        try {
            FacturaVenta facturaSeleccionada = (FacturaVenta) spinnerFacturaVenta.getSelectedItem();
            Articulo articuloSeleccionado = (Articulo) spinnerArticuloVenta.getSelectedItem();

            int idCliente = facturaSeleccionada.getIdCliente();
            int idVenta = facturaSeleccionada.getIdVenta();
            int idArticulo = articuloSeleccionado.getIdArticulo();
            int idVentaDetalle = Integer.parseInt(editTextIdVentaDetalle.getText().toString().trim());
            String fecha = editTextFechaDetalleVenta.getText().toString().trim();
            double precioUnitario = Double.parseDouble(editTextUnitarioDetalleVenta.getText().toString().trim());
            int cantidad = Integer.parseInt(editTextCantidadDetalleVenta.getText().toString().trim());
            double totalDetalle = Double.parseDouble(editTextTotalDetalleVenta.getText().toString().trim());

            DetalleVenta detalleVenta = new DetalleVenta(idCliente, idVenta, idArticulo, idVentaDetalle,
                    cantidad, precioUnitario, fecha, totalDetalle, this);
            detalleVentaDAO.addDetalleVenta(detalleVenta);
            fillList();
            clearFieldsDetalleVenta(editTextIdVentaDetalle, editTextFechaDetalleVenta, editTextUnitarioDetalleVenta,
                    editTextCantidadDetalleVenta, editTextTotalDetalleVenta,
                    spinnerArticuloVenta, spinnerFacturaVenta);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_saving_data_format, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_saving_data, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showOptionsDialog(final DetalleVenta detalleVenta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options_detalle_venta);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2)) {
                viewDetalleVenta(detalleVenta);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3)) {
                editDetalleVenta(detalleVenta);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4)) {
                deleteDetalleVenta(detalleVenta);
            } else {
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private void viewDetalleVenta(DetalleVenta detalleVenta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view_detalle_venta);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_venta, null);
        builder.setView(dialogView);

        EditText editTextIdVentaDetalle = dialogView.findViewById(R.id.editTextIdVentaDetalle);
        EditText editTextFechaDetalleVenta = dialogView.findViewById(R.id.editTextFechaDetalleVenta);
        EditText editTextUnitarioDetalleVenta = dialogView.findViewById(R.id.editTextUnitarioDetalleVenta);
        EditText editTextCantidadDetalleVenta = dialogView.findViewById(R.id.editTextCantidadDetalleVenta);
        EditText editTextTotalDetalleVenta = dialogView.findViewById(R.id.editTextTotalDetalleVenta);
        Spinner spinnerArticuloVenta = dialogView.findViewById(R.id.spinnerArticuloVenta);
        Spinner spinnerFacturaVenta = dialogView.findViewById(R.id.spinnerFacturaVenta);


        editTextIdVentaDetalle.setEnabled(false);
        editTextFechaDetalleVenta.setEnabled(false);
        editTextUnitarioDetalleVenta.setEnabled(false);
        editTextCantidadDetalleVenta.setEnabled(false);
        editTextTotalDetalleVenta.setEnabled(false);
        spinnerArticuloVenta.setEnabled(false);
        spinnerFacturaVenta.setEnabled(false);

        dialogView.findViewById(R.id.btnGuardarDetalleVenta).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnLimpiarDetalleVenta).setVisibility(View.GONE);


        editTextIdVentaDetalle.setText(String.valueOf(detalleVenta.getIdVentaDetalle()));
        editTextFechaDetalleVenta.setText(detalleVenta.getFechaDeVenta());
        editTextUnitarioDetalleVenta.setText(String.format(Locale.getDefault(), "%.2f", detalleVenta.getPrecioUnitarioVenta()));
        editTextCantidadDetalleVenta.setText(String.valueOf(detalleVenta.getCantidadVenta()));
        editTextTotalDetalleVenta.setText(String.format(Locale.getDefault(), "%.2f", detalleVenta.getTotalDetalleVenta()));


        List<FacturaVenta> facturas = detalleVentaDAO.getAllFacturaVenta();
        ArrayAdapter<FacturaVenta> adapterFactura = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, facturas);
        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaVenta.setAdapter(adapterFactura);
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getIdCliente() == detalleVenta.getIdCliente() && facturas.get(i).getIdVenta() == detalleVenta.getIdVenta()) {
                spinnerFacturaVenta.setSelection(i);
                break;
            }
        }

        // Articulo Spinner
        List<Articulo> articulos = detalleVentaDAO.getAllArticulo();
        ArrayAdapter<Articulo> adapterArticulo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulos);
        adapterArticulo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArticuloVenta.setAdapter(adapterArticulo);
        for (int i = 0; i < articulos.size(); i++) {
            if (articulos.get(i).getIdArticulo() == detalleVenta.getIdArticulo()) {
                spinnerArticuloVenta.setSelection(i);
                break;
            }
        }
        builder.setPositiveButton(R.string.ok, (d, w) -> d.dismiss());
        builder.show();
    }


    private void editDetalleVenta(DetalleVenta detalleVenta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_detalle_venta);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_venta, null);
        builder.setView(dialogView);

        EditText editTextIdVentaDetalle = dialogView.findViewById(R.id.editTextIdVentaDetalle);
        EditText editTextFechaDetalleVenta = dialogView.findViewById(R.id.editTextFechaDetalleVenta);
        EditText editTextUnitarioDetalleVenta = dialogView.findViewById(R.id.editTextUnitarioDetalleVenta);
        EditText editTextCantidadDetalleVenta = dialogView.findViewById(R.id.editTextCantidadDetalleVenta);
        EditText editTextTotalDetalleVenta = dialogView.findViewById(R.id.editTextTotalDetalleVenta);
        Spinner spinnerArticuloVenta = dialogView.findViewById(R.id.spinnerArticuloVenta);
        Spinner spinnerFacturaVenta = dialogView.findViewById(R.id.spinnerFacturaVenta);


        editTextIdVentaDetalle.setText(String.valueOf(detalleVenta.getIdVentaDetalle()));
        editTextIdVentaDetalle.setEnabled(false);


        spinnerFacturaVenta.setEnabled(false);
        spinnerArticuloVenta.setEnabled(false);


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal(editTextUnitarioDetalleVenta, editTextCantidadDetalleVenta, editTextTotalDetalleVenta);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        editTextUnitarioDetalleVenta.addTextChangedListener(watcher);
        editTextCantidadDetalleVenta.addTextChangedListener(watcher);
        editTextUnitarioDetalleVenta.setFocusable(false); // Make non-editable
        editTextUnitarioDetalleVenta.setClickable(false); // Make non-editable
        editTextTotalDetalleVenta.setFocusable(false);
        editTextTotalDetalleVenta.setClickable(false);

        // Populate spinners and set current values
        List<FacturaVenta> facturas = detalleVentaDAO.getAllFacturaVenta();
        ArrayAdapter<FacturaVenta> adapterFactura = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, facturas); // Use custom adapter if needed for display
        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaVenta.setAdapter(adapterFactura);
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getIdCliente() == detalleVenta.getIdCliente() && facturas.get(i).getIdVenta() == detalleVenta.getIdVenta()) {
                spinnerFacturaVenta.setSelection(i);
                break;
            }
        }

        List<Articulo> articulos = detalleVentaDAO.getAllArticulo();
        ArrayAdapter<Articulo> adapterArticulo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulos); // Use custom adapter
        adapterArticulo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArticuloVenta.setAdapter(adapterArticulo);
        for (int i = 0; i < articulos.size(); i++) {
            if (articulos.get(i).getIdArticulo() == detalleVenta.getIdArticulo()) {
                spinnerArticuloVenta.setSelection(i);
                break;
            }
        }

        editTextFechaDetalleVenta.setText(detalleVenta.getFechaDeVenta());
        editTextUnitarioDetalleVenta.setText(String.format(Locale.getDefault(), "%.2f", detalleVenta.getPrecioUnitarioVenta()));
        editTextCantidadDetalleVenta.setText(String.valueOf(detalleVenta.getCantidadVenta()));
        editTextTotalDetalleVenta.setText(String.format(Locale.getDefault(), "%.2f", detalleVenta.getTotalDetalleVenta()));


        editTextFechaDetalleVenta.setInputType(InputType.TYPE_NULL);
        editTextFechaDetalleVenta.setFocusable(false);
        editTextFechaDetalleVenta.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, day) -> {
                editTextFechaDetalleVenta.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDetalleVenta);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDetalleVenta);
        btnLimpiar.setVisibility(View.GONE);


        List<View> vistas = Arrays.asList(
                editTextFechaDetalleVenta, editTextUnitarioDetalleVenta, editTextCantidadDetalleVenta,
                editTextTotalDetalleVenta
        );
        List<String> listaRegex = Arrays.asList(
                "\\d{4}-\\d{2}-\\d{2}", "\\d+(\\.\\d{1,2})?", "\\d+", "\\d+(\\.\\d{1,2})?"
        );
        List<Integer> mensajesDeError = Arrays.asList(
                R.string.invalid_date, R.string.only_numbers_price, R.string.only_numbers_quantity, R.string.only_numbers_total
        );
        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);
        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                try {
                    String fecha = editTextFechaDetalleVenta.getText().toString().trim();
                    double precioUnitario = Double.parseDouble(editTextUnitarioDetalleVenta.getText().toString().trim());
                    int cantidad = Integer.parseInt(editTextCantidadDetalleVenta.getText().toString().trim());
                    double totalDetalle = Double.parseDouble(editTextTotalDetalleVenta.getText().toString().trim());


                    detalleVenta.setFechaDeVenta(fecha);
                    detalleVenta.setPrecioUnitarioVenta(precioUnitario);
                    detalleVenta.setCantidadVenta(cantidad);
                    detalleVenta.setTotalDetalleVenta(totalDetalle);


                    detalleVentaDAO.updateDetalleVenta(detalleVenta);
                    fillList();
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.error_updating_data_format, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.error_updating_data, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }


    private void deleteDetalleVenta(final DetalleVenta detalleVenta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete_detalle_venta);
        builder.setMessage(getString(R.string.confirm_delete_message_detalle_venta) + "\nID Detalle: " + detalleVenta.getIdVentaDetalle() + "\nArtículo ID: " + detalleVenta.getIdArticulo()); // Add this string

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            detalleVentaDAO.deleteDetalleVenta(detalleVenta.getIdCliente(), detalleVenta.getIdVenta(),
                    detalleVenta.getIdArticulo(), detalleVenta.getIdVentaDetalle());
            // Toast is shown in DAO
            fillList();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void calcularTotal(EditText precioEditText, EditText cantidadEditText, EditText totalEditText) {
        String precioStr = precioEditText.getText().toString().trim();
        String cantidadStr = cantidadEditText.getText().toString().trim();
        if (!precioStr.isEmpty() && !cantidadStr.isEmpty()) {
            try {
                double precio = Double.parseDouble(precioStr);
                int cantidad = Integer.parseInt(cantidadStr);
                double total = precio * cantidad;
                totalEditText.setText(String.format(Locale.getDefault(), "%.2f", total));
            } catch (NumberFormatException e) {
                totalEditText.setText("");
            }
        } else {
            totalEditText.setText("");
        }
    }

    private void clearFieldsDetalleVenta(EditText editTextIdVentaDetalle, EditText editTextFechaDetalleVenta,
                                         EditText editTextUnitarioDetalleVenta, EditText editTextCantidadDetalleVenta,
                                         EditText editTextTotalDetalleVenta, Spinner spinnerArticuloVenta,
                                         Spinner spinnerFacturaVenta) {
        editTextIdVentaDetalle.setText("");
        editTextFechaDetalleVenta.setText("");
        editTextUnitarioDetalleVenta.setText("");
        editTextCantidadDetalleVenta.setText("");
        editTextTotalDetalleVenta.setText("");
        if (spinnerArticuloVenta.getAdapter() != null && spinnerArticuloVenta.getAdapter().getCount() > 0) spinnerArticuloVenta.setSelection(0);
        if (spinnerFacturaVenta.getAdapter() != null && spinnerFacturaVenta.getAdapter().getCount() > 0) spinnerFacturaVenta.setSelection(0);
    }


    private void buscarDetalleVentaPorIdVentaDetalle(int idVentaDetalle) {

        DetalleVenta foundDetalle = null;
        for (DetalleVenta dv : detalleVentaDAO.getAllDetalleVenta()) {
            if (dv.getIdVentaDetalle() == idVentaDetalle) {
                foundDetalle = dv;
                break;
            }
        }

        if (foundDetalle != null) {
            viewDetalleVenta(foundDetalle);
        } else {
            Toast.makeText(this, R.string.not_found_message_detalle_venta, Toast.LENGTH_SHORT).show();
        }
    }
}
