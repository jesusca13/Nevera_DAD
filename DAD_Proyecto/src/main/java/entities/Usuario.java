package entities;

public class Usuario {
	
	private Integer IdUsuario;
	private String name;
	private String surname;
	private String contrase�a;
	
	
	public Usuario(Integer idUsuario, String name, String surname, String contrase�a) {
		super();
		IdUsuario = idUsuario;
		this.name = name;
		this.surname = surname;
		this.contrase�a = contrase�a;
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

	public String getContrase�a() {
		return contrase�a;
	}

	public void setContrase�a(String contrase�a) {
		this.contrase�a = contrase�a;
	}
	
	
	
	

}
