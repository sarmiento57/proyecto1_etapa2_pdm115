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


public class DetalleCompraActivity extends AppCompatActivity {

    private DetalleCompraDAO detalleCompraDAO;
    private ArrayAdapter<DetalleCompra> adaptadorDetalleCompra;
    private List<DetalleCompra> listaDetalleCompra;
    private ListView listViewDetalleCompra;
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_compra);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        detalleCompraDAO = new DetalleCompraDAO(conexionDB, this);

        TextView txtBusqueda = findViewById(R.id.busquedaDetalleCompra);

        Button btnBuscarDetalleCompraPorId = findViewById(R.id.btnBuscarDetalleCompra);
        btnBuscarDetalleCompraPorId.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnBuscarDetalleCompraPorId.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarDetalleCompraPorId(id);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });

        listViewDetalleCompra = findViewById(R.id.lvDetalleCompra);
        listViewDetalleCompra.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);
        fillList();

        listViewDetalleCompra.setOnItemClickListener((parent, view, position, id) -> {
            DetalleCompra detalleCompra = (DetalleCompra) parent.getItemAtPosition(position);
            showOptionsDialog(detalleCompra);
        });

        Button btnAgregarDetalleCompra = findViewById(R.id.btnAgregarDetalleCompra);
        btnAgregarDetalleCompra.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        btnAgregarDetalleCompra.setOnClickListener(v -> showAddDialog());
    }

    private void fillList() {
        listaDetalleCompra = detalleCompraDAO.getAllDetalleCompra();
        adaptadorDetalleCompra = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleCompra);
        listViewDetalleCompra.setAdapter(adaptadorDetalleCompra);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_compra, null);
        builder.setView(dialogView);

        EditText editTextIdDetalleCompra = dialogView.findViewById(R.id.editTextIdDetalleCompra);
        EditText editTextFechaDetalleCompra = dialogView.findViewById(R.id.editTextFechaDetalleCompra);
        EditText editTextUnitarioDetalleCompra = dialogView.findViewById(R.id.editTextUnitarioDetalleCompra);
        EditText editTextCantidadDetalleCompra = dialogView.findViewById(R.id.editTextCantidadDetalleCompra);
        EditText editTextTotalDetalleCompra = dialogView.findViewById(R.id.editTextTotalDetalleCompra);
        Spinner spinnerArticuloCompra = dialogView.findViewById(R.id.spinnerArticuloCompra);
        Spinner spinnerFacturaCompra = dialogView.findViewById(R.id.spinnerFacturaCompra);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal(editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra, editTextTotalDetalleCompra);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextUnitarioDetalleCompra.addTextChangedListener(watcher);
        editTextCantidadDetalleCompra.addTextChangedListener(watcher);

        editTextTotalDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextTotalDetalleCompra.setFocusableInTouchMode(false);
        editTextTotalDetalleCompra.setFocusable(false);
        editTextTotalDetalleCompra.setClickable(false);

        editTextFechaDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextFechaDetalleCompra.setFocusableInTouchMode(false);
        editTextFechaDetalleCompra.setFocusable(false);
        editTextFechaDetalleCompra.setClickable(false);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDetalleCompra);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDetalleCompra);

        List<FacturaCompra> facturas = detalleCompraDAO.getAllFacturaCompra();
        facturas.add(0, new FacturaCompra(-1, -1, null, this));
        ArrayAdapter<FacturaCompra> adapterFactura = new ArrayAdapter<FacturaCompra>(this, android.R.layout.simple_spinner_item, facturas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                } else {
                    view.setText(getString(R.string.invoice_id) + ": " + factura.getIdCompra() + ", "  + getString(R.string.provider_id) + ": " + factura.getIdProveedor()); // Esto se muestra cuando está cerrado
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                } else {
                    view.setText("ID : " + factura.getIdCompra() + ", "  + getString(R.string.provider) + ": " + factura.getIdProveedor() + ", " + getString(R.string.purchase_date) + ": " + factura.getFechaCompra());
                }
                return view;
            }
        };

        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaCompra.setAdapter(adapterFactura);

        spinnerFacturaCompra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                FacturaCompra facturaSeleccionada = (FacturaCompra) parentView.getSelectedItem();
                if (facturaSeleccionada.getIdCompra() != -1 && facturaSeleccionada.getFechaCompra() != null) {
                    editTextFechaDetalleCompra.setText(facturaSeleccionada.getFechaCompra());
                } else {
                    editTextFechaDetalleCompra.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                editTextFechaDetalleCompra.setText("");
            }
        });

        List<Articulo> articulos = detalleCompraDAO.getAllArticulo();
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
        spinnerArticuloCompra.setAdapter(adapterArticulo);


        List<View> vistas = Arrays.asList(
                editTextIdDetalleCompra, editTextFechaDetalleCompra, editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra,
                editTextTotalDetalleCompra, spinnerArticuloCompra, spinnerFacturaCompra
        );

        List<String> listaRegex = Arrays.asList(
                "\\d+", "\\d{4}-\\d{2}-\\d{2}", "^(?!0(\\.0+)?$)\\d+(\\.\\d{1,2})?$",
                "^[1-9]\\d*$", "^(?!0(\\.0+)?$)\\d+(\\.\\d{1,2})?$", "\\d+", "\\d+"
        );

        List<Integer> mensajesDeError = Arrays.asList(
                R.string.only_numbers, R.string.invalid_date, R.string.only_numbers, R.string.only_numbers,
                R.string.only_numbers, R.string.select_articulo, R.string.select_factura
        );

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                saveDetalleCompra(
                        editTextIdDetalleCompra, editTextFechaDetalleCompra, editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra,
                        editTextTotalDetalleCompra, spinnerArticuloCompra, spinnerFacturaCompra
                );
                dialog.dismiss();
            }
        });

        btnLimpiar.setOnClickListener(v -> clearFieldsDetalleCompra(editTextIdDetalleCompra, editTextFechaDetalleCompra, editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra,
                editTextTotalDetalleCompra, spinnerArticuloCompra, spinnerFacturaCompra));
        dialog.show();
    }


    private void saveDetalleCompra(EditText editTextIdDetalleCompra, EditText editTextFechaDetalleCompra, EditText editTextUnitarioDetalleCompra,
                                   EditText editTextCantidadDetalleCompra, EditText editTextTotalDetalleCompra, Spinner spinnerArticuloCompra, Spinner spinnerFacturaCompra) {

        int idDetalleCompra = Integer.parseInt(editTextIdDetalleCompra.getText().toString());
        String fecha = editTextFechaDetalleCompra.getText().toString().trim();
        double precioUnitario = Double.parseDouble(editTextUnitarioDetalleCompra.getText().toString());
        int cantidadArticulos = Integer.parseInt(editTextCantidadDetalleCompra.getText().toString());
        double totalDetalle = Double.parseDouble(editTextTotalDetalleCompra.getText().toString());

        FacturaCompra facturaSeleccionada = (FacturaCompra) spinnerFacturaCompra.getSelectedItem();
        Articulo articuloSeleccionado = (Articulo) spinnerArticuloCompra.getSelectedItem();
            DetalleCompra detalleCompra = new DetalleCompra(facturaSeleccionada.getIdCompra(), articuloSeleccionado.getIdArticulo(), idDetalleCompra, fecha,
                    precioUnitario, cantidadArticulos, totalDetalle, this);
            detalleCompraDAO.addDetalleCompra(detalleCompra);
            fillList();

        clearFieldsDetalleCompra(editTextIdDetalleCompra, editTextFechaDetalleCompra, editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra,
                editTextTotalDetalleCompra, spinnerArticuloCompra, spinnerFacturaCompra
        );
    }

    private void showOptionsDialog(final DetalleCompra detalleCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    viewDetalleCompra(detalleCompra);
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
                    editDetalleCompra(detalleCompra);
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
                    deleteDetalleCompra(detalleCompra.getIdDetalleCompra());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    private void viewDetalleCompra(DetalleCompra detalleCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_compra, null);
        builder.setView(dialogView);

        EditText editTextIdDetalleCompra = dialogView.findViewById(R.id.editTextIdDetalleCompra);
        EditText editTextFechaDetalleCompra = dialogView.findViewById(R.id.editTextFechaDetalleCompra);
        EditText editTextUnitarioDetalleCompra = dialogView.findViewById(R.id.editTextUnitarioDetalleCompra);
        EditText editTextCantidadDetalleCompra = dialogView.findViewById(R.id.editTextCantidadDetalleCompra);
        EditText editTextTotalDetalleCompra = dialogView.findViewById(R.id.editTextTotalDetalleCompra);
        Spinner spinnerArticuloCompra = dialogView.findViewById(R.id.spinnerArticuloCompra);
        Spinner spinnerFacturaCompra = dialogView.findViewById(R.id.spinnerFacturaCompra);

        editTextIdDetalleCompra.setEnabled(false);
        editTextFechaDetalleCompra.setEnabled(false);
        editTextCantidadDetalleCompra.setEnabled(false);
        editTextTotalDetalleCompra.setEnabled(false);
        editTextUnitarioDetalleCompra.setEnabled(false);
        spinnerArticuloCompra.setEnabled(false);
        spinnerFacturaCompra.setEnabled(false);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDetalleCompra);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDetalleCompra);

        if (btnGuardar != null) btnGuardar.setVisibility(View.GONE);
        if (btnLimpiar != null) btnLimpiar.setVisibility(View.GONE);

        editTextIdDetalleCompra.setText(String.valueOf(detalleCompra.getIdDetalleCompra()));
        editTextFechaDetalleCompra.setText(detalleCompra.getFechaDeCompra());
        editTextUnitarioDetalleCompra.setText(String.valueOf(detalleCompra.getPrecioUnitarioCompra()));
        editTextTotalDetalleCompra.setText(String.valueOf(detalleCompra.getTotalDetalleCompra()));
        editTextCantidadDetalleCompra.setText(String.valueOf(detalleCompra.getCantidadCompra()));

        // obtener lista de facturas compra
        List<FacturaCompra> facturas = detalleCompraDAO.getAllFacturaCompra();
        ArrayAdapter<FacturaCompra> adapterFactura = new ArrayAdapter<FacturaCompra>(this, android.R.layout.simple_spinner_item, facturas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                } else {
                    view.setText(getString(R.string.invoice_id) + ": " + factura.getIdCompra() + ", "  + getString(R.string.provider_id) + ": " + factura.getIdProveedor()); // Esto se muestra cuando está cerrado
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                    
                } else {
                    view.setText("ID : " + factura.getIdCompra() + ", "  + getString(R.string.provider) + ": " + factura.getIdProveedor() + ", " + getString(R.string.purchase_date) + ": " + factura.getFechaCompra());
                    
                }
                return view;
            }
        };


        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaCompra.setAdapter(adapterFactura);

        List<Articulo> articulos = detalleCompraDAO.getAllArticulo();
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
        spinnerArticuloCompra.setAdapter(adapterArticulo);

        // seleccionar factura relacionada
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getIdCompra() == detalleCompra.getIdCompra()) {
                spinnerFacturaCompra.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < articulos.size(); i++) {
            if (articulos.get(i).getIdArticulo() == detalleCompra.getIdArticulo()) {
                spinnerArticuloCompra.setSelection(i);
                break;
            }
        }

        builder.show();
    }

    private void editDetalleCompra(DetalleCompra detalleCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalle_compra, null);
        builder.setView(dialogView);

        EditText editTextIdDetalleCompra = dialogView.findViewById(R.id.editTextIdDetalleCompra);
        EditText editTextFechaDetalleCompra = dialogView.findViewById(R.id.editTextFechaDetalleCompra);
        EditText editTextUnitarioDetalleCompra = dialogView.findViewById(R.id.editTextUnitarioDetalleCompra);
        EditText editTextCantidadDetalleCompra = dialogView.findViewById(R.id.editTextCantidadDetalleCompra);
        EditText editTextTotalDetalleCompra = dialogView.findViewById(R.id.editTextTotalDetalleCompra);
        Spinner spinnerArticuloCompra = dialogView.findViewById(R.id.spinnerArticuloCompra);
        Spinner spinnerFacturaCompra = dialogView.findViewById(R.id.spinnerFacturaCompra);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal(editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra, editTextTotalDetalleCompra);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextUnitarioDetalleCompra.addTextChangedListener(watcher);
        editTextCantidadDetalleCompra.addTextChangedListener(watcher);

        editTextIdDetalleCompra.setEnabled(false);

        editTextTotalDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextTotalDetalleCompra.setFocusableInTouchMode(false);
        editTextTotalDetalleCompra.setFocusable(false);
        editTextTotalDetalleCompra.setClickable(false);

        editTextFechaDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextFechaDetalleCompra.setFocusableInTouchMode(false);
        editTextFechaDetalleCompra.setFocusable(false);
        editTextFechaDetalleCompra.setClickable(false);

        spinnerArticuloCompra.setEnabled(false);
        spinnerFacturaCompra.setEnabled(false);

        editTextCantidadDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextCantidadDetalleCompra.setFocusableInTouchMode(false);
        editTextCantidadDetalleCompra.setFocusable(false);
        editTextCantidadDetalleCompra.setClickable(false);



        // obtener lista de facturas compra
        List<FacturaCompra> facturas = detalleCompraDAO.getAllFacturaCompra();
        ArrayAdapter<FacturaCompra> adapterFactura = new ArrayAdapter<FacturaCompra>(this, android.R.layout.simple_spinner_item, facturas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                } else {
                    view.setText(getString(R.string.invoice_id) + ": " + factura.getIdCompra() + ", "  + getString(R.string.provider_id) + ": " + factura.getIdProveedor()); // Esto se muestra cuando está cerrado
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FacturaCompra factura = getItem(position);
                if (factura.getIdCompra() == -1) {
                    view.setText(getString(R.string.select_factura));
                    
                } else {
                    view.setText("ID : " + factura.getIdCompra() + ", "  + getString(R.string.provider) + ": " + factura.getIdProveedor() + ", " + getString(R.string.purchase_date) + ": " + factura.getFechaCompra());
                    
                }
                return view;
            }
        };

        adapterFactura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFacturaCompra.setAdapter(adapterFactura);

        spinnerFacturaCompra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                FacturaCompra facturaSeleccionada = (FacturaCompra) parentView.getSelectedItem();
                if (facturaSeleccionada.getIdCompra() != -1 && facturaSeleccionada.getFechaCompra() != null) {
                    editTextFechaDetalleCompra.setText(facturaSeleccionada.getFechaCompra());
                } else {
                    editTextFechaDetalleCompra.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                editTextFechaDetalleCompra.setText("");
            }
        });

        List<Articulo> articulos = detalleCompraDAO.getAllArticulo();
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
        spinnerArticuloCompra.setAdapter(adapterArticulo);

        editTextIdDetalleCompra.setText(String.valueOf(detalleCompra.getIdDetalleCompra()));
        editTextFechaDetalleCompra.setText(detalleCompra.getFechaDeCompra());
        editTextUnitarioDetalleCompra.setText(String.valueOf(detalleCompra.getPrecioUnitarioCompra()));
        editTextTotalDetalleCompra.setText(String.valueOf(detalleCompra.getTotalDetalleCompra()));
        editTextCantidadDetalleCompra.setText(String.valueOf(detalleCompra.getCantidadCompra()));


        // seleccionar factura relacionada
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getIdCompra() == detalleCompra.getIdCompra()) {
                spinnerFacturaCompra.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < articulos.size(); i++) {
            if (articulos.get(i).getIdArticulo() == detalleCompra.getIdArticulo()) {
                spinnerArticuloCompra.setSelection(i);
                break;
            }
        }

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDetalleCompra);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDetalleCompra);
        btnLimpiar.setVisibility(View.GONE);

        editTextIdDetalleCompra.setInputType(InputType.TYPE_NULL);
        editTextIdDetalleCompra.setFocusable(false);

        List<View> vistas = Arrays.asList(
                editTextIdDetalleCompra, editTextFechaDetalleCompra, editTextUnitarioDetalleCompra, editTextCantidadDetalleCompra,
                editTextTotalDetalleCompra
        );

        List<String> listaRegex = Arrays.asList(
                "\\d+", "\\d{4}-\\d{2}-\\d{2}", "\\d+(\\.\\d{1,2})?", "\\d+", "\\d+(\\.\\d{1,2})?"
        );

        List<Integer> mensajesDeError = Arrays.asList(
                R.string.only_numbers, R.string.invalid_date, R.string.only_numbers, R.string.only_numbers,
                R.string.only_numbers
        );

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);
        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            if (validadorDeCampos.validarCampos()) {
                int idDetalleCompra = Integer.parseInt(editTextIdDetalleCompra.getText().toString());
                String fecha = editTextFechaDetalleCompra.getText().toString().trim();
                double precioUnitario = Double.parseDouble(editTextUnitarioDetalleCompra.getText().toString());
                int cantidadArticulos = Integer.parseInt(editTextCantidadDetalleCompra.getText().toString());
                double totalDetalle = Double.parseDouble(editTextTotalDetalleCompra.getText().toString());

                // obtener todas la factura seleccionada y validar que esa factura guarda el id y no un string
                FacturaCompra facturaSeleccionada = (FacturaCompra) spinnerFacturaCompra.getSelectedItem();
                Articulo articuloSeleccionado = (Articulo) spinnerArticuloCompra.getSelectedItem();

                detalleCompra.setIdCompra(idDetalleCompra);
                detalleCompra.setFechaDeCompra(fecha);
                detalleCompra.setPrecioUnitarioCompra(precioUnitario);
                detalleCompra.setCantidadCompra(cantidadArticulos);
                detalleCompra.setTotalDetalleCompra(totalDetalle);
                detalleCompra.setIdArticulo(articuloSeleccionado.getIdArticulo());
                detalleCompra.setIdCompra(facturaSeleccionada.getIdCompra());

                detalleCompraDAO.updateDetalleCompra(detalleCompra);
                fillList();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void deleteDetalleCompra(int idDetalleCompra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idDetalleCompra);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            detalleCompraDAO.deleteDetalleCompra(idDetalleCompra);
            Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
            fillList();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void calcularTotal(EditText precioEditText, EditText cantidadEditText, EditText totalEditText) {
        String precioStr = precioEditText.getText().toString();
        String cantidadStr = cantidadEditText.getText().toString();

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

    private void clearFieldsDetalleCompra(EditText editTextIdDetalleCompra, EditText editTextFechaDetalleCompra, EditText editTextUnitarioDetalleCompra, EditText editTextCantidadDetalleCompra,
                                          EditText editTextTotalDetalleCompra, Spinner spinnerArticuloCompra, Spinner spinnerFacturaCompra) {
        editTextIdDetalleCompra.setText("");
        editTextFechaDetalleCompra.setText("");
        editTextUnitarioDetalleCompra.setText("");
        editTextCantidadDetalleCompra.setText("");
        editTextTotalDetalleCompra.setText("");
        spinnerArticuloCompra.setSelection(0);
        spinnerFacturaCompra.setSelection(0);
    }

    private void buscarDetalleCompraPorId(int id) {
        DetalleCompra detalleCompra = detalleCompraDAO.getDetalleCompra(id);
        if(detalleCompra != null) {
            viewDetalleCompra(detalleCompra);
        }
        else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }


}
