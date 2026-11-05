package modelo;

import java.util.ArrayList;
import java.util.Objects;

public class Workout {

	int id;
	String nombre;
	int nivel;
	String videoURL;
	int numEjercicios;
	ArrayList<Ejercicio> ejercicios;

	public Workout() {
		super();
	}

	public Workout(int id, String nombre, int nivel, String videoURL, int numEjercicios,
			ArrayList<Ejercicio> ejercicios) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.nivel = nivel;
		this.videoURL = videoURL;
		this.numEjercicios = numEjercicios;
		this.ejercicios = ejercicios;
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

	public String getVideoURL() {
		return videoURL;
	}

	public void setVideoURL(String videoURL) {
		this.videoURL = videoURL;
	}

	public int getNumEjercicios() {
		return numEjercicios;
	}

	public void setNumEjercicios(int numEjercicios) {
		this.numEjercicios = numEjercicios;
	}

	public ArrayList<Ejercicio> getEjercicio() {
		return ejercicios;
	}

	public void setEjercicio(ArrayList<Ejercicio> ejercicio) {
		this.ejercicios = ejercicio;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ejercicios, id, nivel, nombre, numEjercicios, videoURL);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Workout other = (Workout) obj;
		return Objects.equals(ejercicios, other.ejercicios) && id == other.id && nivel == other.nivel
				&& Objects.equals(nombre, other.nombre) && numEjercicios == other.numEjercicios
				&& Objects.equals(videoURL, other.videoURL);
	}

	@Override
	public String toString() {
		return "Workout [id=" + id + ", nombre=" + nombre + ", nivel=" + nivel + ", videoURL=" + videoURL
				+ ", numEjercicios=" + numEjercicios + ", ejercicios=" + ejercicios + "]";
	}

}
