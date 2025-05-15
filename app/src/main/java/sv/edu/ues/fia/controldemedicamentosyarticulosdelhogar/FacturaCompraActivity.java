package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class FacturaCompraActivity extends AppCompatActivity {

    private FacturaCompraDAO facturaCompraDAO;
    private ArrayAdapter<FacturaCompra> adaptadorFacturaCompra;
    private List<FacturaCompra> listaFacturaCompra;
    private ListView listViewFacturaCompra;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private DetalleCompraDAO detalleCompraDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_factura_compra);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        facturaCompraDAO = new FacturaCompraDAO(conexionDB, this);
        detalleCompraDAO = new DetalleCompraDAO(conexionDB, this);

        TextView txtBusqueda = (TextView) findViewById(R.id.busquedaFacturaCompra);

        Button btnBuscarFacturaCompraPorId = findViewById(R.id.btnBuscarFacturaCompra);
        btnBuscarFacturaCompraPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarFacturaCompraPorId.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarFacturaCompraPorId(id);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });


        listViewFacturaCompra = findViewById(R.id.lvFacturaCompra);
        listViewFacturaCompra.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        listViewFacturaCompra.setOnItemClickListener((parent, view, position, id) -> {
            FacturaCompra facturaCompra = (FacturaCompra) parent.getItemAtPosition(position);
            showOptionsDialog(facturaCompra);
        });

        Button btnAgregarFacturaCompra = findViewById(R.id.btnAgregarFacturaCompra);
        btnAgregarFacturaCompra.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarFacturaCompra.setOnClickListener(v -> showAddDialog());
    }

    private void fillList() {
        listaFacturaCompra = facturaCompraDAO.getAllFacturaCompra();
        adaptadorFacturaCompra = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFacturaCompra);
        listViewFacturaCompra.setAdapter(adaptadorFacturaCompra);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_factura_compra, null);
        builder.setView(dialogView);

        EditText editTextIdFactura = dialogView.findViewById(R.id.editTextIdFactura);
        EditText editTextFechaCompra = dialogView.findViewById(R.id.editTextFechaCompra);
        EditText editTextTotalCompra = dialogView.findViewById(R.id.editTextTotalCompra);

        Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        Spinner spinnerProveedor = dialogView.findViewById(R.id.spinnerProveedor);

        // llenar los tipo combobox
        List<SucursalFarmacia> farmacias = facturaCompraDAO.getAllFarmacias();
        farmacias.add(0, new SucursalFarmacia(-1, getString(R.string.select_farmacia)));
        ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<SucursalFarmacia>(this, android.R.layout.simple_spinner_item, farmacias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                    
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia() + " (ID: " + sucursalFarmacia.getIdFarmacia() + ")");
                    
                }
                return view;
            }
        };
        adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmacia.setAdapter(adapterFarmacia);

        // Obtener lista de proveedores
        List<Proveedor> proveedores = facturaCompraDAO.getAllProveedores();
        proveedores.add(0, new Proveedor(-1, getString(R.string.select_proveedor), this));
        ArrayAdapter<Proveedor> adapterProveedor = new ArrayAdapter<Proveedor>(this, android.R.layout.simple_spinner_item, proveedores) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                } else {
                    view.setText(proveedor.getNombreProveedor());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                    
                } else {
                    view.setText(proveedor.getNombreProveedor() + " (ID: " + proveedor.getIdProveedor() + ")");
                    
                }
                return view;
            }
        };
        adapterProveedor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProveedor.setAdapter(adapterProveedor);

        Button btnGuardarFacturaCompra = dialogView.findViewById(R.id.btnGuardarFacturaCompra);
        Button btnLimpiarFacturaCompra = dialogView.findViewById(R.id.btnLimpiarFacturaCompra);

        editTextFechaCompra.setInputType(InputType.TYPE_NULL);
        editTextFechaCompra.setFocusable(false);

        editTextTotalCompra.setText("0");
        editTextTotalCompra.setFocusable(false);
        editTextTotalCompra.setClickable(false);

        //Autoincrement factura compra
        int idFacturaCompra = facturaCompraDAO.obtenerIdFacturaCompra();
        editTextIdFactura.setText(String.valueOf(idFacturaCompra));
        editTextIdFactura.setEnabled(false);

        editTextFechaCompra.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // seleccionada la fecha y la mete en el EditText
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextFechaCompra.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        List<View> vistas = Arrays.asList(editTextIdFactura, editTextFechaCompra, editTextTotalCompra, spinnerFarmacia, spinnerProveedor);
        List<String> listaRegex = Arrays.asList(
                "\\d+",
                "\\d{4}-\\d{2}-\\d{2}",
                "\\d+(\\.\\d{1,2})?",
                "\\d+",
                "\\d+"
        );

        List<Integer> mensajesDeError = Arrays.asList(R.string.only_numbers, R.string.invalid_date,
                R.string.only_numbers, R.string.select_farmacia, R.string.select_proveedor);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();

        btnGuardarFacturaCompra.setOnClickListener(v -> {
            if(validadorDeCampos.validarCampos()){
                saveFacturaCompra(editTextIdFactura, editTextFechaCompra, editTextTotalCompra, spinnerFarmacia, spinnerProveedor);
                dialog.dismiss();
            }
        });
        btnLimpiarFacturaCompra.setOnClickListener(v -> clearFieldsFacturaCompra(editTextFechaCompra, spinnerFarmacia, spinnerProveedor));
        dialog.show();
    }

    private void saveFacturaCompra(EditText editTextIdFactura, EditText editTextFechaCompra, EditText editTextTotalCompra, Spinner spinnerFarmacia, Spinner spinnerProveedor) {
        int id = Integer.parseInt(editTextIdFactura.getText().toString());
        String fecha = editTextFechaCompra.getText().toString().trim();
        double total = Double.parseDouble(editTextTotalCompra.getText().toString());

        // obtener los proveedores o farmacias que estan seleccionados
        SucursalFarmacia farmaciaSeleccionada = (SucursalFarmacia) spinnerFarmacia.getSelectedItem();
        Proveedor proveedorSeleccionado = (Proveedor) spinnerProveedor.getSelectedItem();

        FacturaCompra facturaCompra = new FacturaCompra(id, farmaciaSeleccionada.getIdFarmacia(), proveedorSeleccionado.getIdProveedor(), fecha, total, this);
        Toast.makeText(this, R.string.save_message, Toast.LENGTH_SHORT).show();
        facturaCompraDAO.addFacturaCompra(facturaCompra);
        fillList();
        clearFieldsFacturaCompra(editTextFechaCompra, spinnerFarmacia, spinnerProveedor);
    }

    private void showOptionsDialog(final FacturaCompra facturaCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        Button btnVerDetalles = dialogView.findViewById(R.id.buttonViewDetails);

        if (this instanceof FacturaCompraActivity) {
            if(vac.validarAcceso(2)){
                btnVerDetalles.setVisibility(View.VISIBLE);
                btnVerDetalles.setEnabled(true);
                btnVerDetalles.setOnClickListener(v -> {
                    mostrarDetallesFactura(facturaCompra.getIdCompra());
                    dialog.dismiss();
                });
            }else{
                Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    viewFacturaCompra(facturaCompra);
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2)){
                    dialog.dismiss();
                    editFacturaCompra(facturaCompra);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(4))
                    deleteFacturaCompra(facturaCompra.getIdCompra());
                else
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void viewFacturaCompra(FacturaCompra facturaCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_factura_compra, null);
        builder.setView(dialogView);

        TextView tvIdFactura = dialogView.findViewById(R.id.editTextIdFactura);
        TextView tvFechaCompra = dialogView.findViewById(R.id.editTextFechaCompra);
        TextView tvTotalCompra = dialogView.findViewById(R.id.editTextTotalCompra);
        Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        Spinner spinnerProveedor = dialogView.findViewById(R.id.spinnerProveedor);

        tvIdFactura.setEnabled(false);
        tvFechaCompra.setEnabled(false);
        tvTotalCompra.setEnabled(false);
        tvIdFactura.setFocusable(false);
        tvFechaCompra.setFocusable(false);
        tvTotalCompra.setFocusable(false);

        spinnerFarmacia.setEnabled(false);
        spinnerProveedor.setEnabled(false);

        // desabiliat los botones del dialogo
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarFacturaCompra);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFacturaCompra);

        if (btnGuardar != null) {
            btnGuardar.setVisibility(View.GONE);
        }
        if (btnLimpiar != null) {
            btnLimpiar.setVisibility(View.GONE);
        }


        tvIdFactura.setText(String.valueOf(facturaCompra.getIdCompra()));
        tvFechaCompra.setText(facturaCompra.getFechaCompra());
        double totalCalculado = calcularTotalFactura(facturaCompra.getIdCompra());
        tvTotalCompra.setText(String.format(Locale.getDefault(), "%.2f", totalCalculado));

        // obtener lista de sucursales y proveedores
        List<SucursalFarmacia> farmacias = facturaCompraDAO.getAllFarmacias();
        List<Proveedor> proveedores = facturaCompraDAO.getAllProveedores();

        ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<SucursalFarmacia>(this, android.R.layout.simple_spinner_item, farmacias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                    
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia() + " (ID: " + sucursalFarmacia.getIdFarmacia() + ")");
                    
                }
                return view;
            }
        };

        adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmacia.setAdapter(adapterFarmacia);

        // Obtener lista de proveedores
        ArrayAdapter<Proveedor> adapterProveedor = new ArrayAdapter<Proveedor>(this, android.R.layout.simple_spinner_item, proveedores) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                } else {
                    view.setText(proveedor.getNombreProveedor());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                    
                } else {
                    view.setText(proveedor.getNombreProveedor() + " (ID: " + proveedor.getIdProveedor() + ")");
                    
                }
                return view;
            }
        };

        adapterProveedor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProveedor.setAdapter(adapterProveedor);

        // selecciona los valores que recupera de la db en los spinner
        for (int i = 0; i < farmacias.size(); i++) {
            if (farmacias.get(i).getIdFarmacia() == facturaCompra.getIdFarmacia()) {
                spinnerFarmacia.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).getIdProveedor() == facturaCompra.getIdProveedor()) {
                spinnerProveedor.setSelection(i);
                break;
            }
        }

        builder.show();
    }


    private void editFacturaCompra(FacturaCompra facturaCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_factura_compra, null);
        builder.setView(dialogView);

        EditText editTextIdFactura = dialogView.findViewById(R.id.editTextIdFactura);
        EditText editTextFechaCompra = dialogView.findViewById(R.id.editTextFechaCompra);
        EditText editTextTotalCompra = dialogView.findViewById(R.id.editTextTotalCompra);

        Spinner spinnerFarmacia = dialogView.findViewById(R.id.spinnerFarmacia);
        Spinner spinnerProveedor = dialogView.findViewById(R.id.spinnerProveedor);

        List<SucursalFarmacia> farmacias = facturaCompraDAO.getAllFarmacias();
        ArrayAdapter<SucursalFarmacia> adapterFarmacia = new ArrayAdapter<SucursalFarmacia>(this, android.R.layout.simple_spinner_item, farmacias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SucursalFarmacia sucursalFarmacia = getItem(position);
                if (sucursalFarmacia.getIdFarmacia() == -1) {
                    view.setText(getString(R.string.select_farmacia));
                    
                } else {
                    view.setText(sucursalFarmacia.getNombreFarmacia() + " (ID: " + sucursalFarmacia.getIdFarmacia() + ")");
                    
                }
                return view;
            }
        };

        adapterFarmacia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFarmacia.setAdapter(adapterFarmacia);

        // Obtener lista de proveedores
        List<Proveedor> proveedores = facturaCompraDAO.getAllProveedores();
        ArrayAdapter<Proveedor> adapterProveedor = new ArrayAdapter<Proveedor>(this, android.R.layout.simple_spinner_item, proveedores) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                } else {
                    view.setText(proveedor.getNombreProveedor());
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Proveedor proveedor = getItem(position);
                if (proveedor.getIdProveedor() == -1) {
                    view.setText(getString(R.string.select_proveedor));
                    
                } else {
                    view.setText(proveedor.getNombreProveedor() + " (ID: " + proveedor.getIdProveedor() + ")");
                    
                }
                return view;
            }
        };
        adapterProveedor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProveedor.setAdapter(adapterProveedor);

        editTextIdFactura.setText(String.valueOf(facturaCompra.getIdCompra()));
        editTextFechaCompra.setText(facturaCompra.getFechaCompra());

        editTextTotalCompra.setFocusable(false);
        editTextTotalCompra.setClickable(false);

        double totalCalculado = calcularTotalFactura(facturaCompra.getIdCompra());
        editTextTotalCompra.setText(String.format(Locale.getDefault(), "%.2f", totalCalculado));


        editTextIdFactura.setEnabled(false);
        editTextIdFactura.setFocusable(false);

        for (int i = 0; i < farmacias.size(); i++) {
            if (farmacias.get(i).getIdFarmacia() == facturaCompra.getIdFarmacia()) {
                spinnerFarmacia.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).getIdProveedor() == facturaCompra.getIdProveedor()) {
                spinnerProveedor.setSelection(i);
                break;
            }
        }

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarFacturaCompra);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarFacturaCompra);
        btnLimpiar.setEnabled(false);

        editTextFechaCompra.setInputType(InputType.TYPE_NULL);
        editTextFechaCompra.setFocusable(false);

        editTextFechaCompra.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // fecha seleccionada y la mostramos en el EditText
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextFechaCompra.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        List<View> vistas = Arrays.asList(editTextIdFactura, editTextFechaCompra, editTextTotalCompra);
        List<String> listaRegex = Arrays.asList(
                "\\d+",
                "\\d{4}-\\d{2}-\\d{2}",
                "\\d+(\\.\\d{1,2})?"
        );

        List<Integer> mensajesDeError = Arrays.asList(R.string.only_numbers, R.string.invalid_date,
                R.string.only_numbers);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if(validadorDeCampos.validarCampos()){
                String nuevaFecha = editTextFechaCompra.getText().toString().trim();
                double nuevoTotal = Double.parseDouble(editTextTotalCompra.getText().toString());
                SucursalFarmacia nuevaFarmacia = (SucursalFarmacia) spinnerFarmacia.getSelectedItem();
                Proveedor nuevoProveedor = (Proveedor) spinnerProveedor.getSelectedItem();

                facturaCompra.setFechaCompra(nuevaFecha);
                facturaCompra.setTotalCompra(nuevoTotal);
                facturaCompra.setIdFarmacia(nuevaFarmacia.getIdFarmacia());
                facturaCompra.setIdProveedor(nuevoProveedor.getIdProveedor());

                facturaCompraDAO.updateFacturaCompra(facturaCompra);
                Toast.makeText(this, R.string.update_message, Toast.LENGTH_SHORT).show();
                fillList();
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void deleteFacturaCompra(int idFacturaCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idFacturaCompra);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            facturaCompraDAO.deleteFacturaCompra(idFacturaCompra);
            Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
            fillList();
        });

        builder.setNegativeButton(R.string.no, ((dialog, which) -> dialog.dismiss()));

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void mostrarDetallesFactura(int idCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_detalles, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.purchase_detail) + " #" + idCompra);

        ListView listView = view.findViewById(R.id.listaDetallesCompra);
        TextView tvTotal = view.findViewById(R.id.tvTotalFactura);

        List<Articulo> articulos = detalleCompraDAO.getAllArticulo();
        Map<Integer, String> mapaNombres = new HashMap<>();
        for (Articulo a : articulos) {
            mapaNombres.put(a.getIdArticulo(), a.getNombreArticulo());
        }

        List<DetalleCompra> detalles = detalleCompraDAO.getDetallesCompra(idCompra);
        List<String> lineas = new ArrayList<>();
        double total = 0;

        for (DetalleCompra d : detalles) {
            double subtotal = d.getTotalDetalleCompra();
            total += subtotal;

            String nombre = mapaNombres.containsKey(d.getIdArticulo()) ?
                    mapaNombres.get(d.getIdArticulo()) : getString(R.string.item) + " " + d.getIdArticulo();

            lineas.add(" | ID: " + d.getIdArticulo() + " | "
                    +
                    nombre
                    + " | " + getString(R.string.quantity) + " : " + d.getCantidadCompra()
                    + " | " + getString(R.string.price) + " : $ " + d.getPrecioUnitarioCompra()
                    + " | Subtotal: $" + String.format("%.2f", d.getTotalDetalleCompra()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lineas);
        listView.setAdapter(adapter);
        tvTotal.setText("Total: $" + String.format("%.2f", total));

        builder.setPositiveButton(getString(R.string.close), null);
        builder.create().show();
    }

    private double calcularTotalFactura(int idCompra) {
        List<DetalleCompra> detalles = detalleCompraDAO.getDetallesCompra(idCompra);
        double total = 0;
        for (DetalleCompra d : detalles) {
            total += d.getTotalDetalleCompra();
        }
        return total;
    }


    private void clearFieldsFacturaCompra(EditText editTextFechaCompra, Spinner spinnerFarmacia, Spinner spinnerProveedor) {
        editTextFechaCompra.setText("");
        spinnerFarmacia.setSelection(0);
        spinnerProveedor.setSelection(0);
    }

    private void buscarFacturaCompraPorId(int id) {
        FacturaCompra facturaCompra = facturaCompraDAO.getFacturaCompra(id);
        if(facturaCompra != null) {
            viewFacturaCompra(facturaCompra);
        }
        else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }
}