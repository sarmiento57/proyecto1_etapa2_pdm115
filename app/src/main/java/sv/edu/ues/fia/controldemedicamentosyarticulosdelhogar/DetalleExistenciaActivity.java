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

import java.util.Arrays;
import java.util.Calendar;
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

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        detalleExistenciaDAO = new DetalleExistenciaDAO(conexionDB, this);
        sucursalFarmaciaDAO = new SucursalFarmaciaDAO(this, conexionDB);

        Button btnAgregar = findViewById(R.id.btnAgregarExistence);
        Button btnBuscar = findViewById(R.id.btnBuscarExistence);



        btnAgregar.setOnClickListener(v -> {showAddDialog();});
        btnBuscar.setOnClickListener(v -> {showSearchDialog();});

        btnAgregar.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.GONE);

        listViewDetalleExistencia = findViewById(R.id.lvExistenceDetail);

        listViewDetalleExistencia.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.GONE);

        fillList();

        btnBuscar.setVisibility(vac.validarAcceso(2) ? View.VISIBLE : View.GONE);



        listViewDetalleExistencia.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4))
                {
                    DetalleExistencia detalleExistencia = (DetalleExistencia) parent.getItemAtPosition(position);
                    showOptionsDialog(detalleExistencia);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                }

            }
        });



    }

    private void fillList(){
        listaDetalleExistencia = detalleExistenciaDAO.getAllDetallesExistencia();
        adaptadorDetalleExistencia = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleExistencia);
        listViewDetalleExistencia.setAdapter(adaptadorDetalleExistencia);
    }

    public  void showAddDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_existencia, null);
        builder.setView(dialogView);


        EditText editTextIdExistenceDetail = dialogView.findViewById(R.id.editTextIdExistenceDetail);
        EditText editTextExpirationDate = dialogView.findViewById(R.id.editTextExpirationDate);
        EditText editTextExistenceAmount = dialogView.findViewById(R.id.editTextExistenceAmount);
        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerIdFarmacia);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerIdArticulo);

        List<SucursalFarmacia> sucursales = sucursalFarmaciaDAO.getAllSucursalFarmacia();
        List<Articulo> articulos =  detalleExistenciaDAO.getAllArticulos();

        SucursalFarmacia seleccionSucursalFarmacia= new SucursalFarmacia(-1, getString(R.string.select_sucursal));
        Articulo seleccionArticulo = new Articulo(
                -1,    // idArticulo
                getString(R.string.select_articulo), // nombreArticulo
                this
                );
        sucursales.add(0, seleccionSucursalFarmacia);
        articulos.add(0, seleccionArticulo);

        ArrayAdapter<SucursalFarmacia> adapterSucursales = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sucursales);
        adapterSucursales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdFarmacia.setAdapter(adapterSucursales);

        ArrayAdapter<Articulo> adapterArticulos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, articulos);
        adapterArticulos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdArticulo.setAdapter(adapterArticulos);

        /// cambiar que muestran los spinner sin depender del to string

        editTextExpirationDate.setInputType(InputType.TYPE_NULL);
        editTextExpirationDate.setFocusable(false);

        editTextExpirationDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // seleccionada la fecha y la mete en el EditText
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextExpirationDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

            List<View> vistas = Arrays.asList(editTextExpirationDate);
            List<String> listaRegex = Arrays.asList(
                    "\\d{4}-\\d{2}-\\d{2}"
            );

            List<Integer> mensajesDeError = Arrays.asList(R.string.invalid_date);

            ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);

        final AlertDialog dialog = builder.create();



        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        btnGuardar.setOnClickListener(v -> {


            saveExistence(spinnerIdArticulo,spinnerIdFarmacia,
                    editTextExistenceAmount,editTextExpirationDate,
                    editTextIdExistenceDetail, dialog);
        });
        btnLimpiar.setOnClickListener(v -> limpiarCampos(spinnerIdArticulo,spinnerIdFarmacia,
                editTextExistenceAmount,editTextExpirationDate,
                editTextIdExistenceDetail));

        dialog.show();




    }


    public void saveExistence(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
            EditText editTextExistenceAmount, EditText editTextExpirationDate,
            EditText editTextIdExistenceDetail,AlertDialog  dialog){


        if(spinnerIdArticulo.getSelectedItemPosition() == 0 ||
                spinnerIdFarmacia.getSelectedItemPosition() == 0 ||
                editTextExistenceAmount.getText().toString().trim().isEmpty() ||
                editTextExpirationDate.getText().toString().trim().isEmpty() ||
                editTextIdExistenceDetail.getText().toString().trim().isEmpty()
        )
        {

            Toast.makeText(this, getString(R.string.datos_validos), Toast.LENGTH_LONG).show();

        }
        else {

            SucursalFarmacia sucursal = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
            Articulo articulo = (Articulo) spinnerIdArticulo.getSelectedItem();
            int idDetalleExistencia =  Integer.parseInt(editTextIdExistenceDetail.getText().toString());
            int cantidadExistencia =  Integer.parseInt(editTextExistenceAmount.getText().toString());
            String fechaDeVencimiento = editTextExpirationDate.getText().toString().trim();

            DetalleExistencia detalleExistencia = new DetalleExistencia(
                    articulo.getIdArticulo(),
                    idDetalleExistencia,
                    sucursal.getIdFarmacia(),
                    cantidadExistencia,
                    fechaDeVencimiento,
                    this
            );

            try {
                detalleExistenciaDAO.addExistencia(detalleExistencia);
                fillList();
                dialog.dismiss();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al insertar existencia", e);
            }


        }

    }

    public void limpiarCampos(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
                              EditText editTextExistenceAmount, EditText editTextExpirationDate,
                              EditText editTextIdExistenceDetail){

        // Limpiar los EditText
        editTextExistenceAmount.setText("");
        editTextExpirationDate.setText("");
        editTextIdExistenceDetail.setText("");


        // Restablecer los Spinners a su estado inicial
        spinnerIdArticulo.setSelection(0);
        spinnerIdFarmacia.setSelection(0);

    }



    public  void showOptionsDialog(DetalleExistencia detalleExistencia)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        if(!vac.validarAcceso(2))
            dialogView.findViewById(R.id.buttonView).setVisibility(View.GONE);

        if(!vac.validarAcceso(3))
                dialogView.findViewById(R.id.buttonEdit).setVisibility(View.GONE);

        if(!vac.validarAcceso(4))
                    dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> viewExistence(detalleExistencia, dialog));
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> editExistence(detalleExistencia, dialog));
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteExistence(detalleExistencia.getIdDetalleExistencia(), dialog));

        dialog.show();

    }

    public void viewExistence(DetalleExistencia detalleExistencia,AlertDialog dialog) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_existencia, null);
        builder.setView(dialogView);


        EditText editTextIdExistenceDetail = dialogView.findViewById(R.id.editTextIdExistenceDetail);
        EditText editTextExpirationDate = dialogView.findViewById(R.id.editTextExpirationDate);
        EditText editTextExistenceAmount = dialogView.findViewById(R.id.editTextExistenceAmount);
        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerIdFarmacia);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerIdArticulo);


        editTextIdExistenceDetail.setText(String.valueOf(detalleExistencia.getIdDetalleExistencia()));
        editTextExpirationDate.setText(String.valueOf(detalleExistencia.getFechaDeVencimiento()));
        editTextExistenceAmount.setText(String.valueOf(detalleExistencia.getCantidadExistencia()));

        editTextIdExistenceDetail.setEnabled(false);
        editTextExpirationDate.setEnabled(false);
        editTextExistenceAmount.setEnabled(false);
        editTextIdExistenceDetail.setTextColor(Color.BLACK);
        editTextExpirationDate.setTextColor(Color.BLACK);
        editTextExistenceAmount.setTextColor(Color.BLACK);

        spinnerIdFarmacia.setEnabled(false);
        spinnerIdArticulo.setEnabled(false);

        SucursalFarmacia sucursalFarmacia = sucursalFarmaciaDAO.getSucursalFarmacia(detalleExistencia.getIdFarmacia());
        Articulo articulo = detalleExistenciaDAO.getArticulo(detalleExistencia.getIdArticulo());

        spinnerIdFarmacia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new SucursalFarmacia[] { sucursalFarmacia }));
        spinnerIdArticulo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Articulo[] { articulo }));



        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        btnGuardar.setVisibility(View.GONE);
        btnLimpiar.setVisibility(View.GONE);


        builder.show();

    }
    public void editExistence(DetalleExistencia detalleExistencia ,AlertDialog dialog) {
        dialog.dismiss();
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


        editTextIdExistenceDetail.setText(String.valueOf(detalleExistencia.getIdDetalleExistencia()));
        editTextExpirationDate.setText(String.valueOf(detalleExistencia.getFechaDeVencimiento()));
        editTextExistenceAmount.setText(String.valueOf(detalleExistencia.getCantidadExistencia()));

        editTextIdExistenceDetail.setEnabled(false);

        editTextIdExistenceDetail.setTextColor(Color.BLACK);


        spinnerIdFarmacia.setEnabled(false);
        spinnerIdArticulo.setEnabled(false);

        SucursalFarmacia sucursalFarmacia = sucursalFarmaciaDAO.getSucursalFarmacia(detalleExistencia.getIdFarmacia());
        Articulo articulo = detalleExistenciaDAO.getArticulo(detalleExistencia.getIdArticulo());

        spinnerIdFarmacia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new SucursalFarmacia[] { sucursalFarmacia }));
        spinnerIdArticulo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Articulo[] { articulo }));


        editTextExpirationDate.setInputType(InputType.TYPE_NULL);
        editTextExpirationDate.setFocusable(false);

        editTextExpirationDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // seleccionada la fecha y la mete en el EditText
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editTextExpirationDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        List<View> vistas = Arrays.asList(editTextExpirationDate);
        List<String> listaRegex = Arrays.asList(
                "\\d{4}-\\d{2}-\\d{2}"
        );

        List<Integer> mensajesDeError = Arrays.asList(R.string.invalid_date);

        ValidadorDeCampos validadorDeCampos = new ValidadorDeCampos(this, vistas, listaRegex, mensajesDeError);



        Button btnGuardar = dialogView.findViewById(R.id.buttonSaveExistence);
        Button btnLimpiar = dialogView.findViewById(R.id.buttonClearExistence);

        btnGuardar.setOnClickListener(v -> {


            updateExistence(spinnerIdArticulo,spinnerIdFarmacia,
                    editTextExistenceAmount,editTextExpirationDate,
                    editTextIdExistenceDetail, dialog2);
        });
        btnLimpiar.setOnClickListener(v -> dialog2.dismiss());  // Cerrar el segundo diálogo al cancelar



        dialog2.show();



    }


    public void updateExistence(Spinner spinnerIdArticulo, Spinner spinnerIdFarmacia,
                              EditText editTextExistenceAmount, EditText editTextExpirationDate,
                              EditText editTextIdExistenceDetail,AlertDialog  dialog){


        if(
                editTextExistenceAmount.getText().toString().trim().isEmpty() ||
                editTextExpirationDate.getText().toString().trim().isEmpty() ||
                editTextIdExistenceDetail.getText().toString().trim().isEmpty()
        )
        {

            Toast.makeText(this, getText(R.string.datos_validos) , Toast.LENGTH_LONG).show();

        }
        else {

            SucursalFarmacia sucursal = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
            Articulo articulo = (Articulo) spinnerIdArticulo.getSelectedItem();
            int idDetalleExistencia =  Integer.parseInt(editTextIdExistenceDetail.getText().toString());
            int cantidadExistencia =  Integer.parseInt(editTextExistenceAmount.getText().toString());
            String fechaDeVencimiento = editTextExpirationDate.getText().toString().trim();

            DetalleExistencia detalleExistencia = new DetalleExistencia(
                    articulo.getIdArticulo(),
                    idDetalleExistencia,
                    sucursal.getIdFarmacia(),
                    cantidadExistencia,
                    fechaDeVencimiento,
                    this
            );

            try {
                detalleExistenciaDAO.updateExistencia(detalleExistencia);
                fillList();
                dialog.dismiss();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al modificar la existencia", e);
            }


        }

    }
    public void deleteExistence(int id , AlertDialog dialog_init) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            detalleExistenciaDAO.deleteExistencia(id);
            fillList();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog_init.dismiss();
    }

    public  void showSearchDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.search);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_buscar, null);
        builder.setView(dialogView);

        Button botonMostrarFarmacia  = dialogView.findViewById(R.id.btnMostrarFarmacia);
        Button botonMostrarArticulo = dialogView.findViewById(R.id.btnMostrarArticulo);
        Button btnSearchDetailExistence = dialogView.findViewById(R.id.btnSearchDetailExistence);




        Spinner spinnerIdFarmacia = dialogView.findViewById(R.id.spinnerFarmaciaB);
        Spinner spinnerIdArticulo = dialogView.findViewById(R.id.spinnerArticuloB);

        spinnerIdFarmacia.setVisibility(View.GONE);
        spinnerIdArticulo.setVisibility(View.GONE);

        botonMostrarFarmacia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerIdFarmacia.setVisibility(View.VISIBLE);
                spinnerIdArticulo.setVisibility(View.GONE);
            }
        });

