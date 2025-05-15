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

import java.util.Arrays;
import java.util.List;

public class DireccionActivity extends AppCompatActivity {

    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);
    private DireccionDAO direccionDAO;
    private ArrayAdapter<Direccion> adaptadorDireccion;
    private List<Direccion> listaDireccion;
    private ListView listViewDireccion;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direccion);

        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        direccionDAO = new DireccionDAO(conexionDB, this);
        Button btnAgregar = findViewById(R.id.btnAgregarDireccion);

        // validar permiso de agregar
        btnAgregar.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);

        btnAgregar.setOnClickListener(v -> {showAddDialog();});

        TextView txtBusqueda = (TextView) findViewById(R.id.txtBusquedaDireccion);
        Button botonBuscar = findViewById(R.id.btnBuscarDireccion);
        botonBuscar.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        botonBuscar.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(txtBusqueda.getText().toString().trim());
                buscarPorId(id);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.invalid_search, Toast.LENGTH_LONG).show();
            }
        });



        listViewDireccion = findViewById(R.id.lvDireccion);
        listViewDireccion.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);

        // Fill the ListView
        fillList();

        // Set item click listener for ListView
        listViewDireccion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)) {
                    Direccion direccion = (Direccion) parent.getItemAtPosition(position);
                    showOptionsDialog(direccion);
                }
                else
                    {
                        Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                    }

            }
        });





    }

    private void fillList(){
        listaDireccion = direccionDAO.getAllDireccion();
        adaptadorDireccion = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDireccion);
        listViewDireccion.setAdapter(adaptadorDireccion);
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


//            Spinner o combo box o listas plegables

        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);

        spinnerMunicipio.setEnabled(false);
        spinnerDistrito.setEnabled(false);



        // Agregar al Spinner las cosas
        List<Departamento> departamentos = direccionDAO.getAllDepartamentos();
        // Crear un departamento ficticio para la opción "Seleccione Departamento"
        Departamento seleccionDepartamento = new Departamento(-1, getString(R.string.select_departamento));
        // Agregar la opción al principio de la lista
        departamentos.add(0, seleccionDepartamento);

        ArrayAdapter<Departamento> adapterDepartamentos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departamentos);
        adapterDepartamentos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartamento.setAdapter(adapterDepartamentos);

        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Obtener el departamento seleccionado
                Departamento itemSeleccionado = (Departamento) parent.getItemAtPosition(position);
                DepartamentoSelected(spinnerMunicipio, itemSeleccionado);
                spinnerDistrito.setEnabled(false);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        spinnerMunicipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Municipio itemSeleccionado = (Municipio) parent.getItemAtPosition(position);
                municipioSelected(spinnerDistrito, itemSeleccionado);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
            final AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            saveDireccion( editTextIdDireccion,  editTextDireccionExacta,
                     spinnerDepartamento,  spinnerMunicipio, spinnerDistrito , dialog);
        });
        btnLimpiar.setOnClickListener(v -> limpiarCampos(editTextIdDireccion, editTextDireccionExacta, spinnerDepartamento, spinnerMunicipio, spinnerDistrito));

        dialog.show();
    }

    private  void DepartamentoSelected(Spinner spinnerMunicipio, Departamento itemSeleccionado)
    {



        // Habilitar el spinner de municipios
        spinnerMunicipio.setEnabled(true);

        // Obtener los municipios correspondientes al departamento seleccionado
        List<Municipio> municipios = direccionDAO.getAllMunicipios(itemSeleccionado.getIdDepartamento());
        Municipio seleccion = new Municipio(-1, -1,getString(R.string.select_municipio));
        municipios.add(0, seleccion);



        // Crear el adaptador para los municipios
        ArrayAdapter<Municipio> adapterMunicipios = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, municipios);
        adapterMunicipios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al spinner de municipios
        spinnerMunicipio.setAdapter(adapterMunicipios);





    }

    private  void municipioSelected(Spinner spinnerDistrito, Municipio itemSeleccionado)
    {
        // Obtener el departamento seleccionado

        // Habilitar el spinner de municipios
        spinnerDistrito.setEnabled(true);

        // Obtener los Distritos correspondientes al departamento seleccionado
        List<Distrito> distritos = direccionDAO.getAllDistritos(itemSeleccionado.getIdMunicipio());
        Distrito seleccion = new Distrito(-1, -1,getString(R.string.select_distrito));
        distritos.add(0, seleccion);



        // Crear el adaptador para los municipios
        ArrayAdapter<Distrito> adapterDistritos = new ArrayAdapter<>(DireccionActivity.this, android.R.layout.simple_spinner_item, distritos);
        adapterDistritos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al spinner de municipios
        spinnerDistrito.setAdapter(adapterDistritos);

    }

    private void saveDireccion(EditText editTextIdDireccion, EditText editTextDireccionExacta,
                               Spinner spinnerDepartamento, Spinner spinnerMunicipio, Spinner spinnerDistrito, AlertDialog dialog)
    {
        if(editTextIdDireccion.getText().toString().isEmpty() || editTextDireccionExacta.getText().toString().isEmpty()   ) {
            Toast.makeText(this, getString(R.string.datos_validos), Toast.LENGTH_LONG).show();
            return;

        }
            if (spinnerDepartamento.getSelectedItemPosition() == 0 ||
                    spinnerMunicipio.getSelectedItemPosition() == 0 ||
                    spinnerDistrito.getSelectedItemPosition() == 0)  {
                Toast.makeText(this, getString(R.string.select_valid_dep_mun_dis), Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                int id = Integer.parseInt(editTextIdDireccion.getText().toString());
                String direccionExacta = editTextDireccionExacta.getText().toString().trim();
                // obtener los proveedores o farmacias que estan seleccionados
                Distrito distrito = (Distrito) spinnerDistrito.getSelectedItem();

                Direccion direccion = new Direccion(id, distrito.getIdDistrito() ,  direccionExacta, this);
                direccionDAO.addDireccion(direccion);
                fillList();
                dialog.dismiss();

            }


        }

    private void showOptionsDialog(final Direccion direccion) {
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

        dialogView.findViewById(R.id.buttonView).setOnClickListener(v -> viewDireccion(direccion, dialog));
        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(v -> editDireccion(direccion, dialog));
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteDireccion(direccion.getIdDireccion(), dialog));

        dialog.show();
    }
    private void limpiarCampos(EditText editTextIdDireccion, EditText editTextDireccionExacta,
                               Spinner spinnerDepartamento, Spinner spinnerMunicipio, Spinner spinnerDistrito) {
        // Limpiar los EditText
        editTextIdDireccion.setText("");
        editTextDireccionExacta.setText("");

        // Restablecer los Spinners a su estado inicial
        spinnerDepartamento.setSelection(0); // "Seleccione un Departamento"
        spinnerMunicipio.setSelection(0);
        spinnerMunicipio.setEnabled(false);// "Seleccione un Municipio"
        spinnerDistrito.setSelection(0); // "Seleccione un Distrito"
        spinnerDistrito.setEnabled(false);
    }
    private void deleteDireccion(int idDireccion, AlertDialog dialog_init) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + idDireccion);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            direccionDAO.deleteDireccion(idDireccion);
            fillList();
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog_init.dismiss();
    }

    private void viewDireccion(Direccion direccion, AlertDialog dialog) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);


        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);


