package entities;

public class Usuario {
	
	private Integer IdUsuario;
	private String name;
	private String surname;
	private String contraseña;
	
	
	public Usuario(Integer idUsuario, String name, String surname, String contraseña) {
		super();
		IdUsuario = idUsuario;
		this.name = name;
		this.surname = surname;
		this.contraseña = contraseña;
	}
	
	public Usuario() {
		this(0, "", "", "");
	}

	public Integer getIdUsuario() {
		return IdUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		IdUsuario = idUsuario;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}
	
	
	
	

}
