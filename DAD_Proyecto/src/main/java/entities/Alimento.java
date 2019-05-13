package entities;

public class Alimento {
	
	private Integer IdAlimento;
	private String Nombre;
	private Integer TemperaturaRecomendada;
	private Integer IdNevera;
	
	public Alimento(Integer idAlimento, String nombre, Integer temperaturaRecomendada, Integer idNevera) {
		super();
		IdAlimento = idAlimento;
		Nombre = nombre;
		TemperaturaRecomendada = temperaturaRecomendada;
		IdNevera = idNevera;
	}
	
	public Alimento() {
		this(0, "", 0, 0);
	}

	public Integer getIdNevera() {
		return IdNevera;
	}

	public void setIdNevera(Integer idNevera) {
		IdNevera = idNevera;
	}

	public Integer getIdAlimento() {
		return IdAlimento;
	}

	public void setIdAlimento(Integer idAlimento) {
		this.IdAlimento = idAlimento;
	}

	public String getNombre() {
		return Nombre;
	}

	public void setNombre(String nombre) {
		this.Nombre = nombre;
	}

	public Integer getTemperaturaRecomendada() {
		return TemperaturaRecomendada;
	}

	public void setTemperaturaRecomendada(Integer temperaturaRecomendada) {
		this.TemperaturaRecomendada = temperaturaRecomendada;
	}
	
}
