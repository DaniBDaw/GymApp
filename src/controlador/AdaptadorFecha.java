package controlador;

import java.io.IOException;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Le enseña a Gson a pasar un LocalDate a texto y al reves.
 *
 * Por que hace falta: Usuario (fechaNacimiento) e Historico (fecha) tienen un
 * LocalDate. Gson solo no sabe convertirlo (con Java 21 da error), asi que en
 * el JSON la fecha viaja como texto: "2004-05-12".
 *
 * Lo usan el cliente (Cliente) y el servidor (HiloCliente) con crearGson().
 */
public class AdaptadorFecha extends TypeAdapter<LocalDate> {

	/**
	 * Java -> JSON: LocalDate 2004-05-12 -> "2004-05-12" (null -> null).
	 */
	@Override
	public void write(JsonWriter salida, LocalDate fecha) throws IOException {
		if (fecha == null) {
			salida.nullValue();
		} else {
			salida.value(fecha.toString());
		}
	}

	/**
	 * JSON -> Java: "2004-05-12" -> LocalDate 2004-05-12 (null -> null).
	 */
	@Override
	public LocalDate read(JsonReader entrada) throws IOException {
		if (entrada.peek() == JsonToken.NULL) {
			entrada.nextNull();
			return null;
		}
		return LocalDate.parse(entrada.nextString());
	}

	/**
	 * Devuelve un Gson que ya sabe convertir las fechas con este adaptador.
	 *
	 * disableHtmlEscaping(): sin esto Gson cambia caracteres como = & < > ' por
	 * codigos (barra invertida + u003d en vez de "=") y las URLs de videos y fotos
	 * se estropean en el JSON. Asi se escriben tal cual.
	 */
	public static Gson crearGson() {
		return new GsonBuilder().registerTypeAdapter(LocalDate.class, new AdaptadorFecha()).disableHtmlEscaping()
				.create();
	}
}
