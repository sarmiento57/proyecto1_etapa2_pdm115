package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ArticuloDAO {
    private SQLiteDatabase dbConection;
    private Context context;

    public ArticuloDAO(Context context, SQLiteDatabase dbConection) {
        this.context = context;
        this.dbConection = dbConection;
    }

    public ArticuloDAO() {
    }

    public boolean insertarArticulo(Articulo articulo) {
        long insercion = 0;
        //Comprobar que existan los registros de las llaves foraneas
        boolean marcaValida = validarForaneas(articulo.getIdMarca(), 1);
        boolean viaAdminValida = validarForaneas(articulo.getIdViaAdministracion(), 2);
        boolean subCatValida = validarForaneas(articulo.getIdSubCategoria(), 3);
        boolean formaFarmValida = validarForaneas(articulo.getIdFormaFarmaceutica(), 4);
        if (marcaValida && viaAdminValida && subCatValida && formaFarmValida) {
            ContentValues item = new ContentValues();
            item.put("IDARTICULO", articulo.getIdArticulo());
            item.put("IDMARCA", articulo.getIdMarca());
            item.put("IDVIAADMINISTRACION", articulo.getIdViaAdministracion());
            item.put("IDSUBCATEGORIA", articulo.getIdSubCategoria());
            item.put("IDFORMAFARMACEUTICA", articulo.getIdFormaFarmaceutica());
            item.put("NOMBREARTICULO", articulo.getNombreArticulo());
            item.put("DESCRIPCIONARTICULO", articulo.getDescripcionArticulo());
            item.put("RESTRINGIDOARTICULO", articulo.getRestringidoArticulo());
            item.put("PRECIOARTICULO", articulo.getPrecioArticulo());

            insercion = dbConection.insert("ARTICULO", null, item);
            if (insercion == -1) {
                Toast.makeText(this.context, R.string.duplicate_message, Toast.LENGTH_SHORT).show();
                return false;
            }
            Toast.makeText(context, R.string.save_message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    public Articulo getArticulo(int id){
        String [] idArticulo = {String.valueOf(id)};
        Cursor cursor = getDbConection().query("ARTICULO",null, "IDARTICULO = ?",idArticulo,null,null,null );

        if(cursor.moveToFirst()){
            Articulo articulo = new Articulo();
            articulo.setIdArticulo(cursor.getInt(0));
            articulo.setIdMarca(cursor.getInt(1));
            articulo.setIdViaAdministracion(cursor.getInt(2));
            articulo.setIdSubCategoria(cursor.getInt(3));
            articulo.setIdFormaFarmaceutica(cursor.getInt(4));
            articulo.setNombreArticulo(cursor.getString(5));
            articulo.setDescripcionArticulo(cursor.getString(6));
            articulo.setRestringidoArticulo(Boolean.getBoolean(cursor.getString(7)));
            articulo.setPrecioArticulo(cursor.getDouble(8));
            cursor.close();
            return articulo;
        }
        else{
            Toast.makeText(context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public ArrayList<Articulo> getAllRows() {
        ArrayList<Articulo> listado = new ArrayList<Articulo>();
        Cursor listadoDB = getDbConection().query("ARTICULO", null, null, null, null, null, null);
        if (listadoDB.moveToFirst()) {
            listadoDB.moveToFirst();
            for (int i = 0; i < listadoDB.getCount(); i++) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(listadoDB.getInt(0));
                articulo.setIdMarca(listadoDB.getInt(1));
                articulo.setIdViaAdministracion(listadoDB.getInt(2));
                articulo.setIdSubCategoria(listadoDB.getInt(3));
                articulo.setIdFormaFarmaceutica(listadoDB.getInt(4));
                articulo.setNombreArticulo(listadoDB.getString(5));
                articulo.setDescripcionArticulo(listadoDB.getString(6));
                int restringido = listadoDB.getInt(7);
                articulo.setRestringidoArticulo(restringido != 0);
                articulo.setPrecioArticulo(listadoDB.getDouble(8));
                listado.add(articulo);
                listadoDB.moveToNext();
            }
        }

        return listado;
    }

    public ArrayList<Articulo> getRowsFiltredByCategory(int idCategoria) {
        String[] id = {Integer.toString(idCategoria)};
        ArrayList<Articulo> listado = new ArrayList<Articulo>();
        Cursor listadoSubCats = getDbConection().query("SUBCATEGORIA", null, "IDCATEGORIA = ?", id, null, null, null);
        if (listadoSubCats.moveToFirst()) {
            listadoSubCats.moveToFirst();
            for (int i = 0; i < listadoSubCats.getCount(); i++) {
                String[] idSubCat = {listadoSubCats.getString(0)};
                Cursor listadoArticulos = getDbConection().query("ARTICULO", null, "IDSUBCATEGORIA = ?", idSubCat, null, null, null);
                if (listadoArticulos.moveToFirst()) {
                    listadoArticulos.moveToFirst();
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(listadoArticulos.getInt(0));
                    articulo.setIdMarca(listadoArticulos.getInt(1));
                    articulo.setIdViaAdministracion(listadoArticulos.getInt(2));
                    articulo.setIdSubCategoria(listadoArticulos.getInt(3));
                    articulo.setIdFormaFarmaceutica(listadoArticulos.getInt(4));
                    articulo.setNombreArticulo(listadoArticulos.getString(5));
                    articulo.setDescripcionArticulo(listadoArticulos.getString(6));
                    articulo.setRestringidoArticulo(Boolean.getBoolean(listadoArticulos.getString(7)));
                    articulo.setPrecioArticulo(listadoArticulos.getDouble(8));
                    listado.add(articulo);
                    listadoArticulos.moveToNext();
                }
            }
        }
        return listado;
    }

    public boolean updateArticulo(Articulo articulo) {
        String[] id = {Integer.toString(articulo.getIdArticulo())};
        Cursor item = getDbConection().query("ARTICULO", null, "IDARTICULO = ?", id, null, null, null);
        if (item.getCount() == 1) {
            boolean marcaValida = validarForaneas(articulo.getIdMarca(), 1);
            boolean viaAdminValida = validarForaneas(articulo.getIdViaAdministracion(), 2);
            boolean subCatValida = validarForaneas(articulo.getIdSubCategoria(), 3);
            boolean formaFarmValida = validarForaneas(articulo.getIdFormaFarmaceutica(), 4);
            if (!marcaValida || !viaAdminValida || !subCatValida || !formaFarmValida) {
                return false;
            } else if (marcaValida && viaAdminValida && subCatValida && formaFarmValida) {
                ContentValues cambios = new ContentValues();
                cambios.put("IDARTICULO", articulo.getIdArticulo());
                cambios.put("IDMARCA", articulo.getIdMarca());
                cambios.put("IDVIAADMINISTRACION", articulo.getIdViaAdministracion());
                cambios.put("IDSUBCATEGORIA", articulo.getIdSubCategoria());
                cambios.put("IDFORMAFARMACEUTICA", articulo.getIdFormaFarmaceutica());
                cambios.put("NOMBREARTICULO", articulo.getNombreArticulo());
                cambios.put("DESCRIPCIONARTICULO", articulo.getDescripcionArticulo());
                cambios.put("RESTRINGIDOARTICULO", articulo.getRestringidoArticulo());
                cambios.put("PRECIOARTICULO", articulo.getPrecioArticulo());

                int control = dbConection.update("ARTICULO", cambios, "IDARTICULO = ?", id);
                if (control == 1) {
                    Toast.makeText(context,R.string.update_message, Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        } else if (item.getCount() > 1) {
            Toast.makeText(this.context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(this.context, R.string.not_found_message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    public int deleteArticulo(Articulo articulo){
        String [] id = {Integer.toString(articulo.getIdArticulo())};
        int registros = dbConection.delete("ARTICULO","IDARTICULO = ?", id);
        return registros;
    }

    public SQLiteDatabase getDbConection() {
        return dbConection;
    }

    public void setDbConection(SQLiteDatabase dbConection) {
        this.dbConection = dbConection;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean validarForaneas(Integer idForanea, Integer opcion) {
    /* Opciones:
       1 - Marca
       2 - Vía Administración (puede ser nula)
       3 - SubCategoría
       4 - Forma Farmacéutica (puede ser nula)
    */

        switch (opcion) {
            case 1: // Marca
                String[] marca = {Integer.toString(idForanea)};
                Cursor findMarca = getDbConection().query("MARCA", null, "IDMARCA = ?", marca, null, null, null);
                if (findMarca.getCount() == 1) {
                    return true;
                } else {
                    Toast.makeText(this.context, R.string.brand_not_found, Toast.LENGTH_SHORT).show();
                    return false;
                }

            case 2: // Vía Administración (puede ser nula)
                if (idForanea == null) return true;
                String[] viaAdmin = {Integer.toString(idForanea)};
                Cursor findViaAdmin = getDbConection().query("VIAADMINISTRACION", null, "IDVIAADMINISTRACION = ?", viaAdmin, null, null, null);
                if (findViaAdmin.getCount() == 1) {
                    return true;
                } else {
                    Toast.makeText(this.context, R.string.admin_route_not_found, Toast.LENGTH_SHORT).show();
                    return false;
                }

            case 3: // SubCategoría
                String[] subCat = {Integer.toString(idForanea)};
                Cursor findSubCat = getDbConection().query("SUBCATEGORIA", null, "IDSUBCATEGORIA = ?", subCat, null, null, null);
                if (findSubCat.getCount() == 1) {
                    return true;
                } else {
                    Toast.makeText(this.context, R.string.sub_category_not_foud, Toast.LENGTH_SHORT).show();
                    return false;
                }

            case 4: // Forma Farmacéutica (puede ser nula)
                if (idForanea == null) return true;
                String[] formaFarm = {Integer.toString(idForanea)};
                Cursor findFormaFarm = getDbConection().query("FORMAFARMACEUTICA", null, "IDFORMAFARMACEUTICA = ?", formaFarm, null, null, null);
                if (findFormaFarm.getCount() == 1) {
                    return true;
                } else {
                    Toast.makeText(this.context, R.string.pharma_form_not_found, Toast.LENGTH_SHORT).show(); // ← este es el nuevo mensaje
                    return false;
                }

            default:
                return false;
        }
    }

    public List<Marca> getAllMarca() {
        List<Marca> lista = new ArrayList<>();
        String sql = "SELECT * FROM MARCA";
        Cursor cursor = dbConection.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IDMARCA"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("NOMBREMARCA"));

                lista.add(new Marca(id, nombre, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public List<ViaAdministracion> getAllViaAdministracion() {
        List<ViaAdministracion> lista = new ArrayList<>();
        String sql = "SELECT * FROM VIAADMINISTRACION";
        Cursor cursor = dbConection.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IDVIAADMINISTRACION"));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow("TIPOADMINISTRACION"));

                lista.add(new ViaAdministracion(id, tipo, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }


    public List<SubCategoria> getAllSubCategoria() {
        List<SubCategoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM SUBCATEGORIA";
        Cursor cursor = dbConection.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IDSUBCATEGORIA"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRESUBCATEGORIA"));

                lista.add(new SubCategoria(id, nombre, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public List<FormaFarmaceutica> getAllFormaFarmaceutica() {
        List<FormaFarmaceutica> lista = new ArrayList<>();
        String sql = "SELECT * FROM FORMAFARMACEUTICA";
        Cursor cursor = dbConection.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IDFORMAFARMACEUTICA"));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow("TIPOFORMAFARMACEUTICA"));

                lista.add(new FormaFarmaceutica(id, tipo, context));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }




}





