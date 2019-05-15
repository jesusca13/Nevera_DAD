package entities;

public class Producto {
	
	private Integer IdProducto;
	private Integer cantidad;
	private Double FechaCaducidad;
	private Integer IdAlimento;
	private Integer IdNevera;
	
	public Producto(Integer idProducto, Integer cantidad, Double fechaCaducidad, Integer idAlimento, Integer idNevera) {
		super();
		IdProducto = idProducto;
		this.cantidad = cantidad;
		FechaCaducidad = fechaCaducidad;
		IdAlimento = idAlimento;
		IdNevera = idNevera;
	} 
	
	public Producto() {
		this( 0,0,null,0,0);
	}
	
	public Integer getIdNevera() {
		return IdNevera;
	}

	public void setIdNevera(Integer idNevera) {
		IdNevera = idNevera;
	}

	public Integer getIdProducto() {
		return IdProducto;
	}

	public void setIdProducto(Integer idProducto) {
		this.IdProducto = idProducto;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Double getFechaCaducidad() {
		return FechaCaducidad;
	}

	public void setFechaCaducidad(Double fechaCaducidad) {
		FechaCaducidad = fechaCaducidad;
	}

	public Integer getIdAlimento() {
		return IdAlimento;
	}

	public void setIdAlimento(Integer idAlimento) {
		this.IdAlimento = idAlimento;
	}

	
	
	
	
	

}
