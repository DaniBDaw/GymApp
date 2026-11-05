package controlador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import modelo.Historico;
import modelo.Usuario;
import modelo.Workout;

/**
 * CLIENTE: la "ventana" por la que la aplicacion habla con el servidor.
 *
 * La aplicacion (GestionVistas) ya no toca Firebase. Llama a estos metodos,
 * que tienen el mismo nombre que los de GestionBD, y cada uno:
 *  1. Prepara una peticion JSON: {"accion":"LOGIN","email":"...", ...}
 *  2. La envia al servidor por un Socket TCP (una linea de texto).
 *  3. Lee la respuesta JSON del servidor: {"ok":true, "usuario":{...}}
 *  4. Pasa el JSON a objetos Java (Usuario, Workout...) con Gson.
 *
 * Cada peticion abre su propia conexion y la cierra al terminar (como
 * llamar por telefono, preguntar y colgar). En el servidor cada conexion la
 * atiende un hilo (HiloCliente).
 *
 * Si el servidor no esta encendido o no contesta, enviar() devuelve null
 * y los metodos devuelven null / false / una lista vacia.
 */
public class Cliente {

	// Donde esta el servidor. Si el servidor va en otro ordenador, aqui se
	// pone su IP (ej: "192.168.1.20"). El puerto tiene que ser el mismo que
	// el de Servidor.PUERTO
	private static final String IP_SERVIDOR = "localhost";
	private static final int PUERTO = 5000;

	// Tiempo maximo para conectar (2 s) y para esperar la respuesta (60 s,
	// porque leer todos los workouts de Firebase tarda unos segundos)
	private static final int TIEMPO_CONEXION = 2000;
	private static final int TIEMPO_RESPUESTA = 60000;

	// Gson que sabe pasar las fechas (LocalDate) a texto y al reves
	private Gson gson = AdaptadorFecha.crearGson();

	// ---------------------------------------------------------------
	// Conexion con el servidor
	// ---------------------------------------------------------------

