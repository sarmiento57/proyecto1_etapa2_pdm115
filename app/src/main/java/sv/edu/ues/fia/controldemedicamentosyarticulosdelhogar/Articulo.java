package sv.edu.ues.fia.controldemedicamentosyarticulosdelhogar;

import android.content.Context;

public class Articulo {
    private Integer idArticulo;
    private Integer idMarca;
    private Integer idViaAdministracion;
    private Integer idSubCategoria;
    private Integer idFormaFarmaceutica;
    private String nombreArticulo;
    private String descripcionArticulo;
    private Boolean restringidoArticulo;
    private Double precioArticulo;
    private Context context;

    public Articulo(Integer idArticulo, Integer idMarca, Integer idViaAdministracion, Integer idSubCategoria,
                    Integer idFormaFarmaceutica, String nombreArticulo, String descripcionArticulo,
                    Boolean restringidoArticulo, Double precioArticulo, Context context) {
        this.idArticulo = idArticulo;
        this.idMarca = idMarca;
        this.idViaAdministracion = idViaAdministracion;
        this.idSubCategoria = idSubCategoria;
        this.idFormaFarmaceutica = idFormaFarmaceutica;
        this.nombreArticulo = nombreArticulo;
        this.descripcionArticulo = descripcionArticulo;
        this.restringidoArticulo = restringidoArticulo;
        this.precioArticulo = precioArticulo;
        this.context = context;
    }

    public Articulo(int idArticulo, String nombreArticulo, Context context) {
        this.idArticulo = idArticulo;
        this.nombreArticulo = nombreArticulo;
        this.context = context;
    }

    public Articulo() {
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public Integer getIdViaAdministracion() {
        return idViaAdministracion;
    }

    public void setIdViaAdministracion(Integer idViaAdministracion) {
        this.idViaAdministracion = idViaAdministracion;
    }

    public Integer getIdSubCategoria() {
        return idSubCategoria;
    }

    public void setIdSubCategoria(Integer idSubCategoria) {
        this.idSubCategoria = idSubCategoria;
    }

    public Integer getIdFormaFarmaceutica() {
        return idFormaFarmaceutica;
    }

    public void setIdFormaFarmaceutica(Integer idFormaFarmaceutica) {
        this.idFormaFarmaceutica = idFormaFarmaceutica;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public void setDescripcionArticulo(String descripcionArticulo) {
        this.descripcionArticulo = descripcionArticulo;
    }

    public Boolean getRestringidoArticulo() {
        return restringidoArticulo;
    }

    public void setRestringidoArticulo(Boolean restringidoArticulo) {
        this.restringidoArticulo = restringidoArticulo;
    }

    public Double getPrecioArticulo() {
        return precioArticulo;
    }

    public void setPrecioArticulo(Double precioArticulo) {
        this.precioArticulo = precioArticulo;
    }

    public String toString() {
        if (idArticulo == -1) {
            return context.getString(R.string.select_articulo);
        }
        return "ID Articulo : " + getIdArticulo() + "\n" +"Nombre: " + getNombreArticulo();
    }
}

