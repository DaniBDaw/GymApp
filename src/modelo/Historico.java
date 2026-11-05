package modelo;

import java.time.LocalDate;
import java.util.Objects;

public class Historico {

	int id;
	String nombre;
	int nivel;
	LocalDate fecha;
	String tiempoTotal;
	String tiempoPrevisto;
	int porcentaje;

	public Historico() {
		super();
	}

	public Historico(int id, String nombre, int nivel, LocalDate fecha, String tiempoTotal, String tiempoPrevisto,
			int porcentaje) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.nivel = nivel;
		this.fecha = fecha;
		this.tiempoTotal = tiempoTotal;
		this.tiempoPrevisto = tiempoPrevisto;
		this.porcentaje = porcentaje;
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

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public String getTiempoTotal() {
		return tiempoTotal;
	}

	public void setTiempoTotal(String tiempoTotal) {
		this.tiempoTotal = tiempoTotal;
	}

	public String getTiempoPrevisto() {
		return tiempoPrevisto;
	}

	public void setTiempoPrevisto(String tiempoPrevisto) {
		this.tiempoPrevisto = tiempoPrevisto;
	}

	public int getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(int porcentaje) {
		this.porcentaje = porcentaje;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fecha, id, nivel, nombre, porcentaje, tiempoPrevisto, tiempoTotal);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Historico other = (Historico) obj;
		return Objects.equals(fecha, other.fecha) && id == other.id && nivel == other.nivel
				&& Objects.equals(nombre, other.nombre) && porcentaje == other.porcentaje
				&& Objects.equals(tiempoPrevisto, other.tiempoPrevisto)
				&& Objects.equals(tiempoTotal, other.tiempoTotal);
	}

	@Override
	public String toString() {
		return "Historico [id=" + id + ", nombre=" + nombre + ", nivel=" + nivel + ", fecha=" + fecha + ", tiempoTotal="
				+ tiempoTotal + ", tiempoPrevisto=" + tiempoPrevisto + ", porcentaje=" + porcentaje + "]";
	}

}