	/**
	 * true si el servidor esta encendido (se puede conectar con el). Solo
	 * abre la conexion y la cierra, no envia nada.
	 */
	public boolean hayConexion() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(IP_SERVIDOR, PUERTO), TIEMPO_CONEXION);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Envia la peticion al servidor y devuelve su respuesta. Las dos van en
	 * JSON en una sola linea de texto. Devuelve null si no hay conexion o el
	 * servidor no contesta.
	 */
	private JsonObject enviar(JsonObject peticion) {
		// try con recursos: el socket se cierra solo al terminar
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(IP_SERVIDOR, PUERTO), TIEMPO_CONEXION);
			socket.setSoTimeout(TIEMPO_RESPUESTA);

			// UTF-8 para que las tildes y la ñ lleguen bien
			PrintWriter salida = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
			BufferedReader entrada = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

			salida.println(peticion.toString());
			String linea = entrada.readLine();
			if (linea == null) {
				return null;
			}
			return gson.fromJson(linea, JsonObject.class);

		} catch (Exception e) {
			System.out.println("Sin respuesta del servidor: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Crea una peticion vacia con la accion que se le pide al servidor.
	 */
	private JsonObject crearPeticion(String accion) {
		JsonObject peticion = new JsonObject();
		peticion.addProperty("accion", accion);
		return peticion;
	}

	/**
	 * true si ha llegado respuesta y el servidor dice "ok": true.
	 */
	private boolean esCorrecta(JsonObject respuesta) {
		return respuesta != null && respuesta.get("ok").getAsBoolean();
	}

	// ---------------------------------------------------------------
	// Usuarios
	// ---------------------------------------------------------------

	/**
	 * LOGIN: el servidor comprueba el email y la contraseña en Firebase.
	 * Devuelve el usuario o null si no son correctos.
	 */
	public Usuario validarLogin(String email, String password) {
		JsonObject peticion = crearPeticion("LOGIN");
		peticion.addProperty("email", email);
		peticion.addProperty("password", password);

		JsonObject respuesta = enviar(peticion);
		if (!esCorrecta(respuesta)) {
			return null;
		}
		// JSON -> objeto Usuario
		return gson.fromJson(respuesta.get("usuario"), Usuario.class);
	}

	/**
	 * EXISTE_EMAIL: true si ya hay un usuario con ese email.
	 */
	public boolean existeEmail(String email) {
		JsonObject peticion = crearPeticion("EXISTE_EMAIL");
		peticion.addProperty("email", email);

		JsonObject respuesta = enviar(peticion);
		return esCorrecta(respuesta) && respuesta.get("existe").getAsBoolean();
	}

	/**
	 * REGISTRO: crea el usuario nuevo. true si se ha creado.
	 */
	public boolean registrarUsuario(String nombre, String apellidos, String email, String password,
			LocalDate fechaNacimiento) {
		JsonObject peticion = crearPeticion("REGISTRO");
		peticion.addProperty("nombre", nombre);
		peticion.addProperty("apellidos", apellidos);
		peticion.addProperty("email", email);
		peticion.addProperty("password", password);
		peticion.addProperty("fechaNacimiento", fechaNacimiento.toString());

		return esCorrecta(enviar(peticion));
	}

	/**
	 * ACTUALIZAR_DATOS: guarda nombre, apellidos, fecha y email. true si se
	 * ha guardado.
	 */
	public boolean actualizarDatosUsuario(int id, String nombre, String apellidos, LocalDate fechaNacimiento,
			String email) {
		JsonObject peticion = crearPeticion("ACTUALIZAR_DATOS");
		peticion.addProperty("id", id);
		peticion.addProperty("nombre", nombre);
		peticion.addProperty("apellidos", apellidos);
		peticion.addProperty("fechaNacimiento", fechaNacimiento.toString());
		peticion.addProperty("email", email);

		return esCorrecta(enviar(peticion));
	}

	/**
	 * CAMBIAR_PASSWORD: guarda la contraseña nueva (el servidor la cifra con
	 * BCrypt). Devuelve la contraseña cifrada o null si ha fallado.
	 */
	public String actualizarPasswordUsuario(int id, String nuevaPassword) {
		JsonObject peticion = crearPeticion("CAMBIAR_PASSWORD");
		peticion.addProperty("id", id);
		peticion.addProperty("password", nuevaPassword);

		JsonObject respuesta = enviar(peticion);
		if (!esCorrecta(respuesta)) {
			return null;
		}
		return respuesta.get("passwordCifrada").getAsString();
	}

	/**
	 * ACTUALIZAR_NIVEL: cambia el nivel del usuario. true si se ha guardado.
	 */
	public boolean actualizarNivelUsuario(int id, int nivel) {
		JsonObject peticion = crearPeticion("ACTUALIZAR_NIVEL");
		peticion.addProperty("id", id);
		peticion.addProperty("nivel", nivel);

		return esCorrecta(enviar(peticion));
	}

	/**
	 * BORRAR_USUARIO: borra el usuario y todo su historico. true si se ha
	 * borrado.
	 */
	public boolean borrarUsuario(int id) {
		JsonObject peticion = crearPeticion("BORRAR_USUARIO");
		peticion.addProperty("id", id);

		return esCorrecta(enviar(peticion));
	}

	/**
	 * OBTENER_USUARIOS: todos los usuarios (para el backup). null si falla.
	 */
	public ArrayList<Usuario> obtenerUsuarios() {
		JsonObject respuesta = enviar(crearPeticion("OBTENER_USUARIOS"));
		if (!esCorrecta(respuesta)) {
			return null;
		}
		// JSON (una lista) -> array de Usuario -> ArrayList
		Usuario[] usuarios = gson.fromJson(respuesta.get("usuarios"), Usuario[].class);
		ArrayList<Usuario> lista = new ArrayList<Usuario>();
		for (int i = 0; i < usuarios.length; i++) {
			lista.add(usuarios[i]);
		}
		return lista;
	}

	// ---------------------------------------------------------------
	// Workouts
	// ---------------------------------------------------------------

	/**
	 * OBTENER_WORKOUTS: todos los workouts con sus ejercicios y series. Si
	 * falla devuelve la lista vacia.
	 */
	public ArrayList<Workout> obtenerWorkouts() {
		ArrayList<Workout> lista = new ArrayList<Workout>();
		JsonObject respuesta = enviar(crearPeticion("OBTENER_WORKOUTS"));
		if (!esCorrecta(respuesta)) {
			return lista;
		}
		Workout[] workouts = gson.fromJson(respuesta.get("workouts"), Workout[].class);
		for (int i = 0; i < workouts.length; i++) {
			lista.add(workouts[i]);
		}
		return lista;
	}

	// ---------------------------------------------------------------
	// Historico
	// ---------------------------------------------------------------

	/**
	 * OBTENER_HISTORICO: los workouts hechos por el usuario. Si falla
	 * devuelve la lista vacia.
	 */
	public ArrayList<Historico> obtenerHistoricoUsuario(int idUsuario) {
		ArrayList<Historico> lista = new ArrayList<Historico>();
		JsonObject peticion = crearPeticion("OBTENER_HISTORICO");
		peticion.addProperty("id", idUsuario);

		JsonObject respuesta = enviar(peticion);
		if (!esCorrecta(respuesta)) {
			return lista;
		}
		Historico[] historicos = gson.fromJson(respuesta.get("historico"), Historico[].class);
		for (int i = 0; i < historicos.length; i++) {
			lista.add(historicos[i]);
		}
		return lista;
	}

	/**
	 * GUARDAR_HISTORICO: guarda un workout terminado en el historico del
	 * usuario. true si se ha guardado. Al historico se le pone el id con el
	 * que lo ha guardado el servidor (para poder borrarlo despues).
	 */
	public boolean guardarHistorico(int idUsuario, Historico historico) {
		JsonObject peticion = crearPeticion("GUARDAR_HISTORICO");
		peticion.addProperty("id", idUsuario);
		// objeto Historico -> JSON
		peticion.add("historico", gson.toJsonTree(historico));

		JsonObject respuesta = enviar(peticion);
		if (!esCorrecta(respuesta)) {
			return false;
		}
		historico.setId(respuesta.get("idHistorico").getAsInt());
		return true;
	}

	/**
	 * BORRAR_HISTORICO: borra un workout del historico del usuario. true si
	 * se ha borrado.
	 */
	public boolean borrarHistorico(int idUsuario, int idHistorico) {
		JsonObject peticion = crearPeticion("BORRAR_HISTORICO");
		peticion.addProperty("id", idUsuario);
		peticion.addProperty("idHistorico", idHistorico);

		return esCorrecta(enviar(peticion));
	}
}
