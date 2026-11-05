package vista;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;

import javax.swing.ImageIcon;

/**
 * Metodos de ayuda que usan varias ventanas: cargar imagenes (del proyecto o
 * de internet), la fecha de hoy
 * y la foto borrosa de los avisos (DialogoSalir y DialogoCuentaAtras).
 */
public class UtilidadesVista {

	// Cuantas veces se hace la foto la mitad de pequeña (mas veces = mas borrosa)
	private static final int VECES_BORROSO = 4;

	// Nombres de los meses y su numero (enero = 1, febrero = 2...)
	private static final String[] NOMBRES_MESES = { "enero", "febrero", "marzo", "abril", "mayo", "junio",
			"julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" };
	private static final int[] NUMEROS_MESES = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

	/**
	 * Carga una imagen de la carpeta images del proyecto
	 * (ej: "images/iconos/ajustes.png"). Devuelve null si no existe.
	 */
	public static ImageIcon cargarImagen(String ruta) {
		if (!new File(ruta).exists()) {
			System.out.println("No se ha encontrado la imagen: " + ruta);
			return null;
		}
		return new ImageIcon(ruta);
	}

	/**
	 * Descarga una imagen de internet (ej: la foto de una serie) y la deja del
	 * tamano que se pide. Devuelve null si no hay URL, si esta mal escrita o si
	 * no se ha podido descargar (por ejemplo, sin internet).
	 * Tarda un poco (la descarga): no llamarlo desde el hilo de Swing.
	 */
	public static ImageIcon cargarImagenUrl(String rutaImagen, int ancho, int alto) {
		if (rutaImagen == null || rutaImagen.isEmpty()) {
			return null;
		}
		try {
			// (new URL(texto) ya no se recomienda desde Java 20: se pasa por URI)
			URL url = new URI(rutaImagen).toURL();
			ImageIcon icono = new ImageIcon(url);
			// Si no se ha podido descargar, el ancho es -1
			if (icono.getIconWidth() <= 0) {
				return null;
			}
			Image imagen = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
			return new ImageIcon(imagen);
		} catch (Exception e) {
			System.out.println("URL de imagen mal escrita: " + rutaImagen);
			return null;
		}
	}

	/**
	 * Devuelve la fecha de hoy en formato "17 de octubre".
	 */
	public static String obtenerFechaHoy() {
		LocalDate hoy = LocalDate.now();
		int dia = hoy.getDayOfMonth();
		int mes = hoy.getMonthValue();

		// Buscar el nombre del mes que corresponde al numero
		String nombreMes = "";
		for (int i = 0; i < NUMEROS_MESES.length; i++) {
			if (NUMEROS_MESES[i] == mes) {
				nombreMes = NOMBRES_MESES[i];
			}
		}

		return dia + " de " + nombreMes;
	}

	/**
	 * Foto borrosa de una ventana (el fondo de los avisos):
	 * 1. Se pinta la ventana en una imagen (como una captura de pantalla).
	 * 2. Se hace la mitad de pequeña varias veces: se pierden los detalles.
	 * 3. Se hace el doble de grande las mismas veces: vuelve a su tamano, pero
	 *    los detalles ya no estan y los colores se han mezclado (borrosa).
	 */
	public static BufferedImage crearFotoBorrosa(Container contenido) {
		int ancho = contenido.getWidth();
		int alto = contenido.getHeight();

		BufferedImage foto = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics gFoto = foto.getGraphics();
		contenido.paint(gFoto);
		gFoto.dispose();

		for (int i = 0; i < VECES_BORROSO; i++) {
			foto = cambiarTamano(foto, foto.getWidth() / 2, foto.getHeight() / 2);
		}
		for (int i = 0; i < VECES_BORROSO; i++) {
			foto = cambiarTamano(foto, foto.getWidth() * 2, foto.getHeight() * 2);
		}
		return foto;
	}

	/**
	 * Copia la imagen con otro tamano. BILINEAR = al cambiar el tamano mezcla
	 * los colores de los pixeles de al lado (no los copia tal cual).
	 */
	private static BufferedImage cambiarTamano(BufferedImage imagen, int ancho, int alto) {
		BufferedImage nueva = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = nueva.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(imagen, 0, 0, ancho, alto, null);
		g2.dispose();
		return nueva;
	}
}
