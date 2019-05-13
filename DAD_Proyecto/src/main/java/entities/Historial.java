package entities;

public class Historial {
	
	private Integer IdHistorial;
	private Integer IdAlimento;
	private String ultimaVez;
	
	public Historial(Integer idHistorial, Integer idAlimento, String ultimaVez) {
		super();
		IdHistorial = idHistorial;
		IdAlimento = idAlimento;
		this.ultimaVez = ultimaVez;
	}
	
	public Historial() {
		this(0, 0, "");
	}

	public Integer getIdHistorial() {
		return IdHistorial;
	}

	public void setIdHistorial(Integer idHistorial) {
		IdHistorial = idHistorial;
	}

	public Integer getIdAlimento() {
		return IdAlimento;
	}

	public void setIdAlimento(Integer idAlimento) {
		IdAlimento = idAlimento;
	}

	public String getUltimaVez() {
		return ultimaVez;
	}

	public void setUltimaVez(String ultimaVez) {
		this.ultimaVez = ultimaVez;
	}
	
	
	
	
	
	

}
