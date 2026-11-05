package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import com.google.cloud.firestore.annotation.PropertyName;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int id;
	String nombre;
	String apellidos;
	String email;
	String password;
	LocalDate fechaNacimiento;
	int nivel;
	transient ArrayList<Historico> historico;

	public Usuario() {
		super();
	}

	public Usuario(int id, String nombre, String apellidos, String email, String password, LocalDate fechaNacimiento,
			int nivel, ArrayList<Historico> historico) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.email = email;
		this.password = password;
		this.fechaNacimiento = fechaNacimiento;
		this.nivel = nivel;
		this.historico = historico;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	@PropertyName("fecNacimiento")
	public String getFecNacimientoString() {
		if (this.fechaNacimiento == null)
			return null;
		return this.fechaNacimiento.toString();
	}

	// el PropertyName sirve para usar la funcion como "setter" del valor
	// fecNacimiento
	// del usuario de firebase a la hora de sacar usuarios y transformarlos a
	// objetos de Java
	@PropertyName("fecNacimiento")
	public void setFecNacimientoString(String fechaString) {
		if (fechaString != null && !fechaString.isEmpty()) {
			try {
				this.fechaNacimiento = LocalDate.parse(fechaString);
			} catch (Exception e) {
				System.err.println("Error al transformar la fecha de Firestore: " + e.getMessage());
				this.fechaNacimiento = null;
			}
		}
	}

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public ArrayList<Historico> getHistorico() {
		return historico;
	}

	public void setHistorico(ArrayList<Historico> historico) {
		this.historico = historico;
	}

	@Override
	public int hashCode() {
		return Objects.hash(apellidos, email, fechaNacimiento, historico, id, nivel, nombre, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(apellidos, other.apellidos) && Objects.equals(email, other.email)
				&& Objects.equals(fechaNacimiento, other.fechaNacimiento) && Objects.equals(historico, other.historico)
				&& id == other.id && nivel == other.nivel && Objects.equals(nombre, other.nombre)
				&& Objects.equals(password, other.password);
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", apellidos=" + apellidos + ", email=" + email
				+ ", password=" + password + ", fechaNacimiento=" + fechaNacimiento + ", nivel=" + nivel
				+ ", historico=" + historico + "]";
	}

}
