package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Collections;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.view.ViewGroup;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class FacturaVentaActivity extends AppCompatActivity {
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private ListView listViewFacturasVenta;
    private Button btnAddFacturaVenta;
    private FacturaVentaDAO facturaVentaDAO;
    private ClienteDAO clienteDAO;
    private DetalleVentaDAO detalleVentaDAO;
    private ArrayAdapter<FacturaVenta> adapterFacturasVenta;
    private List<FacturaVenta> listaFacturasVenta;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_factura_venta);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        facturaVentaDAO = new FacturaVentaDAO(conexionDB, this);
        clienteDAO = new ClienteDAO(conexionDB, this);
        detalleVentaDAO = new DetalleVentaDAO(conexionDB, this);

        TextView txtBusquedaFacturaVenta = findViewById(R.id.txtBusquedaFacturaVenta);
        Button btnBuscarFacturaVenta = findViewById(R.id.btnBuscarFacturaVenta);
        btnBuscarFacturaVenta.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        if (btnBuscarFacturaVenta != null) {
            btnBuscarFacturaVenta.setOnClickListener(v -> {
                try {
                    if (txtBusquedaFacturaVenta.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, R.string.field_empty, Toast.LENGTH_LONG).show();
                        return;
                    }
                    int id = Integer.parseInt(txtBusquedaFacturaVenta.getText().toString().trim());
                    buscarFacturaVentaPorId(id);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
                }
            });
        }


        listViewFacturasVenta = findViewById(R.id.listViewFacturasVenta);
        listViewFacturasVenta.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        btnAddFacturaVenta = findViewById(R.id.btnAddFacturaVenta);
        btnAddFacturaVenta.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAddFacturaVenta.setOnClickListener(v -> showAddDialog());

        listViewFacturasVenta.setOnItemClickListener((parent, view, position, id) -> {
            FacturaVenta facturaSeleccionada = (FacturaVenta) parent.getItemAtPosition(position);
            showOptionsDialog(facturaSeleccionada);
        });
    }


    private void mostrarDetallesVenta(FacturaVenta factura) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_detalles, null);
        builder.setView(view);

        builder.setTitle(getString(R.string.sale_detail) + " #" + factura.getIdVenta());


        ListView listView = view.findViewById(R.id.listaDetallesCompra);
        TextView tvTotal = view.findViewById(R.id.tvTotalFactura);
        TextView tvTitle = view.findViewById(R.id.tvTituloDetalles);


        tvTitle.setText(getString(R.string.sale_detail) + " #" + factura.getIdVenta());


        List<Articulo> articulos = detalleVentaDAO.getAllArticulo();
        Map<Integer, String> mapaNombresArticulos = new HashMap<>();
        for (Articulo a : articulos) {
            mapaNombresArticulos.put(a.getIdArticulo(), a.getNombreArticulo());
        }


        List<DetalleVenta> detalles = detalleVentaDAO.getDetallesVenta(factura.getIdCliente(), factura.getIdVenta());
        List<String> lineasDetalle = new ArrayList<>();
        double totalCalculado = 0;

        for (DetalleVenta d : detalles) {
            double subtotal = d.getTotalDetalleVenta();

            totalCalculado += subtotal;

            String nombreArticulo = mapaNombresArticulos.containsKey(d.getIdArticulo()) ?
                    mapaNombresArticulos.get(d.getIdArticulo()) : getString(R.string.item) + " " + d.getIdArticulo();


            lineasDetalle.add(" | ID: " + d.getIdArticulo() + " | " + nombreArticulo
                    + " | " + getString(R.string.quantity_sale) + ": " + d.getCantidadVenta()
                    + " | " + getString(R.string.unit_price_sale) + ": $" + String.format(Locale.US, "%.2f", d.getPrecioUnitarioVenta())
                    + " | Subtotal: $" + String.format(Locale.US, "%.2f", subtotal));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lineasDetalle);
        listView.setAdapter(adapter);
        tvTotal.setText("Total: $" + String.format(Locale.US, "%.2f", totalCalculado));

        builder.setPositiveButton(getString(R.string.close), null);
        builder.create().show();
    }


    private void fillList() {
        listaFacturasVenta = facturaVentaDAO.getAllFacturasVenta();
        adapterFacturasVenta = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFacturasVenta);
        listViewFacturasVenta.setAdapter(adapterFacturasVenta);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_factura_venta, null);
        builder.setView(dialogView);

        final EditText edtIdVenta = dialogView.findViewById(R.id.edtIdVentaFactura);
        final Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerCliente);
        final Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        final EditText edtFechaVenta = dialogView.findViewById(R.id.edtFechaVentaFactura);
        final EditText edtTotalVenta = dialogView.findViewById(R.id.edtTotalVentaFactura);


        edtTotalVenta.setText("0");
        edtTotalVenta.setFocusable(false);
        edtTotalVenta.setClickable(false);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarFacturaVenta);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFacturaVenta);


        int nextIdVenta = facturaVentaDAO.obtenerIdFacturaVenta();
        edtIdVenta.setText(String.valueOf(nextIdVenta));
        edtIdVenta.setEnabled(false);

        setupDatePicker(edtFechaVenta);
        edtFechaVenta.setText(dateFormat.format(new Date()));


        List<Cliente> clientes = facturaVentaDAO.getAllClientes();
        clientes.add(0, new Cliente(0, getString(R.string.select_client), this));
        ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, clientes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                label.setText(getItem(position).getNombreCliente());
                return label;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                label.setText(getItem(position).getNombreCliente());
                return label;
            }
        };
        adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(adapterCliente);


        List<SucursalFarmacia> farmacias = facturaVentaDAO.getAllFarmacias();
        farmacias.add(0, new SucursalFarmacia(0, getString(R.string.select_farmacia)));
        ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmacias);
        adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmacia.setAdapter(adapterFarmacia);


        List<View> vistas = Arrays.asList(edtFechaVenta, spinnerCliente, spinnerFarmacia);
        List<String> regex = Arrays.asList(
                "\\d{4}-\\d{2}-\\d{2}",
                ".+",
                ".+"
        );
        List<Integer> errores = Arrays.asList(
                R.string.invalid_date,
                R.string.select_client_error,
                R.string.select_farmacia_error
        );
        ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, errores) {
            @Override
            public boolean validarCampos() {
                boolean camposValidos = super.validarCampos();
                if (((Cliente) spinnerCliente.getSelectedItem()).getIdCliente() == 0) {
                    ((TextView)spinnerCliente.getSelectedView()).setError(FacturaVentaActivity.this.getString(R.string.select_client_error));
                    camposValidos = false;
                } else {
                    ((TextView)spinnerCliente.getSelectedView()).setError(null);
                }
                if (((SucursalFarmacia) spinnerFarmacia.getSelectedItem()).getIdFarmacia() == 0) {
                    ((TextView)spinnerFarmacia.getSelectedView()).setError(FacturaVentaActivity.this.getString(R.string.select_farmacia_error));
                    camposValidos = false;
                } else {
                    ((TextView)spinnerFarmacia.getSelectedView()).setError(null);
                }
                return camposValidos;
            }
        };


        final AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (validador.validarCampos()) {
                saveFacturaVenta(edtIdVenta, spinnerCliente, spinnerFarmacia, edtFechaVenta, edtTotalVenta);
                dialog.dismiss();
            }
        });

        btnLimpiar.setOnClickListener(v -> clearFieldsFacturaVenta(edtIdVenta, spinnerCliente, spinnerFarmacia, edtFechaVenta, edtTotalVenta));
        dialog.show();
    }

    private void saveFacturaVenta(EditText edtIdVenta, Spinner spinnerCliente, Spinner spinnerFarmacia, EditText edtFechaVenta, EditText edtTotalVenta) {
        try {
            int idVenta = Integer.parseInt(edtIdVenta.getText().toString());
            Cliente clienteSeleccionado = (Cliente) spinnerCliente.getSelectedItem();
            SucursalFarmacia farmaciaSeleccionada = (SucursalFarmacia) spinnerFarmacia.getSelectedItem();
            String fecha = edtFechaVenta.getText().toString().trim();
            double total = Double.parseDouble(edtTotalVenta.getText().toString().trim());

            FacturaVenta nuevaFactura = new FacturaVenta(idVenta, clienteSeleccionado.getIdCliente(), farmaciaSeleccionada.getIdFarmacia(), fecha, total, this);
            facturaVentaDAO.addFacturaVenta(nuevaFactura);
            fillList();
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_processing_data, Toast.LENGTH_SHORT).show();
        }
    }

    private void showOptionsDialog(final FacturaVenta factura) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();


        Button btnViewDetails = dialogView.findViewById(R.id.buttonViewDetails);
        if (btnViewDetails != null) {
            btnViewDetails.setVisibility(View.VISIBLE);
            btnViewDetails.setText(R.string.sale_detail);
            btnViewDetails.setOnClickListener(v -> {
                if (vac.validarAcceso(2)) {
                    mostrarDetallesVenta(factura);
                } else {
                    Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> {
            if (vac.validarAcceso(2)) viewFacturaVenta(factura);
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            if (vac.validarAcceso(3)) editFacturaVenta(factura); // Renamed
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            if (vac.validarAcceso(4)) deleteFacturaVenta(factura.getIdVenta());
            else Toast.makeText(this, R.string.action_block, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void viewFacturaVenta(FacturaVenta factura) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_factura_venta, null);
        builder.setView(dialogView);

        final EditText edtIdVenta = dialogView.findViewById(R.id.edtIdVentaFactura);
        final Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerCliente);
        final Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        final EditText edtFechaVenta = dialogView.findViewById(R.id.edtFechaVentaFactura);
        final EditText edtTotalVenta = dialogView.findViewById(R.id.edtTotalVentaFactura);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarFacturaVenta);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFacturaVenta);

        edtIdVenta.setText(String.valueOf(factura.getIdVenta()));
        edtIdVenta.setEnabled(false);

        Cliente clienteFactura = facturaVentaDAO.getClienteById(factura.getIdCliente());
        if (clienteFactura != null) {
            ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<Cliente>(
                    this, android.R.layout.simple_spinner_item, Collections.singletonList(clienteFactura)
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView label = (TextView) super.getView(position, convertView, parent);
                    if (getItem(position) != null) {
                        label.setText(getItem(position).getNombreCliente());
                    }
                    return label;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                    if (getItem(position) != null) {
                        label.setText(getItem(position).getNombreCliente());
                    }
                    return label;
                }
            };
            adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCliente.setAdapter(adapterCliente);
            spinnerCliente.setSelection(0);
        }
        spinnerCliente.setEnabled(false);

        SucursalFarmacia farmaciaFactura = facturaVentaDAO.getFarmaciaById(factura.getIdFarmacia());
        if (farmaciaFactura != null) {
            ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList(farmaciaFactura));
            adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFarmacia.setAdapter(adapterFarmacia);
            spinnerFarmacia.setSelection(0);
        }
        spinnerFarmacia.setEnabled(false);

        edtFechaVenta.setText(factura.getFechaVenta());
        edtFechaVenta.setEnabled(false);
        edtFechaVenta.setOnClickListener(null);

        double totalCalculado = calcularTotalVenta(factura.getIdCliente(), factura.getIdVenta());
        edtTotalVenta.setText(String.format(Locale.US, "%.2f", totalCalculado));
        edtTotalVenta.setEnabled(false);

        if (btnGuardar != null) btnGuardar.setVisibility(View.GONE);
        if (btnLimpiar != null) btnLimpiar.setVisibility(View.GONE);

        builder.setNeutralButton(R.string.close, (d, w) -> d.dismiss());
        builder.create().show();
    }

    private void editFacturaVenta(final FacturaVenta facturaOriginal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit); // Consistent title

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_factura_venta, null);
        builder.setView(dialogView);

        final EditText edtIdVenta = dialogView.findViewById(R.id.edtIdVentaFactura);
        final Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerCliente);
        final Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        final EditText edtFechaVenta = dialogView.findViewById(R.id.edtFechaVentaFactura);
        final EditText edtTotalVenta = dialogView.findViewById(R.id.edtTotalVentaFactura);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarFacturaVenta);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFacturaVenta);


        edtIdVenta.setText(String.valueOf(facturaOriginal.getIdVenta()));
        edtIdVenta.setEnabled(false);


        List<Cliente> todosLosClientes = facturaVentaDAO.getAllClientes();
        ArrayAdapter<Cliente> adapterCliente = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, todosLosClientes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                label.setText(getItem(position).getNombreCliente());
                return label;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView label = (TextView) super.getDropDownView(position, convertView, parent);
                label.setText(getItem(position).getNombreCliente());
                return label;
            }
        };
        adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(adapterCliente);
        for (int i = 0; i < todosLosClientes.size(); i++) {
            if (todosLosClientes.get(i).getIdCliente() == facturaOriginal.getIdCliente()) {
                spinnerCliente.setSelection(i);
                break;
            }
        }
        spinnerCliente.setEnabled(true);



        List<SucursalFarmacia> todasLasFarmacias = facturaVentaDAO.getAllFarmacias();
        ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, todasLasFarmacias);
        adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmacia.setAdapter(adapterFarmacia);
        for (int i = 0; i < todasLasFarmacias.size(); i++) {
            if (todasLasFarmacias.get(i).getIdFarmacia() == facturaOriginal.getIdFarmacia()) {
                spinnerFarmacia.setSelection(i);
                break;
            }
        }

        edtFechaVenta.setText(facturaOriginal.getFechaVenta());
        setupDatePicker(edtFechaVenta);


        edtTotalVenta.setFocusable(false);
        edtTotalVenta.setClickable(false);
        double totalCalculado = calcularTotalVenta(facturaOriginal.getIdCliente(), facturaOriginal.getIdVenta());
        edtTotalVenta.setText(String.format(Locale.US, "%.2f", totalCalculado));


        if(btnLimpiar != null) btnLimpiar.setEnabled(false);


        List<View> vistas = Arrays.asList(edtFechaVenta);
        List<String> regex = Arrays.asList(
                "\\d{4}-\\d{2}-\\d{2}"
        );
        List<Integer> errores = Arrays.asList(
                R.string.invalid_date
        );
        ValidadorDeCampos validador = new ValidadorDeCampos(this, vistas, regex, errores);

        final AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            boolean validEditTexts = validador.validarCampos();
            boolean validSpinners = true;

            Cliente clienteSeleccionado = (Cliente) spinnerCliente.getSelectedItem();
            TextView errorTextCliente = (TextView)spinnerCliente.getSelectedView();
            if (clienteSeleccionado == null) {
                if(errorTextCliente != null) errorTextCliente.setError(getString(R.string.select_client_error));
                else Toast.makeText(this, R.string.select_client_error, Toast.LENGTH_SHORT).show();
                validSpinners = false;
            } else {
                if(errorTextCliente != null) errorTextCliente.setError(null);
            }

            SucursalFarmacia farmaciaSeleccionada = (SucursalFarmacia) spinnerFarmacia.getSelectedItem();
            TextView errorTextFarmacia = (TextView)spinnerFarmacia.getSelectedView();
            if (farmaciaSeleccionada == null) {
                if(errorTextFarmacia != null) errorTextFarmacia.setError(getString(R.string.select_farmacia_error));
                else Toast.makeText(this, R.string.select_farmacia_error, Toast.LENGTH_SHORT).show();
                validSpinners = false;
            } else {
                if(errorTextFarmacia != null) errorTextFarmacia.setError(null);
            }

            if (validEditTexts && validSpinners) {
                String fecha = edtFechaVenta.getText().toString().trim();
                double total = Double.parseDouble(edtTotalVenta.getText().toString().trim());

                FacturaVenta facturaActualizada = new FacturaVenta(
                        facturaOriginal.getIdVenta(),
                        clienteSeleccionado.getIdCliente(),
                        farmaciaSeleccionada.getIdFarmacia(),
                        fecha,
                        total,
                        this
                );
                facturaVentaDAO.updateFacturaVenta(facturaActualizada);
                fillList();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void deleteFacturaVenta(int idVenta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        FacturaVenta factura = facturaVentaDAO.getFacturaVentaById(idVenta);
        String facturaInfo = factura != null ? "ID " + factura.getIdVenta() : String.valueOf(idVenta);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + facturaInfo + "?");

        builder.setPositiveButton(R.string.yes, (d, which) -> {
            facturaVentaDAO.deleteFacturaVenta(idVenta);
            fillList();
            d.dismiss();
        });
        builder.setNegativeButton(R.string.no, ((d, which) -> d.dismiss()));
        builder.create().show();
    }

    private void buscarFacturaVentaPorId(int id) {
        FacturaVenta factura = facturaVentaDAO.getFacturaVentaById(id);
        if (factura != null) {
            viewFacturaVenta(factura);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFieldsFacturaVenta(EditText edtIdVenta, Spinner spinnerCliente, Spinner spinnerFarmacia, EditText edtFechaVenta, EditText edtTotalVenta) {

        spinnerCliente.setSelection(0);
        spinnerFarmacia.setSelection(0);
        edtFechaVenta.setText(dateFormat.format(new Date()));
        edtTotalVenta.setText("");
    }


    private double calcularTotalVenta(int idCliente, int idVenta) {
        List<DetalleVenta> detalles = detalleVentaDAO.getDetallesVenta(idCliente, idVenta);
        double total = 0;
        for (DetalleVenta d : detalles) {
            total += d.getTotalDetalleVenta();
        }
        return total;
    }

    private void setupDatePicker(final EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            try {
                Date existingDate = dateFormat.parse(editText.getText().toString());
                if (existingDate != null) {
                    calendar.setTime(existingDate);
                }
            } catch (ParseException e) {
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year1, monthOfYear, dayOfMonth);
                        editText.setText(dateFormat.format(newDate.getTime()));
                    }, year, month, day);
            datePickerDialog.show();
        });
    }
}
