package modelo;

import java.util.Objects;

public class Serie {

	int id;
	String nombre;
	int repeticiones;
	int tiempo;
	String foto;
	int orden;

	public Serie() {
		super();
	}

	public Serie(int id, String nombre, int repeticiones, int tiempo, String foto, int orden) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.repeticiones = repeticiones;
		this.tiempo = tiempo;
		this.foto = foto;
		this.orden = orden;
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

	public int getRepeticiones() {
		return repeticiones;
	}

	public void setRepeticiones(int repeticiones) {
		this.repeticiones = repeticiones;
	}

	public int getTiempo() {
		return tiempo;
	}

	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	@Override
	public int hashCode() {
		return Objects.hash(foto, id, nombre, orden, repeticiones, tiempo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Serie other = (Serie) obj;
		return Objects.equals(foto, other.foto) && id == other.id && Objects.equals(nombre, other.nombre)
				&& orden == other.orden && repeticiones == other.repeticiones && tiempo == other.tiempo;
	}

	@Override
	public String toString() {
		return "Series [id=" + id + ", nombre=" + nombre + ", repeticiones=" + repeticiones + ", tiempo=" + tiempo
				+ ", foto=" + foto + ", orden=" + orden + "]";
	}

}
