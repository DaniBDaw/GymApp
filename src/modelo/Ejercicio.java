package modelo;

import java.util.ArrayList;
import java.util.Objects;

public class Ejercicio {

	int id;
	String nombre;
	String descripcion;
	int orden;
	int descanso;
	ArrayList<Serie> series;

	public Ejercicio() {
		super();
	}

	public Ejercicio(int id, String nombre, String descripcion, int orden, int descanso, ArrayList<Serie> series) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.orden = orden;
		this.descanso = descanso;
		this.series = series;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public int getDescanso() {
		return descanso;
	}

	public void setDescanso(int descanso) {
		this.descanso = descanso;
	}

	public ArrayList<Serie> getSerie() {
		return series;
	}

	public void setSerie(ArrayList<Serie> serie) {
		this.series = serie;
	}

	@Override
	public int hashCode() {
		return Objects.hash(descanso, descripcion, id, nombre, orden, series);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ejercicio other = (Ejercicio) obj;
		return descanso == other.descanso && Objects.equals(descripcion, other.descripcion) && id == other.id
				&& Objects.equals(nombre, other.nombre) && Objects.equals(orden, other.orden)
				&& Objects.equals(series, other.series);
	}

	@Override
	public String toString() {
		return "Ejercicio [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", orden=" + orden
				+ ", descanso=" + descanso + ", serie=" + series + "]";
	}

}
