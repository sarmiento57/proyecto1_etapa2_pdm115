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
    private ListView lista;

    private DireccionDAO direccionDAO;

    public ArrayAdapter<SucursalFarmacia> adapter;

    private List<SucursalFarmacia> sucursales;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sucursal_farmacia);

        // Initialize DAO with SQLite connection
        SQLiteDatabase conexionDB = new ControlBD(this).getConnection();
        sucursalFarmaciaDAO = new SucursalFarmaciaDAO(this, conexionDB);
        direccionDAO = new DireccionDAO(conexionDB, this);

        // Comprobacion Inicial de Permisos de Consulta
        lista = findViewById(R.id.lvBranch);
        cargarLista();

        Button boton = findViewById(R.id.btnAddBranch);


        TextView txtBusqueda = (TextView) findViewById(R.id.txtBusquedaSucursal);
        Button botonBuscar = findViewById(R.id.btnSearchBranch);
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



        boton.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);

        lista.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);





        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)) {
                    SucursalFarmacia sucursalFarmacia = (SucursalFarmacia) parent.getItemAtPosition(position);
                    showOptionsDialog(sucursalFarmacia);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                }



            }
        });

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.add);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
                builder.setView(dialogView);

                final EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
                Spinner spinneridDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
                final EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);
                final AlertDialog dialog = builder.create();
                dialog.show();


                List<Direccion> direccion = direccionDAO.getAllDireccion();
                List<SucursalFarmacia> sucursales = sucursalFarmaciaDAO.getAllSucursalFarmacia();
                List<Direccion> direccionNoAsignadas = new ArrayList<>(direccion);

                for (int i = 0; i < direccion.size(); i++) {
                    for (int j = 0; j < sucursales.size(); j++) {
                        if (direccion.get(i).getIdDireccion() == sucursales.get(j).getIdDireccion()) {
                            direccionNoAsignadas.remove(direccion.get(i)); // Eliminar la dirección ya asignada
                            break; // Salimos del segundo bucle para evitar eliminarla varias veces
                        }
                    }
                }

                Direccion seleccion = new Direccion(-1,-1 , getString(R.string.select_addres), SucursalFarmaciaActivity.this);
                direccionNoAsignadas.add(0, seleccion);
                ArrayAdapter<Direccion> adapterDireccion = new ArrayAdapter<>(SucursalFarmaciaActivity.this, android.R.layout.simple_spinner_item, direccionNoAsignadas);
                adapterDireccion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinneridDireccion.setAdapter(adapterDireccion);


                Button botonGuardar = dialog.findViewById(R.id.buttonSave);
                botonGuardar.setOnClickListener(view1 ->{
                    if(spinneridDireccion.getSelectedItemPosition() == 0)
                    {
                        Toast.makeText(SucursalFarmaciaActivity.this, getString(R.string.valid_addres), Toast.LENGTH_LONG).show();
                    }
                    else{

                        if(nombreFarma.getText().toString().trim().isEmpty() || idFarmacia.getText().toString().trim().isEmpty() ){

                            Toast.makeText(SucursalFarmaciaActivity.this, getString(R.string.completar_Campos), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else
                        {
                            try {
                                int idFarma = Integer.parseInt(idFarmacia.getText().toString());
                                Direccion idDireccion = (Direccion) spinneridDireccion.getSelectedItem();

                                String nombre = nombreFarma.getText().toString();

                                SucursalFarmacia sucursal = new SucursalFarmacia(idFarma, idDireccion.getIdDireccion(), nombre,SucursalFarmaciaActivity.this);
                                sucursalFarmaciaDAO.addSucursalFarmacia(sucursal); // Asegúrate que este método exista
                                dialog.dismiss();
                                Log.d("DEBUG", "Sucursal guardada: " + sucursal.toString());
                                cargarLista();
                            } catch (Exception e) {
                                Log.e("ERROR", "Error al guardar sucursal: " + e.getMessage());
                            }
                        }

                    }

                });


                Button botonClear = dialog.findViewById(R.id.buttonClear);
                botonClear.setOnClickListener(view1 ->{
                    idFarmacia.setText("");
                    spinneridDireccion.setSelection(0);
                    nombreFarma.setText("");

                });

            }
        });



    }


    private void cargarLista() {
        sucursales = sucursalFarmaciaDAO.getAllSucursalFarmacia();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sucursales);
        lista.setAdapter(adapter);
    }

    private void showOptionsDialog(final SucursalFarmacia sucursalFarmacia) {
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

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle view action
                viewDireccion(sucursalFarmacia);
                dialog.dismiss();
                // Implement view logic here
            }
        });

        dialogView.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Handle edit action
                dialog.dismiss();
                editarFarmacia(sucursalFarmacia);


                // Implement edit logic here
            }
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle delete action
                dialog.dismiss();
                deleteSucursalFarmacia(sucursalFarmacia.getIdFarmacia());
            }
        });

        dialog.show();
    }

    private void deleteSucursalFarmacia(int id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(getString(R.string.confirm_delete_message) + ": " + id);

        builder.setPositiveButton(R.string.yes, (dialog, which) -> {

            int rowsAffected = sucursalFarmaciaDAO.eliminarSucursal(id);
            cargarLista();

            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_message, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editarFarmacia(SucursalFarmacia sucursalFarmacia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
        builder.setView(dialogView);

        final EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
        Spinner spinneridDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
        final EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);
        final AlertDialog dialog = builder.create();
        dialog.show();

        // 1) Rellenar los campos
        idFarmacia.setText(String.valueOf(sucursalFarmacia.getIdFarmacia()));
        nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());
        idFarmacia.setEnabled(false);

        // 2) Obtener datos
        List<Direccion> todas = direccionDAO.getAllDireccion();
        List<SucursalFarmacia> todasSucursales = sucursalFarmaciaDAO.getAllSucursalFarmacia();
        Direccion actual = direccionDAO.getDireccion(sucursalFarmacia.getIdDireccion());

        // 3) Filtrar direcciones no asignadas a otras sucursales (salvo la actual)
        List<Direccion> libres = new ArrayList<>();
        for (Direccion d : todas) {
            if (d.getIdDireccion() == actual.getIdDireccion()) {
                continue;  // saltamos la actual
            }
            boolean usada = false;
            for (SucursalFarmacia sf : todasSucursales) {
                if (sf.getIdDireccion() == d.getIdDireccion()) {
                    usada = true;
                    break;
                }
            }
            if (!usada) {
                libres.add(d);
            }
        }

        // 4) Construir lista definitiva para el Spinner
        Direccion placeholder = new Direccion(-1, -1, getString(R.string.select_addres), SucursalFarmaciaActivity.this);
        libres.add(0, placeholder);
        libres.add(actual);  // al final ponemos la actual

        ArrayAdapter<Direccion> adapter = new ArrayAdapter<>(
                SucursalFarmaciaActivity.this,
                android.R.layout.simple_spinner_item,
                libres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinneridDireccion.setAdapter(adapter);

        // 5) Seleccionar la posición de la dirección actual
        int posActual = adapter.getPosition(actual);
        spinneridDireccion.setSelection(posActual);

        // 6) Guardar cambios con método de actualización
        Button botonGuardar = dialog.findViewById(R.id.buttonSave);
        botonGuardar.setOnClickListener(v -> {

            if(spinneridDireccion.getSelectedItemPosition() == 0)
            {
                Toast.makeText(SucursalFarmaciaActivity.this , getString(R.string.valid_addres) , Toast.LENGTH_LONG).show();
            }
            else{
            try {
                int idFarma = Integer.parseInt(idFarmacia.getText().toString());
                Direccion seleccion = (Direccion) spinneridDireccion.getSelectedItem();
                String nombre = nombreFarma.getText().toString();

                SucursalFarmacia actualizado = new SucursalFarmacia(
                        idFarma,
                        seleccion.getIdDireccion(),
                        nombre,
                        this
                );
                sucursalFarmaciaDAO.updateSucursalFarmacia(actualizado);  // <<— update en lugar de add
                Toast.makeText(SucursalFarmaciaActivity.this, R.string.update_message, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                cargarLista();
            } catch (Exception e) {
                Log.e("ERROR", "Error al actualizar sucursal: " + e.getMessage());
            }
            }
        });

        // 7) Botón Limpiar vuelve a la posición original
        Button botonClear = dialog.findViewById(R.id.buttonClear);
        botonClear.setOnClickListener(v -> {
            spinneridDireccion.setSelection(posActual);
            nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());
        });
    }


    private  void viewDireccion(SucursalFarmacia sucursalFarmacia)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_branch, null);
        builder.setView(dialogView);

        final EditText idFarmacia = dialogView.findViewById(R.id.editTextIdFarmacia);
        Spinner spinnerDireccion = dialogView.findViewById(R.id.spinnerIdDireccion);
        final EditText nombreFarma = dialogView.findViewById(R.id.editTextNombreFarmacia);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Direccion actual = direccionDAO.getDireccion(sucursalFarmacia.getIdDireccion());
        idFarmacia.setText(String.valueOf(sucursalFarmacia.getIdFarmacia()));
        nombreFarma.setText(sucursalFarmacia.getNombreFarmacia());

        List<Direccion> libres = new ArrayList<>();
        libres.add(actual);


        ArrayAdapter<Direccion> adapter = new ArrayAdapter<>(
                SucursalFarmaciaActivity.this,
                android.R.layout.simple_spinner_item,
                libres
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDireccion.setAdapter(adapter);
        int posActual = adapter.getPosition(actual);
        spinnerDireccion.setSelection(posActual);



        idFarmacia.setEnabled(false);
        nombreFarma.setEnabled(false);

        dialog.findViewById(R.id.buttonSave).setVisibility(View.GONE);
        dialog.findViewById(R.id.buttonClear).setVisibility(View.GONE);

    }

    private void buscarPorId(int id) {
        SucursalFarmacia sucursalFarmacia = sucursalFarmaciaDAO.getSucursalFarmacia(id);
        if (sucursalFarmacia != null) {
            viewDireccion(sucursalFarmacia);
        } else {
            Toast.makeText(this, R.string.not_found_message, Toast.LENGTH_SHORT).show();
        }
    }
}