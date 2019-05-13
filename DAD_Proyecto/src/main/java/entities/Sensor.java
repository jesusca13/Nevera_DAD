package entities;

public class Sensor {
	
	//objetos base de datos
	
	private Integer IdSensor;
	private Integer IdNevera;
	private String Nombre;
	
	public Sensor(Integer idSensor, Integer idNevera, String nombre) {
		super();
		IdSensor = idSensor;
		IdNevera = idNevera;
		Nombre = nombre;
	}

	public Sensor() {
		this(0,0,"");
	}

	public int getIdSensor() {
		return IdSensor;
	}

	public void setIdSensor(Integer idSensor) {
		IdSensor = idSensor;
	}

	public int getIdNevera() {
		return IdNevera;
	}

	public void setIdNevera(Integer idNevera) {
		IdNevera = idNevera;
	}

	public String getNombre() {
		return Nombre;
	}

	public void setNombre(String nombre) {
		Nombre = nombre;
	}

}
