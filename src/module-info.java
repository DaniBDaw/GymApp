/**
 * 
 */
/**
 * 
 */
module GymApp {
	requires firebase.admin;
	requires com.google.auth;
	requires com.google.auth.oauth2;
	requires google.cloud.core;
	requires google.cloud.firestore;
	requires com.google.api.apicommon;
	requires java.desktop;
	requires gson;
	requires java.sql;
	requires bcrypt;

	// Firestore y Gson crean los objetos del modelo (Usuario, Workout...) y
	// rellenan sus atributos: para eso necesitan permiso ("opens")
	opens modelo to google.cloud.firestore, gson;
}