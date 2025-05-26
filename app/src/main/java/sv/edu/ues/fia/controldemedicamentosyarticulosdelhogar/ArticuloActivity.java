package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class ArticuloActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private ArticuloDAO articuloDAO;
    private CategoriaDAO categoriaDAO;
    private ArrayAdapter<Articulo> adaptadorListV;
    private ArrayAdapter<Categoria> adaptadorSpinner;

    private Categoria selected = new Categoria();
    private List<Articulo> valuesArt = new ArrayList<>();

    private List<Categoria> valuesCat = new ArrayList<>();

    private Articulo busqueda = new Articulo();
    private final ValidarAccesoCRUD vac = new ValidarAccesoCRUD(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulo);

        //Database conection
        SQLiteDatabase conection = new ControlBD(this).getConnection();
        articuloDAO = new ArticuloDAO(this, conection);
        categoriaDAO = new CategoriaDAO(conection, this);

        //Spinner
        adaptadorSpinner = new ArrayAdapter<Categoria>(this, android.R.layout.simple_spinner_item, valuesCat) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Categoria categoria = getItem(position);
                if (categoria.getIdCategoria() == -1) {
                    view.setText(getString(R.string.category_prompt));
                } else {
                    view.setText(categoria.getNombreCategoria()); // Esto se muestra cuando está cerrado
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Categoria categoria = getItem(position);
                if (categoria.getIdCategoria() == -1) {
                    view.setText(getString(R.string.category_prompt));
                    
                } else {
                    view.setText("ID : " + categoria.getIdCategoria() + ", " + getString(R.string.category_name) + ": " + categoria.getNombreCategoria());
                }
                return view;
            }
        };
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.itemCategorySpinner);
        spinner.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        spinner.setAdapter(adaptadorSpinner);
        llenadoSpinner();
        spinner.setOnItemSelectedListener(this);

        //textView
        TextView categoriaFiltro = (TextView) findViewById(R.id.itemCategoryText);

        //ListV
        adaptadorListV = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valuesArt);
        ListView listV = (ListView) findViewById(R.id.itemListv);
        listV.setAdapter(adaptadorListV);
        listV.setOnItemClickListener(this);
        listV.setVisibility(vac.validarAcceso(3) || vac.validarAcceso(4) ? View.VISIBLE : View.INVISIBLE);

        //EditText
        EditText buscar = (EditText) findViewById(R.id.editTextSearchItem);
        buscar.setVisibility(GONE);

        //Botones
        Button btnAdd = (Button) findViewById(R.id.btnAddItem);
        btnAdd.setVisibility(vac.validarAcceso(1) ? View.VISIBLE : View.INVISIBLE);
        Button btnSearch = (Button) findViewById(R.id.btnSearchItem);
        btnSearch.setVisibility(vac.validarAcceso(2) || vac.validarAcceso(3) || vac.validarAcceso(4)? View.VISIBLE : View.INVISIBLE);
        btnAdd.setOnClickListener(v -> {
            showAddDialog();
        });

        btnSearch.setOnClickListener(v -> {
            listV.setVisibility(VISIBLE);
            if (buscar.getVisibility() == GONE) {
                Log.d("inicial", "se activo");
                categoriaFiltro.setVisibility(GONE);
                spinner.setVisibility(GONE);
                buscar.setVisibility(VISIBLE);
                buscar.requestFocus();
            }else if (buscar.getVisibility() == VISIBLE) {
                if (buscar.getText().length() > 0) {
                    Log.d("primero", "mas de 0");
                    int id = Integer.parseInt(String.valueOf(buscar.getText()));
                    buscarArticulo(id);
                }else if(buscar.getText().length() == 0){
                    Log.d("segundo", "cabal 0 ");
                    buscar.setVisibility(GONE);
                    actualizarListView(selected);
                    categoriaFiltro.setVisibility(VISIBLE);
                    spinner.setVisibility(VISIBLE);
                }
            }
        });

        listV.setOnItemClickListener((parent, view, position, id) -> {
            Articulo articulo = (Articulo) parent.getItemAtPosition(position);
            showOptionsDialog(articulo);
        });

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Articulo articulo = (Articulo) parent.getItemAtPosition(position);
        Log.d("Seleccionado", articulo.getNombreArticulo());
        showOptionsDialog(articulo);
    }

    public void actualizarListView(Categoria filtro) {
        if (!valuesArt.isEmpty()) {
            valuesArt.clear();
        }
        if (filtro != null) {
            int idFiltro = filtro.getIdCategoria();
            if (idFiltro == -1) {
                valuesArt.addAll(articuloDAO.getAllRows());
                adaptadorListV.notifyDataSetChanged();
            } else {
                valuesArt.addAll(articuloDAO.getRowsFiltredByCategory(idFiltro));
                adaptadorListV.notifyDataSetChanged();
            }
        } else if (filtro == null) {
            valuesArt.clear();
            if(busqueda!=null){
            valuesArt.add(busqueda);
            }
            adaptadorListV.notifyDataSetChanged();
        }
    }

    public void llenadoSpinner() {
        if (!valuesCat.isEmpty()) {
            valuesCat.clear();
        }
        Categoria opcionTodas = new Categoria(-1, "All");
        valuesCat.add(0, opcionTodas);
        valuesCat.addAll(categoriaDAO.getAllRows());
        adaptadorSpinner.notifyDataSetChanged();
    }

    public void showAddDialog() {
        int numRegistros = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_articulo, null);
        builder.setView(dialogView);
        EditText idArticulo = dialogView.findViewById(R.id.editTextItemId);
        EditText name = dialogView.findViewById(R.id.editTextItemName);
        EditText description = dialogView.findViewById(R.id.editTextItemDescription);
        CheckBox isRestricted = dialogView.findViewById(R.id.checkBoxItemRestricted);
        EditText price = dialogView.findViewById(R.id.editTextItemPrice);


        Spinner spinnerItemMarca = dialogView.findViewById(R.id.spinnerItemMarca);
        Spinner spinnerItemROA = dialogView.findViewById(R.id.spinnerItemROA);
        Spinner spinnerItemSubCategoria = dialogView.findViewById(R.id.spinnerItemSubCategoria);
        Spinner spinnerItemFormaFarmaceutica = dialogView.findViewById(R.id.spinnerItemFormaFarmaceutica);

        List<Marca> marcas = articuloDAO.getAllMarca();
        marcas.add(0, new Marca(-1, getString(R.string.select_brand), this));

        ArrayAdapter<Marca> adapterMarca = new ArrayAdapter<Marca>(this, android.R.layout.simple_spinner_item, marcas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                } else {
                    view.setText(marca.getNombreMarca());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                    
                } else {
                    view.setText(marca.getNombreMarca() + " (ID: " + marca.getIdMarca() + ")");
                    
                }
                return view;
            }
        };
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemMarca.setAdapter(adapterMarca);

        // VIA AMNISTRACION
        List<ViaAdministracion> vias = articuloDAO.getAllViaAdministracion();
        vias.add(0, new ViaAdministracion(-1, getString(R.string.select_admin_route), this));

        ArrayAdapter<ViaAdministracion> adapterVia = new ArrayAdapter<ViaAdministracion>(this, android.R.layout.simple_spinner_item, vias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                ViaAdministracion via = getItem(position);
                if (via.getIdViaAdministracion() == -1) {
                    view.setText(getString(R.string.select_admin_route));
                } else {
                    view.setText(via.getTipoAdministracion());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                ViaAdministracion via = getItem(position);
                if (via.getIdViaAdministracion() == -1) {
                    view.setText(getString(R.string.select_admin_route));
                    
                } else {
                    view.setText(via.getTipoAdministracion() + " (ID: " + via.getIdViaAdministracion() + ")");
                    
                }
                return view;
            }
        };
        adapterVia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemROA.setAdapter(adapterVia);

        //SUVCATEGORIA
        List<SubCategoria> subcategorias = articuloDAO.getAllSubCategoria();
        subcategorias.add(0, new SubCategoria(-1, getString(R.string.select_subcategory), this));

        ArrayAdapter<SubCategoria> adapterSubCat = new ArrayAdapter<SubCategoria>(this, android.R.layout.simple_spinner_item, subcategorias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                } else {
                    view.setText(sub.getNombreSubCategoria());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                    
                } else {
                    view.setText(sub.getNombreSubCategoria() + " (ID: " + sub.getIdSubCategoria() + ")");
                    
                }
                return view;
            }
        };
        adapterSubCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemSubCategoria.setAdapter(adapterSubCat);

        //FOrMA Farmaceutica
        List<FormaFarmaceutica> formas = articuloDAO.getAllFormaFarmaceutica();
        formas.add(0, new FormaFarmaceutica(-1, getString(R.string.select_pharma_form), this));

        ArrayAdapter<FormaFarmaceutica> adapterForma = new ArrayAdapter<FormaFarmaceutica>(this, android.R.layout.simple_spinner_item, formas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FormaFarmaceutica forma = getItem(position);
                if (forma.getIdFormaFarmaceutica() == -1) {
                    view.setText(getString(R.string.select_pharma_form));
                } else {
                    view.setText(forma.getTipoFormaFarmaceutica());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FormaFarmaceutica forma = getItem(position);
                if (forma.getIdFormaFarmaceutica() == -1) {
                    view.setText(getString(R.string.select_pharma_form));
                    
                } else {
                    view.setText(forma.getTipoFormaFarmaceutica() + " (ID: " + forma.getIdFormaFarmaceutica() + ")");
                    
                }
                return view;
            }
        };
        adapterForma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemFormaFarmaceutica.setAdapter(adapterForma);

        EditText[] campos = {idArticulo, name, description, price};

        Button btnSaveArticulo = dialogView.findViewById(R.id.btnGuardarArticulo);
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarArticulo);

        AlertDialog dialog = builder.create();

        Cursor cursor = articuloDAO.getDbConection().rawQuery("SELECT COUNT(*) FROM ARTICULO", null);
        if (cursor.moveToFirst()) {
            numRegistros = cursor.getInt(0);
        }

        idArticulo.setText(Integer.toString(numRegistros + 1));

        btnClear.setOnClickListener(v -> {
            spinnerItemMarca.setSelection(0);
            spinnerItemROA.setSelection(0);
            spinnerItemSubCategoria.setSelection(0);
            spinnerItemFormaFarmaceutica.setSelection(0);
            idArticulo.setText("");
            name.setText("");
            description.setText("");
            isRestricted.setChecked(false);
            price.setText("");
        });

        btnSaveArticulo.setOnClickListener(v -> {
            boolean hayError = false;

            String nombre = name.getText().toString().trim();
            String descripcion = description.getText().toString().trim();
            String precioTexto = price.getText().toString().trim();

            if (nombre.isEmpty()) {
                name.setError(getString(R.string.field_empty));
                hayError = true;
            }

            if (descripcion.isEmpty()) {
                description.setError(getString(R.string.field_empty));
                hayError = true;
            }

            if (precioTexto.isEmpty()) {
                price.setError(getString(R.string.field_empty));
                hayError = true;
            }

            Marca marcaSeleccionada = (Marca) spinnerItemMarca.getSelectedItem();
            SubCategoria subSeleccionada = (SubCategoria) spinnerItemSubCategoria.getSelectedItem();
            ViaAdministracion viaSeleccionada = (ViaAdministracion) spinnerItemROA.getSelectedItem();
            FormaFarmaceutica formaSeleccionada = (FormaFarmaceutica) spinnerItemFormaFarmaceutica.getSelectedItem();

            if (marcaSeleccionada.getIdMarca() == -1) {
                View view = spinnerItemMarca.getSelectedView();
                if (view instanceof TextView) {
                    ((TextView) view).setError("");
                    ((TextView) view).setTextColor(Color.RED);
                }
                hayError = true;
            }

            if (subSeleccionada.getIdSubCategoria() == -1) {
                View view = spinnerItemSubCategoria.getSelectedView();
                if (view instanceof TextView) {
                    ((TextView) view).setError("");
                    ((TextView) view).setTextColor(Color.RED);
                }
                hayError = true;
            }

            if (hayError) {
                Toast.makeText(this, getString(R.string.field_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int id = Integer.parseInt(idArticulo.getText().toString());
                int brand = marcaSeleccionada.getIdMarca();
                int subCat = subSeleccionada.getIdSubCategoria();
                Integer ROA = (viaSeleccionada.getIdViaAdministracion() == -1) ? null : viaSeleccionada.getIdViaAdministracion();
                Integer PF = (formaSeleccionada.getIdFormaFarmaceutica() == -1) ? null : formaSeleccionada.getIdFormaFarmaceutica();
                boolean restringido = isRestricted.isChecked();
                double precio = Double.parseDouble(precioTexto);

                Articulo art = new Articulo(id, brand, ROA, subCat, PF, nombre, descripcion, restringido, precio, this);
                boolean exito = articuloDAO.insertarArticulo(art);
                if (exito) {
                    dialog.dismiss();
                    actualizarListView(selected);
                } else {
                    Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.only_numbers), Toast.LENGTH_SHORT).show();
            }
        });



        dialog.show();
    }


    public void showOptionsDialog(Articulo articulo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.buttonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vac.validarAcceso(2))
                    verArticulo(articulo);
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
                    editArticulo(articulo, dialog);
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
                    eliminarArticulo(articulo, dialog);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.action_block, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void verArticulo(Articulo articulo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_articulo, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();


        Spinner spinnerItemMarca = dialogView.findViewById(R.id.spinnerItemMarca);
        Spinner spinnerItemROA = dialogView.findViewById(R.id.spinnerItemROA);
        Spinner spinnerItemSubCategoria = dialogView.findViewById(R.id.spinnerItemSubCategoria);
        Spinner spinnerItemFormaFarmaceutica = dialogView.findViewById(R.id.spinnerItemFormaFarmaceutica);

        EditText idArticulo = dialogView.findViewById(R.id.editTextItemId);
        EditText name = dialogView.findViewById(R.id.editTextItemName);
        EditText description = dialogView.findViewById(R.id.editTextItemDescription);
        CheckBox isRestricted = dialogView.findViewById(R.id.checkBoxItemRestricted);
        EditText price = dialogView.findViewById(R.id.editTextItemPrice);

        Button btnSaveArticulo = dialogView.findViewById(R.id.btnGuardarArticulo);
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarArticulo);

        List<Marca> marcas = articuloDAO.getAllMarca();
        marcas.add(0, new Marca(-1, getString(R.string.select_brand), this));

        ArrayAdapter<Marca> adapterMarca = new ArrayAdapter<Marca>(this, android.R.layout.simple_spinner_item, marcas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                } else {
                    view.setText(marca.getNombreMarca());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                    
                } else {
                    view.setText(marca.getNombreMarca() + " (ID: " + marca.getIdMarca() + ")");
                    
                }
                return view;
            }
        };
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemMarca.setAdapter(adapterMarca);

        // VIA AMNISTRACION
        List<ViaAdministracion> vias = articuloDAO.getAllViaAdministracion();
        vias.add(0, new ViaAdministracion(-1, getString(R.string.select_admin_route), this));

        boolean viaExiste = false;
        if (articulo.getIdViaAdministracion() != null) {
            for (ViaAdministracion v : vias) {
                if (v.getIdViaAdministracion() == articulo.getIdViaAdministracion()) {
                    viaExiste = true;
                    break;
                }
            }
            if (!viaExiste) {
                vias.add(0, new ViaAdministracion(articulo.getIdViaAdministracion(), "NULL", this));
            }
        }

        ArrayAdapter<ViaAdministracion> adapterVia = new ArrayAdapter<ViaAdministracion>(this, android.R.layout.simple_spinner_item, vias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(vias.get(position).getTipoAdministracion());
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setText(vias.get(position).getTipoAdministracion());
                view.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        adapterVia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemROA.setAdapter(adapterVia);

        int indexVia = 0;
        for (int i = 0; i < vias.size(); i++) {
            if (vias.get(i).getIdViaAdministracion() == articulo.getIdViaAdministracion()) {
                indexVia = i;
                break;
            }
        }
        spinnerItemROA.setSelection(indexVia);


        //SUVCATEGORIA
        List<SubCategoria> subcategorias = articuloDAO.getAllSubCategoria();
        subcategorias.add(0, new SubCategoria(-1, getString(R.string.select_subcategory), this));

        ArrayAdapter<SubCategoria> adapterSubCat = new ArrayAdapter<SubCategoria>(this, android.R.layout.simple_spinner_item, subcategorias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                } else {
                    view.setText(sub.getNombreSubCategoria());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                    
                } else {
                    view.setText(sub.getNombreSubCategoria() + " (ID: " + sub.getIdSubCategoria() + ")");
                    
                }
                return view;
            }
        };
        adapterSubCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemSubCategoria.setAdapter(adapterSubCat);

        //FOrMA Farmaceutica
        List<FormaFarmaceutica> formas = articuloDAO.getAllFormaFarmaceutica();
        formas.add(0, new FormaFarmaceutica(-1, getString(R.string.select_pharma_form), this));

        boolean formaExiste = false;
        if (articulo.getIdFormaFarmaceutica() != null) {
            for (FormaFarmaceutica f : formas) {
                if (f.getIdFormaFarmaceutica() == articulo.getIdFormaFarmaceutica()) {
                    formaExiste = true;
                    break;
                }
            }
            if (!formaExiste) {
                formas.add(0, new FormaFarmaceutica(articulo.getIdFormaFarmaceutica(), "NULL", this));
            }
        }

        ArrayAdapter<FormaFarmaceutica> adapterForma = new ArrayAdapter<FormaFarmaceutica>(this, android.R.layout.simple_spinner_item, formas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(formas.get(position).getTipoFormaFarmaceutica());
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setText(formas.get(position).getTipoFormaFarmaceutica());
                view.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        adapterForma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemFormaFarmaceutica.setAdapter(adapterForma);

        int indexForma = 0;
        for (int i = 0; i < formas.size(); i++) {
            if (formas.get(i).getIdFormaFarmaceutica() == articulo.getIdFormaFarmaceutica()) {
                indexForma = i;
                break;
            }
        }
        spinnerItemFormaFarmaceutica.setSelection(indexForma);

        // seleccionar relacionada
        for (int i = 0; i < marcas.size(); i++) {
            if (marcas.get(i).getIdMarca() == articulo.getIdMarca()) {
                spinnerItemMarca.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < subcategorias.size(); i++) {
            if (subcategorias.get(i).getIdSubCategoria() == articulo.getIdSubCategoria()) {
                spinnerItemSubCategoria.setSelection(i);
                break;
            }
        }

        idArticulo.setText(Integer.toString(articulo.getIdArticulo()));
        name.setText(articulo.getNombreArticulo());
        description.setText(articulo.getDescripcionArticulo());
        isRestricted.setChecked(articulo.getRestringidoArticulo());
        price.setText(Double.toString(articulo.getPrecioArticulo()));

        spinnerItemMarca.setEnabled(false);
        spinnerItemROA.setEnabled(false);
        spinnerItemSubCategoria.setEnabled(false);
        spinnerItemFormaFarmaceutica.setEnabled(false);
        idArticulo.setEnabled(false);
        name.setEnabled(false);
        description.setEnabled(false);
        isRestricted.setEnabled(false);
        price.setEnabled(false);

        btnSaveArticulo.setVisibility(GONE);
        btnClear.setVisibility(GONE);

        dialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selected = (Categoria) parent.getItemAtPosition(position);
        ListView listV = findViewById(R.id.itemListv);

        boolean permisoActualizar = vac.validarAcceso(3);
        boolean permisoEliminar = vac.validarAcceso(4);
        boolean permisoVer = vac.validarAcceso(2);

        if (permisoActualizar || permisoEliminar) {
            listV.setVisibility(View.VISIBLE);
            actualizarListView(selected);
        } else if (permisoVer && selected.getIdCategoria() != -1) {
            listV.setVisibility(View.VISIBLE);
            actualizarListView(selected);
        } else {
            listV.setVisibility(View.INVISIBLE);
        }
    }


    public void editArticulo(Articulo articulo, AlertDialog dialogoPadre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_articulo, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Spinners y campos
        Spinner spinnerItemMarca = dialogView.findViewById(R.id.spinnerItemMarca);
        Spinner spinnerItemROA = dialogView.findViewById(R.id.spinnerItemROA);
        Spinner spinnerItemSubCategoria = dialogView.findViewById(R.id.spinnerItemSubCategoria);
        Spinner spinnerItemFormaFarmaceutica = dialogView.findViewById(R.id.spinnerItemFormaFarmaceutica);

        EditText idArticulo = dialogView.findViewById(R.id.editTextItemId);
        EditText name = dialogView.findViewById(R.id.editTextItemName);
        EditText description = dialogView.findViewById(R.id.editTextItemDescription);
        CheckBox isRestricted = dialogView.findViewById(R.id.checkBoxItemRestricted);
        EditText price = dialogView.findViewById(R.id.editTextItemPrice);

        // Cargar adaptadores
        List<Marca> marcas = articuloDAO.getAllMarca();
        marcas.add(0, new Marca(-1, getString(R.string.select_brand), this));

        ArrayAdapter<Marca> adapterMarca = new ArrayAdapter<Marca>(this, android.R.layout.simple_spinner_item, marcas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                } else {
                    view.setText(marca.getNombreMarca());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Marca marca = getItem(position);
                if (marca.getIdMarca() == -1) {
                    view.setText(getString(R.string.select_brand));
                    
                } else {
                    view.setText(marca.getNombreMarca() + " (ID: " + marca.getIdMarca() + ")");
                    
                }
                return view;
            }
        };
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemMarca.setAdapter(adapterMarca);


        // VIA AMNISTRACION
        List<ViaAdministracion> vias = articuloDAO.getAllViaAdministracion();
        vias.add(0, new ViaAdministracion(-1, getString(R.string.select_admin_route), this));

        ArrayAdapter<ViaAdministracion> adapterVia = new ArrayAdapter<ViaAdministracion>(this, android.R.layout.simple_spinner_item, vias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                ViaAdministracion via = getItem(position);
                if (via.getIdViaAdministracion() == -1) {
                    view.setText(getString(R.string.select_admin_route));
                } else {
                    view.setText(via.getTipoAdministracion());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                ViaAdministracion via = getItem(position);
                if (via.getIdViaAdministracion() == -1) {
                    view.setText(getString(R.string.select_admin_route));
                    
                } else {
                    view.setText(via.getTipoAdministracion() + " (ID: " + via.getIdViaAdministracion() + ")");
                    
                }
                return view;
            }
        };
        adapterVia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemROA.setAdapter(adapterVia);

        //SUVCATEGORIA
        List<SubCategoria> subcategorias = articuloDAO.getAllSubCategoria();
        subcategorias.add(0, new SubCategoria(-1, getString(R.string.select_subcategory), this));

        ArrayAdapter<SubCategoria> adapterSubCat = new ArrayAdapter<SubCategoria>(this, android.R.layout.simple_spinner_item, subcategorias) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                } else {
                    view.setText(sub.getNombreSubCategoria());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SubCategoria sub = getItem(position);
                if (sub.getIdSubCategoria() == -1) {
                    view.setText(getString(R.string.select_subcategory));
                    
                } else {
                    view.setText(sub.getNombreSubCategoria() + " (ID: " + sub.getIdSubCategoria() + ")");
                    
                }
                return view;
            }
        };
        adapterSubCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemSubCategoria.setAdapter(adapterSubCat);

        //FOrMA Farmaceutica
        List<FormaFarmaceutica> formas = articuloDAO.getAllFormaFarmaceutica();
        formas.add(0, new FormaFarmaceutica(-1, getString(R.string.select_pharma_form), this));

        ArrayAdapter<FormaFarmaceutica> adapterForma = new ArrayAdapter<FormaFarmaceutica>(this, android.R.layout.simple_spinner_item, formas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                FormaFarmaceutica forma = getItem(position);
                if (forma.getIdFormaFarmaceutica() == -1) {
                    view.setText(getString(R.string.select_pharma_form));
                } else {
                    view.setText(forma.getTipoFormaFarmaceutica());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                FormaFarmaceutica forma = getItem(position);
                if (forma.getIdFormaFarmaceutica() == -1) {
                    view.setText(getString(R.string.select_pharma_form));
                    
                } else {
                    view.setText(forma.getTipoFormaFarmaceutica() + " (ID: " + forma.getIdFormaFarmaceutica() + ")");
                    
                }
                return view;
            }
        };
        adapterForma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemFormaFarmaceutica.setAdapter(adapterForma);

        seleccionarItemSpinner(spinnerItemMarca, marcas, articulo.getIdMarca());
        seleccionarItemSpinner(spinnerItemROA, vias, articulo.getIdViaAdministracion());
        seleccionarItemSpinner(spinnerItemSubCategoria, subcategorias, articulo.getIdSubCategoria());
        seleccionarItemSpinner(spinnerItemFormaFarmaceutica, formas, articulo.getIdFormaFarmaceutica());

        idArticulo.setText(String.valueOf(articulo.getIdArticulo()));
        name.setText(articulo.getNombreArticulo());
        description.setText(articulo.getDescripcionArticulo());
        isRestricted.setChecked(articulo.getRestringidoArticulo());
        price.setText(String.valueOf(articulo.getPrecioArticulo()));
        idArticulo.setEnabled(false);

        Button btnSaveArticulo = dialogView.findViewById(R.id.btnGuardarArticulo);
        btnSaveArticulo.setOnClickListener(v -> {
            boolean hayError = false;

            String nombre = name.getText().toString().trim();
            String descripcion = description.getText().toString().trim();
            String precioTxt = price.getText().toString().trim();

            Marca marca = (Marca) spinnerItemMarca.getSelectedItem();
            SubCategoria sub = (SubCategoria) spinnerItemSubCategoria.getSelectedItem();
            ViaAdministracion via = (ViaAdministracion) spinnerItemROA.getSelectedItem();
            FormaFarmaceutica forma = (FormaFarmaceutica) spinnerItemFormaFarmaceutica.getSelectedItem();

            if (nombre.isEmpty()) {
                name.setError(getString(R.string.field_empty));
                hayError = true;
            }
            if (descripcion.isEmpty()) {
                description.setError(getString(R.string.field_empty));
                hayError = true;
            }
            if (precioTxt.isEmpty()) {
                price.setError(getString(R.string.field_empty));
                hayError = true;
            }

            // Validar spinners obligatorios (marca y subcategoría)
            if (marca.getIdMarca() == -1) {
                View view = spinnerItemMarca.getSelectedView();
                if (view instanceof TextView) {
                    ((TextView) view).setError("");
                    ((TextView) view).setTextColor(Color.RED);
                }
                hayError = true;
            }
            if (sub.getIdSubCategoria() == -1) {
                View view = spinnerItemSubCategoria.getSelectedView();
                if (view instanceof TextView) {
                    ((TextView) view).setError("");
                    ((TextView) view).setTextColor(Color.RED);
                }
                hayError = true;
            }

            if (hayError) {
                Toast.makeText(this, getString(R.string.field_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                articulo.setIdMarca(marca.getIdMarca());
                articulo.setIdViaAdministracion(via.getIdViaAdministracion() == -1 ? null : via.getIdViaAdministracion());
                articulo.setIdSubCategoria(sub.getIdSubCategoria());
                articulo.setIdFormaFarmaceutica(forma.getIdFormaFarmaceutica() == -1 ? null : forma.getIdFormaFarmaceutica());
                articulo.setNombreArticulo(nombre);
                articulo.setDescripcionArticulo(descripcion);
                articulo.setRestringidoArticulo(isRestricted.isChecked());
                articulo.setPrecioArticulo(Double.parseDouble(precioTxt));

                boolean exito = articuloDAO.updateArticulo(articulo);
                if (exito) {
                    dialog.dismiss();
                    dialogoPadre.dismiss();
                    actualizarListView(selected);
                } else {
                    Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_SHORT).show();
            }
        });

        // Limpiar
        Button btnClear = dialogView.findViewById(R.id.btnLimpiarArticulo);
        btnClear.setOnClickListener(v -> {
            spinnerItemMarca.setSelection(0);
            spinnerItemROA.setSelection(0);
            spinnerItemSubCategoria.setSelection(0);
            spinnerItemFormaFarmaceutica.setSelection(0);
            name.setText("");
            description.setText("");
            isRestricted.setChecked(false);
            price.setText("");
        });

        dialog.show();
    }


    public void eliminarArticulo(Articulo articulo, AlertDialog dialogoPadre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView advertencia = dialogView.findViewById(R.id.confirmationText);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirm);
        Button btnCancelar = dialogView.findViewById(R.id.btnDecline);

        advertencia.setText(getText(R.string.confirm_delete_message) + ": " + articulo.getIdArticulo());

        btnConfirmar.setOnClickListener(v -> {
            int filasAfectadas = articuloDAO.deleteArticulo(articulo);
            if (filasAfectadas > 0) {
                actualizarListView(selected);
                Toast.makeText(this, getString(R.string.delete_message)+ ": " + articulo.getIdArticulo(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                dialogoPadre.dismiss();
            } else {
                Log.d("DELETE_ERROR", "No se eliminó el artículo");
            }
        });

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    public void buscarArticulo(int id) {
        Articulo articulo = articuloDAO.getArticulo(id);
        if (articulo != null) {
            busqueda = articulo;
            actualizarListView(null);
        }
        else{
            busqueda = null;
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {

    }

    public boolean areFieldsEmpty(EditText[] campos) {
        boolean hayVacios = false;
        for (EditText campo : campos) {
            if (campo.getText().toString().trim().isEmpty()) {
                campo.setError(getString(R.string.emptyWarning));
                hayVacios = true;
            }
        }
        return hayVacios;
    }

    private <T> void seleccionarItemSpinner(Spinner spinner, List<T> lista, Integer idBuscado) {
        if (idBuscado == null) {
            spinner.setSelection(0);
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            Object item = lista.get(i);
            if (item instanceof Marca && ((Marca) item).getIdMarca() == idBuscado) {
                spinner.setSelection(i); return;
            }
            if (item instanceof ViaAdministracion && ((ViaAdministracion) item).getIdViaAdministracion() == idBuscado) {
                spinner.setSelection(i); return;
            }
            if (item instanceof SubCategoria && ((SubCategoria) item).getIdSubCategoria() == idBuscado) {
                spinner.setSelection(i); return;
            }
            if (item instanceof FormaFarmaceutica && ((FormaFarmaceutica) item).getIdFormaFarmaceutica() == idBuscado) {
                spinner.setSelection(i); return;
            }
        }
    }

}