// Mostrar el spinner de artículo y ocultar el de farmacia
        botonMostrarArticulo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerIdArticulo.setVisibility(View.VISIBLE);
                spinnerIdFarmacia.setVisibility(View.GONE);
            }
        });


        List<SucursalFarmacia> sucursales = sucursalFarmaciaDAO.getAllSucursalFarmacia();
        List<Articulo> articulos =  detalleExistenciaDAO.getAllArticulos();

        SucursalFarmacia seleccionSucursalFarmacia= new SucursalFarmacia(-1, getString(R.string.select_sucursal));
        Articulo seleccionArticulo = new Articulo(
                -1,    // idArticulo
                getString(R.string.select_articulo), // nombreArticulo
                this
        );
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

        btnSearchDetailExistence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerIdFarmacia.getVisibility() == View.GONE) {
                    Articulo articuloSeleccionado = (Articulo) spinnerIdArticulo.getSelectedItem();
                    // Aquí haces lo que necesites con el artículo seleccionado
                    List<DetalleExistencia> detalles = detalleExistenciaDAO.getAllDetallesExistenciaByIdArticulo(articuloSeleccionado.getIdArticulo());
                    dialog.dismiss();
                    fillList(detalles);

                } else {
                    SucursalFarmacia farmaciaSeleccionada = (SucursalFarmacia) spinnerIdFarmacia.getSelectedItem();
                    List<DetalleExistencia> detalles = detalleExistenciaDAO.getAllDetallesExistenciaByIdFarm(farmaciaSeleccionada.getIdFarmacia());
                    dialog.dismiss();
                    fillList(detalles);

                    // Aquí haces lo que necesites con la farmacia seleccionada
                }
            }
        });






    }

private void fillList(List<DetalleExistencia>  detalles){
        listViewDetalleExistencia.setVisibility( View.VISIBLE);
        listaDetalleExistencia = detalles;
        adaptadorDetalleExistencia = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDetalleExistencia);
        listViewDetalleExistencia.setAdapter(adaptadorDetalleExistencia);
}


}