//            Spinner o combo box o listas plegables

        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);


        //set text

        editTextIdDireccion.setText(  String.valueOf((direccion.getIdDireccion())) );
        editTextDireccionExacta.setText(  String.valueOf((direccion.getDireccionExacta())) );
        Distrito dis =  direccionDAO.getDistrito(direccion.getIdDistrito());
        Municipio mun = direccionDAO.getMunicipio(dis.getIdMunicipio());
        Departamento dep = direccionDAO.getDepartamento(mun.getIdDepartamento());

        editTextIdDireccion.setEnabled(false);
        editTextIdDireccion.setTextColor(Color.BLACK);
        editTextDireccionExacta.setEnabled(false);
        editTextDireccionExacta.setTextColor(Color.BLACK);

        spinnerDepartamento.setEnabled(false);
        spinnerMunicipio.setEnabled(false);
        spinnerDistrito.setEnabled(false);

        spinnerDepartamento.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Departamento[] { dep }));
        spinnerMunicipio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Municipio[] { mun }));
        spinnerDistrito.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Distrito[] { dis }));
        btnGuardar.setVisibility(View.GONE);
        btnLimpiar.setVisibility(View.GONE);

        builder.show();
    }

    private void editDireccion(Direccion direccion, AlertDialog dialog) {

        // Asegúrate de cerrar el diálogo actual antes de abrir el siguiente
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);
        final AlertDialog dialog2 = builder.create();

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);
        btnLimpiar.setText(getString(R.string.cancel));

        // Spinners
        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);

        // Llenar campos con la información de la dirección
        editTextIdDireccion.setText(String.valueOf(direccion.getIdDireccion()));
        editTextDireccionExacta.setText(direccion.getDireccionExacta());
        Distrito dis = direccionDAO.getDistrito(direccion.getIdDistrito());
        Municipio mun = direccionDAO.getMunicipio(dis.getIdMunicipio());
        Departamento dep = direccionDAO.getDepartamento(mun.getIdDepartamento());

        editTextIdDireccion.setEnabled(false);
        editTextIdDireccion.setTextColor(Color.BLACK);
        editTextDireccionExacta.setEnabled(true);

        // Configurar Spinners
        List<Departamento> departamentos = direccionDAO.getAllDepartamentos();
        List<Municipio> municipios = direccionDAO.getAllMunicipios(mun.getIdDepartamento());
        List<Distrito> distritos = direccionDAO.getAllDistritos(mun.getIdMunicipio());

        Departamento seleccionDepartamento = new Departamento(-1, getString(R.string.select_departamento));
        Municipio seleccionMunicipio = new Municipio(-1, -1, getString(R.string.select_municipio));
        Distrito seleccionDistrito = new Distrito(-1, -1, getString(R.string.select_distrito));

        // Agregar la opción "Seleccione" al inicio de la lista
        departamentos.add(0, seleccionDepartamento);
        municipios.add(0, seleccionMunicipio);
        distritos.add(0, seleccionDistrito);

        ArrayAdapter<Departamento> adapterDepartamentos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departamentos);
        adapterDepartamentos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartamento.setAdapter(adapterDepartamentos);
        spinnerDepartamento.setSelection(getIndexById(departamentos, dep.getIdDepartamento()));

        ArrayAdapter<Municipio> adapterMunicipios = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, municipios);
        adapterMunicipios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMunicipio.setAdapter(adapterMunicipios);
        spinnerMunicipio.setSelection(getIndexById(municipios, mun.getIdMunicipio()));

        ArrayAdapter<Distrito> adapterDistritos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distritos);
        adapterDistritos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrito.setAdapter(adapterDistritos);
        spinnerDistrito.setSelection(getIndexById(distritos, dis.getIdDistrito()));

        // Logica para detectar interacción del usuario con los Spinners
        final boolean[] userTouchedDepartamento = {false};
        final boolean[] userTouchedMunicipio = {false};

        spinnerDepartamento.setOnTouchListener((v, event) -> {
            userTouchedDepartamento[0] = true;
            return false;
        });

        spinnerMunicipio.setOnTouchListener((v, event) -> {
            userTouchedMunicipio[0] = true;
            return false;
        });

        spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userTouchedDepartamento[0]) {
                    Departamento itemSeleccionado = (Departamento) parent.getItemAtPosition(position);
                    DepartamentoSelected(spinnerMunicipio, itemSeleccionado);
                    spinnerDistrito.setEnabled(false);
                    userTouchedDepartamento[0] = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMunicipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userTouchedMunicipio[0]) {
                    Municipio itemSeleccionado = (Municipio) parent.getItemAtPosition(position);
                    municipioSelected(spinnerDistrito, itemSeleccionado);
                    userTouchedMunicipio[0] = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Mostrar el diálogo
        dialog2.show();

        btnGuardar.setOnClickListener(v -> {
            int idDireccion = Integer.parseInt(editTextIdDireccion.getText().toString());
            String direccionExacta = editTextDireccionExacta.getText().toString().trim();

            Departamento departamento = (Departamento) spinnerDepartamento.getSelectedItem();
            Municipio municipio = (Municipio) spinnerMunicipio.getSelectedItem();
            Distrito distrito = (Distrito) spinnerDistrito.getSelectedItem();

            if (departamento.getIdDepartamento() == -1 || municipio.getIdMunicipio() == -1 || distrito.getIdDistrito() == -1 || direccionExacta.isEmpty()) {
                Toast.makeText(this, getString(R.string.completar_Campos), Toast.LENGTH_SHORT).show();
                return;
            }

            Direccion nuevaDireccion = new Direccion(idDireccion, distrito.getIdDistrito(), direccionExacta, this);

            direccionDAO.updateDireccion(nuevaDireccion);
            fillList();
            dialog2.dismiss();  // Cerrar el segundo diálogo después de guardar
        });

        btnLimpiar.setOnClickListener(v -> dialog2.dismiss());  // Cerrar el segundo diálogo al cancelar
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
        return 0; // por defecto el primero (como "Seleccione...")
    }

    private void buscarPorId(int id) {
        Direccion direccion = direccionDAO.getDireccion(id);
        if (direccion != null) {
            viewDireccion(direccion);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void viewDireccion(Direccion direccion) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_direccion, null);
        builder.setView(dialogView);

        EditText editTextIdDireccion = dialogView.findViewById(R.id.editTextIdDireccion);
        EditText editTextDireccionExacta = dialogView.findViewById(R.id.editTextDireccionExacta);


        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarDireccion);
        Button btnLimpiar = dialogView.findViewById(R.id.btnLimpiarDireccion);


