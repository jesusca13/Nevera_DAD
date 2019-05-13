package entities;

public class Nevera {
	
	private Integer IdNevera;
	private Integer IdUsuario;
	
	public Nevera(Integer idNevera, Integer idUsuario) {
		super();
		IdNevera = idNevera;
		IdUsuario = idUsuario;
	}
	
	public Nevera() {
		this(0, 0);
	}

	public Integer getIdNevera() {
		return IdNevera;
	}

	public void setIdNevera(Integer idNevera) {
		IdNevera = idNevera;
	}

	public Integer getIdUsuario() {
		return IdUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		IdUsuario = idUsuario;
	}
	
	
	
	

}
