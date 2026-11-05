package controlador;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.AggregateQuerySnapshot;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import at.favre.lib.crypto.bcrypt.BCrypt;
import modelo.Ejercicio;
import modelo.Historico;
import modelo.Serie;
import modelo.Usuario;
import modelo.Workout;

public class GestionBD {

	private static final String CRED_FIREBASE = "assets/firebase/credenciales-firebase.json";
	private static final String USUARIOS_COLECTION = "usuarios";
	private static final String HISTORICOS_COLECTION = "Historico";

	private static final String WORKOUTS_COLECTION = "workouts";
	private static final String EJERCICIOS_COLECTION = "ejercicios";
	private static final String SERIES_COLECTION = "series";
	private static Firestore firestore;

	private static GestorFicheros gestorFic;

	public GestionBD() {
	}

//	Funcion para acceder a la base de datos de Firestore firebase
	public Firestore accesoFirebase() {
		try {
			if (firestore == null) {
				FileInputStream credFB = new FileInputStream(CRED_FIREBASE);

				FirebaseOptions opcionesFB = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(credFB)).build();

				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(opcionesFB);
				}

				firestore = FirestoreClient.getFirestore();

				gestorFic = new GestorFicheros(this);

			}
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		}
		return firestore;

	}

//	Funcion para agregar todos los valores especificados en el panel de registro a un objeto Usuario
//	Devuelve true si se ha creado y false si el email ya existe o hay un error
	public boolean registrarUsuario(String nombre, String apellidos, String mail, String passwd, LocalDate fechaNac) {
		try {
			if (!buscarMailFB(mail)) {

				// El id nuevo es el mas alto + 1 (contar los usuarios no vale: si se
				// borra uno, el id nuevo seria el de otro usuario y lo pisaria)
				int newID = obtenerSiguienteId(firestore.collection(USUARIOS_COLECTION));

				ArrayList<Historico> hist = new ArrayList<Historico>();

				Usuario newUser = new Usuario(newID, nombre, apellidos, mail, passwd, fechaNac, 0, hist);

				this.agregarUsuarioFB(newUser);

				this.agregarHistoricoPorUsuario(newID);

				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

//	Funcion para agregar los valores del usuario registrado a Firebase
	public void agregarUsuarioFB(Usuario user) {
		String contraHaseada = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("nombre", user.getNombre());
			data.put("apellidos", user.getApellidos());
			data.put("fecNacimiento", user.getFechaNacimiento().toString());
			data.put("email", user.getEmail());
			data.put("password", contraHaseada);
			data.put("nivel", user.getNivel());
			firestore.collection(USUARIOS_COLECTION).document(Integer.toString(user.getId())).set(data).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

//  Funcion para agregar el historico de ejercicios completados de cada usuario
	public void agregarHistoricoPorUsuario(int id) {
		try {
			ArrayList<Historico> historico = gestorFic.obtenerHistoricoUsuarioXML(id);
			if (historico == null || historico.isEmpty()) {

				Map<String, Object> data = new HashMap<>();

				firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id)).collection(HISTORICOS_COLECTION)
						.document("1").set(data).get();

			} else {
				for (int i = 0; i < historico.size(); i++) {
					Historico hist = historico.get(i);

					Map<String, Object> data = new HashMap<>();
					data.put("nombre", hist.getNombre());
					data.put("nivel", hist.getNivel());
					data.put("fecha", hist.getFecha().toString());
					data.put("tiempoTotal", hist.getTiempoTotal());
					data.put("tiempoPrevisto", hist.getTiempoPrevisto());
					data.put("porcentaje", hist.getPorcentaje());

					firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id))
							.collection(HISTORICOS_COLECTION).document(Integer.toString(hist.getId())).set(data).get();

				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

//	Funcion que recibe los datos del JSON de workouts de la app y agrega los daton en colecciones en firebase
	public void subirWorkoutsAFirebase() {
		ArrayList<Workout> listaWorkouts = gestorFic.procesarRutinas();
		// Referencia a la colección raíz 'workouts'
		CollectionReference workoutsRef = firestore.collection("workouts");

		System.out.println("Iniciando la carga de Workouts a Firebase...");

		for (Workout workout : listaWorkouts) {
			try {
				// 2. Preparar los datos del Workout (excluyendo la lista de ejercicios del
				// documento principal)
				Map<String, Object> datosWorkout = new HashMap<>();
				datosWorkout.put("nombre", workout.getNombre());
				datosWorkout.put("nivel", workout.getNivel());
				datosWorkout.put("videoURL", workout.getVideoURL());
				datosWorkout.put("numEjercicios", workout.getNumEjercicios());

				// Creamos el documento usando el ID del Workout como ID del documento en
				// Firebase
				DocumentReference workoutDocRef = workoutsRef.document(Integer.toString(workout.getId()));
				workoutDocRef.set(datosWorkout).get(); // El '.get()' bloquea el hilo para asegurar orden secuencial

				// 3. Crear la subcolección 'ejercicios' dentro de este Workout concreto
				CollectionReference ejerciciosRef = workoutDocRef.collection("ejercicios");

				for (Ejercicio ejercicio : workout.getEjercicio()) {
					Map<String, Object> datosEjercicio = new HashMap<>();
					datosEjercicio.put("nombre", ejercicio.getNombre());
					datosEjercicio.put("descripcion", ejercicio.getDescripcion());
					datosEjercicio.put("descanso", ejercicio.getDescanso());
					datosEjercicio.put("orden", ejercicio.getOrden());

					// Añadimos el ejercicio con un ID automático generado por Firestore
					DocumentReference ejercicioDocRef = ejerciciosRef.document(Integer.toString(ejercicio.getOrden()));
					ejercicioDocRef.set(datosEjercicio).get();

					// 4. Crear la subcolección 'series' dentro de este Ejercicio concreto
					CollectionReference seriesRef = ejercicioDocRef.collection("series");

					for (Serie serie : ejercicio.getSerie()) {

						Map<String, Object> datosSerie = new HashMap<>();
						datosSerie.put("nombre", serie.getNombre());
						datosSerie.put("repeticiones", serie.getRepeticiones());
						datosSerie.put("tiempo", serie.getTiempo());
						datosSerie.put("foto", serie.getFoto());
						datosSerie.put("orden", serie.getOrden());

						// Añadimos la serie con un ID automático
						seriesRef.document(Integer.toString(serie.getOrden())).set(datosSerie).get();
					}
				}

			} catch (Exception e) {
				System.err.println("Error al subir el workout con ID " + workout.getId() + ": " + e.getMessage());
			}
		}
		System.out.println("¡Proceso de carga de Workouts finalizado con éxito!");
	}

	// Funcion que valida que el mail exista y que la contraseña sea la misma
	public Usuario validarLogin(String mail, String passwd) {
		Usuario user = null;
		try {
			if (buscarMailFB(mail)) {
				user = devolverUsuario(mail);
				if (user != null) {
					BCrypt.Result result = BCrypt.verifyer().verify(passwd.toCharArray(), user.getPassword());
					if (!result.verified) {
						user = null;
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	// Funcion que valida que el usuario exista mediante correo y devuelva los datos
	// del usuario. El historico no se carga aqui: la aplicacion lo pide aparte
	// (OBTENER_HISTORICO -> obtenerHistoricoUsuario)
	private Usuario devolverUsuario(String mail) {
		Usuario usuario = null;
		try {
			ApiFuture<QuerySnapshot> queryUsuario = firestore.collection(USUARIOS_COLECTION).whereEqualTo("email", mail)
					.get();

			QuerySnapshot querySnapshot = queryUsuario.get();

			if (!querySnapshot.isEmpty()) {
				QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);

				usuario = document.toObject(Usuario.class);
				// El id no es un campo del documento, es su nombre ("1", "2"...)
				usuario.setId(leerIdDocumento(document));
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return usuario;
	}

	// Funcion que devuelve un entero de la cantidad de usuarios que hay en Firebase
	// (Sirve para poder hacer el ID autoincrementable)
	private int obtenerCantidadUsuarios() {
		try {
			CollectionReference usuariosRef = firestore.collection(USUARIOS_COLECTION);

			ApiFuture<AggregateQuerySnapshot> futureCount = usuariosRef.count().get();
			AggregateQuerySnapshot snapshot = futureCount.get();

			return (int) snapshot.getCount();

		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Error al contar documentos: " + e.getMessage());
			return -1;
		}
	}

//	Funcion que valida que el email exista en Firebase
	private boolean buscarMailFB(String mail) throws Exception {
		// Busca en la colección cualquier documento donde el campo "email" sea igual al
		// buscado
		ApiFuture<QuerySnapshot> query = firestore.collection(USUARIOS_COLECTION).whereEqualTo("email", mail).get();

		// Si la lista de resultados no está vacía, devuelve true
		// Si mail no existe en firebase devuelve false
		return !query.get().isEmpty();
	}

	public void datosInicialesFB() {
		this.subirWorkoutsAFirebase();
		gestorFic.cargarUsuariosDesdeCsv();
		int numUsuariosFB = this.obtenerCantidadUsuarios();

		for (int i = 1; i <= numUsuariosFB; i++) {
			this.agregarHistoricoPorUsuario(i);
		}

	}

	// ---------------------------------------------------------------
	// Ajustes del usuario (datos y contraseña)
	// ---------------------------------------------------------------

	// Funcion que dice si ya hay algun usuario con ese email en Firebase
	public boolean existeEmail(String mail) {
		try {
			return buscarMailFB(mail);
		} catch (Exception e) {
			System.err.println("Error al buscar el email: " + e.getMessage());
			return false;
		}
	}

	// Funcion que guarda en Firebase los datos personales del usuario.
	// update() solo cambia estos campos: la contraseña y el nivel no se tocan.
	// Devuelve false si no se ha podido guardar.
	public boolean actualizarDatosUsuario(int id, String nombre, String apellidos, LocalDate fechaNac, String mail) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("nombre", nombre);
			data.put("apellidos", apellidos);
			data.put("fecNacimiento", fechaNac.toString());
			data.put("email", mail);
			firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id)).update(data).get();
			return true;
		} catch (Exception e) {
			System.err.println("Error al guardar los datos del usuario " + id + ": " + e.getMessage());
			return false;
		}
	}

	// Funcion que cambia la contraseña del usuario en Firebase. Se guarda cifrada
	// con BCrypt, igual que al registrarse. Devuelve la contraseña cifrada (para
	// guardarla tambien en el usuario de la aplicacion) o null si ha fallado.
	public String actualizarPasswordUsuario(int id, String nuevaPasswd) {
		String contraHaseada = BCrypt.withDefaults().hashToString(12, nuevaPasswd.toCharArray());
		try {
			firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id)).update("password", contraHaseada)
					.get();
			return contraHaseada;
		} catch (Exception e) {
			System.err.println("Error al cambiar la contraseña del usuario " + id + ": " + e.getMessage());
			return null;
		}
	}

	// Funcion que cambia el nivel del usuario (al completar un workout de su
	// nivel sube al siguiente). Devuelve false si no se ha podido guardar.
	public boolean actualizarNivelUsuario(int id, int nivel) {
		try {
			firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id)).update("nivel", nivel).get();
			return true;
		} catch (Exception e) {
			System.err.println("Error al cambiar el nivel del usuario " + id + ": " + e.getMessage());
			return false;
		}
	}

	// Funcion que borra un usuario (la D del CRUD). Firestore NO borra las
	// subcolecciones solas: primero se borra cada documento de su historico y
	// despues el documento del usuario. Devuelve false si ha fallado.
	public boolean borrarUsuario(int id) {
		try {
			DocumentReference refUsuario = firestore.collection(USUARIOS_COLECTION).document(Integer.toString(id));

			List<QueryDocumentSnapshot> historicos = refUsuario.collection(HISTORICOS_COLECTION).get().get()
					.getDocuments();
			for (int i = 0; i < historicos.size(); i++) {
				historicos.get(i).getReference().delete().get();
			}

			refUsuario.delete().get();
			System.out.println("Usuario " + id + " borrado");
			return true;
		} catch (Exception e) {
			System.err.println("Error al borrar el usuario " + id + ": " + e.getMessage());
			return false;
		}
	}

	// ---------------------------------------------------------------
	// Historico del usuario
	// ---------------------------------------------------------------

	// Funcion que devuelve el historico de un usuario desde Firebase
	// (subcoleccion usuarios/{id}/Historico). Se lee campo a campo porque la
	// fecha esta guardada como texto y en la clase Historico es un LocalDate.
	public ArrayList<Historico> obtenerHistoricoUsuario(int idUsuario) {
		ArrayList<Historico> listaHistorico = new ArrayList<Historico>();
		try {
			QuerySnapshot consulta = firestore.collection(USUARIOS_COLECTION).document(Integer.toString(idUsuario))
					.collection(HISTORICOS_COLECTION).get().get();

			List<QueryDocumentSnapshot> documentos = consulta.getDocuments();
			for (int i = 0; i < documentos.size(); i++) {
				QueryDocumentSnapshot doc = documentos.get(i);

				// Al registrarse se crea un documento vacio (para que exista la
				// subcoleccion): no es un workout hecho, se salta
				if (doc.getString("nombre") == null) {
					continue;
				}

				Historico hist = new Historico();
				hist.setId(leerIdDocumento(doc));
				hist.setNombre(doc.getString("nombre"));
				hist.setNivel(leerEntero(doc, "nivel"));
				String fecha = doc.getString("fecha");
				if (fecha != null && !fecha.isEmpty()) {
					hist.setFecha(LocalDate.parse(fecha));
				}
				hist.setTiempoTotal(doc.getString("tiempoTotal"));
				hist.setTiempoPrevisto(doc.getString("tiempoPrevisto"));
				hist.setPorcentaje(leerEntero(doc, "porcentaje"));

				listaHistorico.add(hist);
			}
		} catch (Exception e) {
			System.err.println("Error al leer el historico del usuario " + idUsuario + ": " + e.getMessage());
		}
		return listaHistorico;
	}

	// Funcion que guarda un workout terminado en el historico del usuario
	// (usuarios/{id}/Historico/{n}). El tiempo previsto NO se guarda: se
	// calcula al enseñar el historico. Al Historico se le pone el id nuevo
	// (n) para poder borrarlo despues. Devuelve false si no se ha podido guardar.
	public boolean guardarHistorico(int idUsuario, Historico hist) {
		try {
			CollectionReference refHistorico = firestore.collection(USUARIOS_COLECTION)
					.document(Integer.toString(idUsuario)).collection(HISTORICOS_COLECTION);
			int nuevoId = obtenerSiguienteId(refHistorico);

			Map<String, Object> data = new HashMap<>();
			data.put("nombre", hist.getNombre());
			data.put("nivel", hist.getNivel());
			data.put("fecha", hist.getFecha().toString());
			data.put("tiempoTotal", hist.getTiempoTotal());
			data.put("porcentaje", hist.getPorcentaje());

			refHistorico.document(Integer.toString(nuevoId)).set(data).get();
			hist.setId(nuevoId);
			return true;
		} catch (Exception e) {
			System.err.println("Error al guardar el historico del usuario " + idUsuario + ": " + e.getMessage());
			return false;
		}
	}

	// Funcion que borra un workout del historico del usuario (el documento
	// usuarios/{id}/Historico/{idHistorico}). Devuelve false si ha fallado.
	public boolean borrarHistorico(int idUsuario, int idHistorico) {
		try {
			firestore.collection(USUARIOS_COLECTION).document(Integer.toString(idUsuario))
					.collection(HISTORICOS_COLECTION).document(Integer.toString(idHistorico)).delete().get();
			System.out.println("Historico " + idHistorico + " del usuario " + idUsuario + " borrado");
			return true;
		} catch (Exception e) {
			System.err.println("Error al borrar el historico del usuario " + idUsuario + ": " + e.getMessage());
			return false;
		}
	}

	// ---------------------------------------------------------------
	// Workouts
	// ---------------------------------------------------------------

	// Funcion que devuelve todos los workouts de Firebase con sus ejercicios y
	// series (workouts -> ejercicios -> series), ordenados por id.
	// Si falla (sin conexion) devuelve la lista vacia.
	public ArrayList<Workout> obtenerWorkouts() {
		ArrayList<Workout> listaWorkouts = new ArrayList<Workout>();
		try {
			List<QueryDocumentSnapshot> documentos = firestore.collection(WORKOUTS_COLECTION).get().get()
					.getDocuments();

			for (int i = 0; i < documentos.size(); i++) {
				QueryDocumentSnapshot doc = documentos.get(i);

				Workout workout = new Workout();
				workout.setId(leerIdDocumento(doc));
				workout.setNombre(doc.getString("nombre"));
				workout.setNivel(leerEntero(doc, "nivel"));
				workout.setVideoURL(doc.getString("videoURL"));
				workout.setNumEjercicios(leerEntero(doc, "numEjercicios"));
				workout.setEjercicio(obtenerEjercicios(doc.getReference()));

				listaWorkouts.add(workout);
			}
			ordenarWorkoutsPorId(listaWorkouts);
			System.out.println("Workouts cargados de Firebase: " + listaWorkouts.size());

		} catch (Exception e) {
			System.err.println("Error al leer los workouts de Firebase: " + e.getMessage());
		}
		return listaWorkouts;
	}

	// Ejercicios de un workout, ordenados por el campo "orden"
	private ArrayList<Ejercicio> obtenerEjercicios(DocumentReference refWorkout) throws Exception {
		ArrayList<Ejercicio> listaEjercicios = new ArrayList<Ejercicio>();
		List<QueryDocumentSnapshot> documentos = refWorkout.collection(EJERCICIOS_COLECTION).orderBy("orden").get()
				.get().getDocuments();

		for (int i = 0; i < documentos.size(); i++) {
			QueryDocumentSnapshot doc = documentos.get(i);

			Ejercicio ejercicio = new Ejercicio();
			ejercicio.setNombre(doc.getString("nombre"));
			ejercicio.setDescripcion(doc.getString("descripcion"));
			ejercicio.setDescanso(leerEntero(doc, "descanso"));
			ejercicio.setOrden(leerEntero(doc, "orden"));
			ejercicio.setSerie(obtenerSeries(doc.getReference()));

			listaEjercicios.add(ejercicio);
		}
		return listaEjercicios;
	}

	// Series de un ejercicio, ordenadas por el campo "orden"
	private ArrayList<Serie> obtenerSeries(DocumentReference refEjercicio) throws Exception {
		ArrayList<Serie> listaSeries = new ArrayList<Serie>();
		List<QueryDocumentSnapshot> documentos = refEjercicio.collection(SERIES_COLECTION).orderBy("orden").get().get()
				.getDocuments();

		for (int i = 0; i < documentos.size(); i++) {
			QueryDocumentSnapshot doc = documentos.get(i);

			Serie serie = new Serie();
			serie.setNombre(doc.getString("nombre"));
			serie.setRepeticiones(leerEntero(doc, "repeticiones"));
			serie.setTiempo(leerEntero(doc, "tiempo"));
			serie.setFoto(doc.getString("foto"));
			serie.setOrden(leerEntero(doc, "orden"));

			listaSeries.add(serie);
		}
		return listaSeries;
	}

	// Firebase devuelve los documentos ordenados como texto ("1", "10", "11",
	// "2"...).
	// Se ordenan por el id numerico con el metodo de la burbuja.
	private void ordenarWorkoutsPorId(ArrayList<Workout> lista) {
		for (int i = 0; i < lista.size() - 1; i++) {
			for (int j = 0; j < lista.size() - 1 - i; j++) {
				if (lista.get(j).getId() > lista.get(j + 1).getId()) {
					Workout aux = lista.get(j);
					lista.set(j, lista.get(j + 1));
					lista.set(j + 1, aux);
				}
			}
		}
	}

	// Firebase guarda los numeros como Long: se pasan a int (0 si no existe)
	private int leerEntero(DocumentSnapshot doc, String campo) {
		Long valor = doc.getLong(campo);
		if (valor == null) {
			return 0;
		}
		return valor.intValue();
	}

	// Siguiente id libre de una coleccion: el id mas alto + 1
	private int obtenerSiguienteId(CollectionReference coleccion) throws Exception {
		int maximo = 0;
		List<QueryDocumentSnapshot> documentos = coleccion.get().get().getDocuments();
		for (int i = 0; i < documentos.size(); i++) {
			int id = leerIdDocumento(documentos.get(i));
			if (id > maximo) {
				maximo = id;
			}
		}
		return maximo + 1;
	}

	// El id es el nombre del documento ("1", "2"...). 0 si no es un numero.
	private int leerIdDocumento(DocumentSnapshot doc) {
		try {
			return Integer.parseInt(doc.getId());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	// ---------------------------------------------------------------
	// Funciones de Ficheros
	// ---------------------------------------------------------------

	// Funcion para ordenar por ID los Usuarios en el ArrayList de usuarios
	private void ordenarUsuariosPorId(ArrayList<Usuario> lista) {
		for (int i = 0; i < lista.size() - 1; i++) {
			for (int j = 0; j < lista.size() - 1 - i; j++) {
				if (lista.get(j).getId() > lista.get(j + 1).getId()) {
					Usuario aux = lista.get(j);
					lista.set(j, lista.get(j + 1));
					lista.set(j + 1, aux);
				}
			}
		}
	}

//	Funcion para devolver un ArrayList de usuarios ordenado por ID (para el backup)
//	Devuelve null si no se han podido leer
	public ArrayList<Usuario> obtenerListadoUsuarios() {
		ArrayList<Usuario> listaUsuarios = new ArrayList<>();
		try {
			// Las consultas en Firestore no traen subcolecciones automáticamente
			ApiFuture<QuerySnapshot> future = firestore.collection("usuarios").get();
			List<QueryDocumentSnapshot> documents;

			documents = future.get().getDocuments();

			for (QueryDocumentSnapshot document : documents) {
				// toObject ya pasa la fecha "fecNacimiento" (texto) a LocalDate con
				// el @PropertyName de Usuario.setFecNacimientoString
				Usuario usuario = document.toObject(Usuario.class);
				usuario.setId(leerIdDocumento(document));
				listaUsuarios.add(usuario);
			}

		} catch (InterruptedException e) {
			System.out.println("Sin conexion a firebase");
			return null;
		} catch (ExecutionException e) {
			System.out.println("Usuarios no cargaron correctamente");
			return null;
		}

		ordenarUsuariosPorId(listaUsuarios);

		return listaUsuarios;
	}

}
