package entities;

public class Temperatura {
	
	private Double FechaMedicion; //dia.hora -> 1203.1435 -> 12/03 14:35  
	private Double Valor;
	private Integer IdSensor;
	
	public Temperatura(Double fechaMedicion, Double valor, Integer idSensor) {
		super();
		FechaMedicion = fechaMedicion;
		Valor = valor;
		IdSensor = idSensor;
	}
	
	public Temperatura() {
		this(0., 0., 0);
	}

	public Double getFechaMedicion() {
		return FechaMedicion;
	}

	public void setFechaMedicion(Double fechaMedicion) {
		this.FechaMedicion = fechaMedicion;
	}

	public Double getValor() {
		return Valor;
	}

	public void setValor(Double valor) {
		this.Valor = valor;
	}

	public Integer getIdSensor() {
		return IdSensor;
	}

	public void setIdSensor(Integer idSensor) {
		this.IdSensor = idSensor;
	}
	
	
	
	
	
	
	

}