//            Spinner o combo box o listas plegables

        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerMunicipio = dialogView.findViewById(R.id.spinnerMunicipio);
        Spinner spinnerDistrito = dialogView.findViewById(R.id.spinnerDistrito);


        //set text

        editTextIdDireccion.setText(  String.valueOf((direccion.getIdDireccion())) );
        editTextDireccionExacta.setText(  String.valueOf((direccion.getDireccionExacta())) );
        Distrito dis =  direccionDAO.getDistrito(direccion.getIdDistrito());
        Municipio mun = direccionDAO.getMunicipio(dis.getIdMunicipio());
        Departamento dep = direccionDAO.getDepartamento(mun.getIdDepartamento());

        editTextIdDireccion.setEnabled(false);
        editTextIdDireccion.setTextColor(Color.BLACK);
        editTextDireccionExacta.setEnabled(false);
        editTextDireccionExacta.setTextColor(Color.BLACK);

        spinnerDepartamento.setEnabled(false);
        spinnerMunicipio.setEnabled(false);
        spinnerDistrito.setEnabled(false);

        spinnerDepartamento.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Departamento[] { dep }));
        spinnerMunicipio.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Municipio[] { mun }));
        spinnerDistrito.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Distrito[] { dis }));
        btnGuardar.setVisibility(View.GONE);
        btnLimpiar.setVisibility(View.GONE);

        builder.show();
    }






}
