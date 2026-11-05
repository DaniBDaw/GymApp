package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import at.favre.lib.crypto.bcrypt.BCrypt;
import modelo.Ejercicio;
import modelo.Historico;
import modelo.Serie;
import modelo.Usuario;
import modelo.Workout;

public class GestorFicheros {
	private static final String RUTA_CSV = "assets/datosFB/usuarios_comun.csv";
	private static final String RUTA_XML = "assets/datosFB/historico_comun.xml";
	private static final String RUTA_GYM = "assets/datosFB/gimnasio.json";
	// Los backups van FUERA del proyecto: en una carpeta GymAppBackups en el
	// Escritorio (ej: C:\Users\Ana\Desktop\GymAppBackups). Si no existe se crea;
	// si ya existe, los ficheros se sobreescriben con los datos nuevos.
	public static final String CARPETA_BACKUPS = buscarEscritorio() + File.separator + "GymAppBackups";
	// Usuarios en binario (.dat); workouts e historico siempre en JSON
	private static final String BACKUP_USUARIOS = "usuarios.dat";
	private static final String BACKUP_WORKOUTS = "workouts.json";
	private static final String BACKUP_HISTORICO = "historico.json";

	private GestionBD gestionBD;

	// Gson para los ficheros JSON: sabe pasar las fechas (AdaptadorFecha) y
	// escribe con saltos de linea y sangria para que se pueda leer.
	// disableHtmlEscaping: las URLs se escriben tal cual (sin esto el "=" de
	// "?v=abc" se guarda como barra invertida + u003d)
	private Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new AdaptadorFecha())
			.setPrettyPrinting().disableHtmlEscaping().create();

	// Inyectamos la conexión a la base de datos por el constructor
	public GestorFicheros(GestionBD gestionBD) {
		this.gestionBD = gestionBD;
	}

	// Sin base de datos: para la aplicacion (cliente) y el proceso de backup,
	// que solo leen y escriben ficheros
	public GestorFicheros() {
	}

	// Carpeta del Escritorio del usuario. Normalmente es C:\Users\Ana\Desktop,
	// pero si el ordenador guarda el Escritorio en OneDrive esta en
	// C:\Users\Ana\OneDrive\Escritorio (o \Desktop). Se usa la primera que
	// exista (primero las de OneDrive: solo existen si el Escritorio esta
	// alli). Si no hay ninguna, la carpeta del usuario.
	private static String buscarEscritorio() {
		String usuario = System.getProperty("user.home");
		String[] posibles = { usuario + File.separator + "OneDrive" + File.separator + "Escritorio",
				usuario + File.separator + "OneDrive" + File.separator + "Desktop",
				usuario + File.separator + "Desktop" };
		for (int i = 0; i < posibles.length; i++) {
			if (new File(posibles[i]).exists()) {
				return posibles[i];
			}
		}
		return usuario;
	}

	public void cargarUsuariosDesdeCsv() {
		String linea;
		try (BufferedReader br = new BufferedReader(new FileReader(RUTA_CSV))) {

			// Leer y descartar la primera línea (cabecera)
			br.readLine();

			while ((linea = br.readLine()) != null) {
				// Separar los valores por comas
				String[] campos = linea.split(",");

				if (campos.length >= 7) {
					int id = Integer.parseInt(campos[0].trim());
					LocalDate fecNacimiento = LocalDate.parse(campos[1].trim());
					int nivel = Integer.parseInt(campos[2].trim());
					String nombre = campos[3].trim();
					String apellidos = campos[4].trim();
					String correo = campos[5].trim();
					String contrasena = campos[6].trim();

					ArrayList<Historico> hist = new ArrayList<Historico>();

					// Crear el objeto Usuario con los datos leídos
					Usuario usuario = new Usuario(id, nombre, apellidos, correo, contrasena, fecNacimiento, nivel,
							hist);

					// Enviar el usuario a la clase de gestión de base de datos
					gestionBD.agregarUsuarioFB(usuario);
				}
			}
			System.out.println("Proceso de lectura del CSV finalizado.");

		} catch (IOException e) {
			System.err.println("Error al leer el archivo CSV: " + e.getMessage());
		} catch (NumberFormatException e) {
			System.err.println("Error de formato numérico al procesar una línea: " + e.getMessage());
		}
	}

	// Historico de un usuario del XML comun (assets/datosFB/historico_comun.xml)
	public ArrayList<Historico> obtenerHistoricoUsuarioXML(int userID) {
		return leerHistoricoXML(new File(RUTA_XML), userID);
	}

	// Historico de un usuario del backup "historico.json" (modo sin conexion).
	// El fichero es una lista con un objeto por usuario (ver
	// guardarBackupHistorico): se busca el del usuario y se pasa su lista a
	// objetos Historico. Si no esta, devuelve la lista vacia.
	public ArrayList<Historico> obtenerHistoricoSinConexion(int userID) {
		ArrayList<Historico> historico = new ArrayList<Historico>();
		File archivo = new File(CARPETA_BACKUPS, BACKUP_HISTORICO);
		if (!archivo.exists()) {
			return historico;
		}

		try (FileReader lector = new FileReader(archivo, StandardCharsets.UTF_8)) {
			JsonArray usuarios = gson.fromJson(lector, JsonArray.class);
			for (int i = 0; i < usuarios.size(); i++) {
				JsonObject usuario = usuarios.get(i).getAsJsonObject();
				if (usuario.get("usuario").getAsInt() == userID) {
					// JSON (una lista) -> array de Historico -> ArrayList
					Historico[] lista = gson.fromJson(usuario.get("historico"), Historico[].class);
					for (int j = 0; j < lista.length; j++) {
						historico.add(lista[j]);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error al leer el historico JSON: " + e.getMessage());
		}
		return historico;
	}

	private ArrayList<Historico> leerHistoricoXML(File archivoXml, int userID) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(archivoXml);
			doc.getDocumentElement().normalize();

			// Obtener todos los nodos <historico> del documento
			NodeList listaNodosHistorico = doc.getElementsByTagName("historico");

			for (int i = 0; i < listaNodosHistorico.getLength(); i++) {
				Node nodoHistorico = listaNodosHistorico.item(i);

				if (nodoHistorico.getNodeType() == Node.ELEMENT_NODE) {
					Element elementoHistorico = (Element) nodoHistorico;

					// Leer el atributo "usuario" de la etiqueta actual
					int idUsuarioXml = Integer.parseInt(elementoHistorico.getAttribute("usuario"));

					// Comprobamos si es el usuario exacto que buscamos
					if (idUsuarioXml == userID) {
						ArrayList<Historico> listaHistoricos = new ArrayList<>();
						NodeList listadoWorkouts = elementoHistorico.getElementsByTagName("workout");

						for (int j = 0; j < listadoWorkouts.getLength(); j++) {
							Element elemEntrenamiento = (Element) listadoWorkouts.item(j);

							String nombre = elemEntrenamiento.getElementsByTagName("nombre").item(0).getTextContent();
							int nivel = Integer
									.parseInt(elemEntrenamiento.getElementsByTagName("nivel").item(0).getTextContent());
							String fechaHora = elemEntrenamiento.getElementsByTagName("fecha").item(0).getTextContent();
							LocalDateTime ldt = LocalDateTime.parse(fechaHora);
							LocalDate fecha = ldt.toLocalDate();
							String tiempoTotal = elemEntrenamiento.getElementsByTagName("tiempoTotal").item(0)
									.getTextContent();
							String tiempoPrevisto = elemEntrenamiento.getElementsByTagName("tiempoPrevisto").item(0)
									.getTextContent();
							int completado = Integer.parseInt(
									elemEntrenamiento.getElementsByTagName("completado").item(0).getTextContent());

							Historico hist = new Historico((j + 1), nombre, nivel, fecha, tiempoTotal, tiempoPrevisto,
									completado);

							listaHistoricos.add(hist);
						}

						// Devolvemos el objeto Historico exclusivo de este usuario
						return listaHistoricos;
					}
				}
			}

		} catch (Exception e) {
			System.err.println("Error al procesar el archivo XML: " + e.getMessage());
		}

		// Si no se encuentra el usuario en el XML, devuelve null
		return null;
	}

	public ArrayList<Workout> procesarRutinas() {
		ArrayList<Workout> listaWorkouts = new ArrayList<>();

		try (JsonReader reader = new JsonReader(new FileReader(RUTA_GYM))) {
			reader.beginObject(); // Entra en el objeto raíz '{'

			while (reader.hasNext()) {
				String claveRaiz = reader.nextName();

				if (claveRaiz.equals("workouts")) {
					reader.beginArray(); // Entra en el array de entrenamientos '['

					while (reader.hasNext()) {
						// 1. INICIA UN NUEVO WORKOUT (POJO Principal)
						Workout workout = new Workout();
						workout.setEjercicio(new ArrayList<>()); // Inicializa su lista interna

						reader.beginObject(); // Entra a las llaves del Workout '{'

						while (reader.hasNext()) {
							String claveWorkout = reader.nextName();
							switch (claveWorkout) {
							case "id" -> workout.setId(reader.nextInt());
							case "nombre" -> workout.setNombre(reader.nextString());
							case "nivel" -> workout.setNivel(reader.nextInt());
							case "videoURL" -> workout.setVideoURL(reader.nextString());
							case "numEjercicios" -> workout.setNumEjercicios(reader.nextInt());

							case "ejercicios" -> {
								reader.beginArray(); // Entra al array de ejercicios '['

								while (reader.hasNext()) {
									// 2. INICIA UN NUEVO EJERCICIO (POJO Secundario)
									Ejercicio ejercicio = new Ejercicio();
									ejercicio.setSerie(new ArrayList<>()); // Inicializa sus series

									reader.beginObject(); // Llaves del Ejercicio '{'
									while (reader.hasNext()) {
										String claveEjercicio = reader.nextName();
										switch (claveEjercicio) {
										case "nombre" -> ejercicio.setNombre(reader.nextString());
										case "descripcion" -> ejercicio.setDescripcion(reader.nextString());
										case "descanso" -> ejercicio.setDescanso(reader.nextInt());
										case "orden" -> ejercicio.setOrden(reader.nextInt());

										case "series" -> {
											reader.beginArray(); // Entra al array de series '['

											while (reader.hasNext()) {
												// 3. INICIA UNA NUEVA SERIE (POJO Terciario)
												Serie serie = new Serie();

												reader.beginObject(); // Llaves de la Serie '{'
												while (reader.hasNext()) {
													String claveSerie = reader.nextName();
													switch (claveSerie) {
													case "nombre" -> serie.setNombre(reader.nextString());
													case "repeticiones" -> serie.setRepeticiones(reader.nextInt());
													case "tiempo" -> serie.setTiempo(reader.nextInt());
													case "foto" -> serie.setFoto(reader.nextString());
													case "orden" -> serie.setOrden(reader.nextInt());
													default -> reader.skipValue();
													}
												}
												reader.endObject(); // Cierra las llaves de la Serie '}'

												// Guarda la serie procesada en el POJO Ejercicio
												ejercicio.getSerie().add(serie);
											}
											reader.endArray(); // Cierra el array de series ']'
										}
										default -> reader.skipValue();
										}
									}
									reader.endObject(); // Cierra las llaves del Ejercicio '}'

									// Guarda el ejercicio procesado en el POJO Workout
									workout.getEjercicio().add(ejercicio);
								}
								reader.endArray(); // Cierra el array de ejercicios ']'
							}
							default -> reader.skipValue();
							}
						}
						reader.endObject(); // Cierra las llaves del Workout '}'

						// 4. ¡WORKOUT TOTALMENTE RELLENADO! Se mete en la lista definitiva
						listaWorkouts.add(workout);
					}
					reader.endArray(); // Cierra el array de workouts ']'
				} else {
					reader.skipValue();
				}
			}
			reader.endObject(); // Cierra el objeto raíz '}'

		} catch (IOException e) {
			System.err.println("Error procesando rutinas de ejercicio: " + e.getMessage());
		}

		return listaWorkouts;
	}

//	---------------------------------------------------------
//	FUNCIONES DE BACKUPS
//	---------------------------------------------------------

//	Funcion para guardar todos los usuarios en el archivo de backup "usuarios.dat"
//	El historico NO se guarda aqui (en Usuario es "transient"): va en historico.json
//	Devuelve false si no se ha podido escribir
	public boolean guardarBackupUsuarios(ArrayList<Usuario> usuarios) {
		// Carpeta fuera del proyecto (CARPETA_BACKUPS)
		File carpeta = new File(CARPETA_BACKUPS);
		File archivo = new File(carpeta, BACKUP_USUARIOS);

		// Asegurar que la carpeta existe
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		// Escritura del ArrayList en el archivo .dat
		try (FileOutputStream fos = new FileOutputStream(archivo);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {

			for (Usuario usuario : usuarios) {
				oos.writeObject(usuario);
			}

			oos.writeObject(null);

			System.out.println("Backup guardado con éxito en: " + archivo.getAbsolutePath());
			return true;

		} catch (Exception e) {
			System.err.println("Error al escribir el archivo de respaldo: " + e.getMessage());
			return false;
		}
	}

//	Funcion que lee el archivo de backup "usuarios.dat"
//	Usar en caso de no tener acceso a internet
	public ArrayList<Usuario> leerBackupUsuarios() {
		File archivo = new File(CARPETA_BACKUPS, BACKUP_USUARIOS);
		ArrayList<Usuario> listaUsuarios = new ArrayList<>();

		// Si el archivo no existe, devolvemos la lista vacía para evitar errores
		if (!archivo.exists()) {
			System.out.println("El archivo de backup no existe en la ruta especificada.");
			return listaUsuarios;
		}

		// Abrimos los flujos de lectura de objetos binarios
		try (FileInputStream fis = new FileInputStream(archivo); ObjectInputStream ois = new ObjectInputStream(fis)) {

			Object objeto;
			while ((objeto = ois.readObject()) != null) {
				if (objeto instanceof Usuario) {
					listaUsuarios.add((Usuario) objeto);
				}
			}
			System.out.println("Backup cargado con éxito. Se han recuperado " + listaUsuarios.size() + " usuarios.");

		} catch (Exception e) {
			System.err.println("Error al leer el archivo de respaldo: " + e.getMessage());
			e.printStackTrace();
		}

		return listaUsuarios;
	}

//	Funcion para guardar todos los workouts (con sus ejercicios y series) en el
//	archivo de backup "workouts.json". Gson pasa la lista entera a JSON.
	public boolean guardarBackupWorkouts(ArrayList<Workout> workouts) {
		return escribirJson(BACKUP_WORKOUTS, gson.toJsonTree(workouts));
	}

//	Funcion que lee el archivo de backup "workouts.json"
	public ArrayList<Workout> leerBackupWorkouts() {
		File archivo = new File(CARPETA_BACKUPS, BACKUP_WORKOUTS);
		ArrayList<Workout> listaWorkouts = new ArrayList<>();

		if (!archivo.exists()) {
			System.out.println("El archivo de backup de workouts no existe.");
			return listaWorkouts;
		}

		try (FileReader lector = new FileReader(archivo, StandardCharsets.UTF_8)) {
			// JSON (una lista) -> array de Workout -> ArrayList
			Workout[] workouts = gson.fromJson(lector, Workout[].class);
			for (int i = 0; i < workouts.length; i++) {
				listaWorkouts.add(workouts[i]);
			}
		} catch (Exception e) {
			System.err.println("Error al leer el archivo de respaldo: " + e.getMessage());
		}

		return listaWorkouts;
	}

//	Funcion que guarda el historico de todos los usuarios en "historico.json".
//	Una lista con un objeto por usuario:
//	[
//	  { "usuario": 1,
//	    "historico": [ { "id": 2, "nombre": "Cardio suave", "nivel": 1,
//	                     "fecha": "2026-10-28", "tiempoTotal": "08:16",
//	                     "porcentaje": 100 } ] },
//	  ...
//	]
//	Devuelve false si no se ha podido escribir
	public boolean guardarBackupHistorico(ArrayList<Usuario> usuarios) {
		JsonArray lista = new JsonArray();
		for (Usuario usuario : usuarios) {
			ArrayList<Historico> historico = usuario.getHistorico();
			if (historico == null) {
				historico = new ArrayList<Historico>();
			}
			JsonObject objetoUsuario = new JsonObject();
			objetoUsuario.addProperty("usuario", usuario.getId());
			objetoUsuario.add("historico", gson.toJsonTree(historico));
			lista.add(objetoUsuario);
		}
		return escribirJson(BACKUP_HISTORICO, lista);
	}

//	Escribe el JSON en un fichero de la carpeta de backups (lo sobreescribe).
//	Si la carpeta no existe la crea. Devuelve false si no se ha podido escribir.
	private boolean escribirJson(String nombreFichero, JsonElement json) {
		File carpeta = new File(CARPETA_BACKUPS);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		File archivo = new File(carpeta, nombreFichero);
		try (FileWriter escritor = new FileWriter(archivo, StandardCharsets.UTF_8)) {
			gson.toJson(json, escritor);
			System.out.println("Backup guardado con éxito en: " + archivo.getAbsolutePath());
			return true;
		} catch (Exception e) {
			System.err.println("Error al escribir " + nombreFichero + ": " + e.getMessage());
			return false;
		}
	}

//	---------------------------------------------------------
//	MODO SIN CONEXION (cuando el servidor no contesta)
//	---------------------------------------------------------

//	Login sin conexion: busca el email en el backup "usuarios.dat" y comprueba
//	la contraseña. En el backup esta cifrada con BCrypt (como en Firebase).
//	Devuelve el usuario o null si no esta o la contraseña no coincide.
	public Usuario validarLoginSinConexion(String email, String password) {
		ArrayList<Usuario> usuarios = leerBackupUsuarios();
		for (Usuario usuario : usuarios) {
			if (usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(email)) {
				try {
					BCrypt.Result resultado = BCrypt.verifyer().verify(password.toCharArray(), usuario.getPassword());
					if (resultado.verified) {
						return usuario;
					}
				} catch (Exception e) {
					System.err.println("Contraseña del backup no valida: " + e.getMessage());
				}
				return null;
			}
		}
		return null;
	}

//	---------------------------------------------------------
//	WORKOUTS HECHOS SIN CONEXION (pendientes de subir al servidor)
//	Un fichero por usuario: pendientes_<id>.json (ej: pendientes_5.json)
//	en la carpeta de los backups. Es historico, por eso va en JSON (una
//	lista de Historico). Se suben en el proximo login con conexion.
//	---------------------------------------------------------

//	Lee los workouts pendientes del usuario. Si no hay fichero, lista vacia.
	public ArrayList<Historico> leerPendientes(int idUsuario) {
		File archivo = new File(CARPETA_BACKUPS, "pendientes_" + idUsuario + ".json");
		ArrayList<Historico> pendientes = new ArrayList<>();

		if (!archivo.exists()) {
			return pendientes;
		}

		try (FileReader lector = new FileReader(archivo, StandardCharsets.UTF_8)) {
			Historico[] lista = gson.fromJson(lector, Historico[].class);
			for (int i = 0; i < lista.length; i++) {
				pendientes.add(lista[i]);
			}
		} catch (Exception e) {
			System.err.println("Error al leer los workouts pendientes: " + e.getMessage());
		}

		return pendientes;
	}

//	Escribe la lista de pendientes del usuario (sobreescribe el fichero).
//	Si la lista esta vacia, borra el fichero: ya no queda nada por subir.
	public boolean guardarPendientes(int idUsuario, ArrayList<Historico> pendientes) {
		String nombreFichero = "pendientes_" + idUsuario + ".json";

		if (pendientes.isEmpty()) {
			new File(CARPETA_BACKUPS, nombreFichero).delete();
			return true;
		}

		return escribirJson(nombreFichero, gson.toJsonTree(pendientes));
	}

//	Añade un workout hecho sin conexion a los pendientes del usuario
	public boolean anadirPendiente(int idUsuario, Historico historico) {
		ArrayList<Historico> pendientes = leerPendientes(idUsuario);
		pendientes.add(historico);
		return guardarPendientes(idUsuario, pendientes);
	}

//	Workouts sin conexion: los del backup "workouts.json". Si todavia no hay
//	backup, los del fichero del proyecto (assets/datosFB/gimnasio.json).
	public ArrayList<Workout> obtenerWorkoutsSinConexion() {
		ArrayList<Workout> workouts = leerBackupWorkouts();
		if (workouts.isEmpty()) {
			System.out.println("Sin backup de workouts: se usan los de " + RUTA_GYM);
			workouts = procesarRutinas();
		}
		return workouts;
	}

}