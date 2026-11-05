package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;

/**
 * Colores, fuentes, icono y cursor comunes para todas las ventanas. Asi todas
 * las ventanas tienen el mismo aspecto.
 */
public class Estilos {

	// ---------- COLORES ----------
	public static final Color COLOR_FONDO = new Color(255, 255, 255);
	public static final Color COLOR_TEXTO = new Color(28, 28, 28);
	public static final Color COLOR_TEXTO_SECUNDARIO = new Color(90, 90, 90);

	// Caja negra del login y sus elementos
	public static final Color COLOR_CAJA = new Color(0, 0, 0);
	public static final Color COLOR_TEXTO_CAJA = new Color(255, 255, 255);
	public static final Color COLOR_BOTON_CLARO = new Color(255, 255, 255);
	public static final Color COLOR_BOTON_CLARO_HOVER = new Color(215, 215, 215);

	// Botones negros de todas las ventanas (GUARDAR, CONTINUAR, ATRÁS...)
	public static final Color COLOR_BOTON_OSCURO = new Color(23, 21, 22);
	public static final Color COLOR_BOTON_OSCURO_HOVER = new Color(60, 60, 60);

	// Redondeo de las esquinas de los botones y de los campos de texto
	// (8 = radio de 4 px). Asi todos tienen las mismas esquinas
	public static final int ARCO_BOTON = 8;

	// Aro gris claro del circulo de carga
	public static final Color COLOR_CARGA_FONDO = new Color(225, 225, 225);

	// Errores (mismo rojo que el fondo de error)
	public static final Color COLOR_ERROR = new Color(208, 0, 0);
	// Botones rojos (ELIMINAR CUENTA) con el raton encima: un rojo mas oscuro
	public static final Color COLOR_ERROR_HOVER = new Color(150, 0, 0);

	// Ventana principal (home)
	public static final Color COLOR_SEPARADOR = new Color(230, 230, 230);
	public static final Color COLOR_TEXTO_TARJETA = new Color(200, 200, 200);
	public static final Color COLOR_PIE = new Color(160, 160, 160);

	// Color de cada nivel (el nivel actual se pinta asi, el siguiente igual pero
	// transparente)
	public static final Color COLOR_NIVEL_NOVATO = new Color(41, 98, 180);
	public static final Color COLOR_NIVEL_PRINCIPIANTE = new Color(46, 139, 87);
	public static final Color COLOR_NIVEL_INTERMEDIO = new Color(184, 134, 11);
	public static final Color COLOR_NIVEL_AVANZADO = new Color(190, 30, 45);
	public static final Color COLOR_NIVEL_EXPERTO = new Color(111, 45, 168);

	// ---------- FUENTES ----------
	public static final String FUENTE_PREFERIDA = "Segoe UI";
	public static final String FUENTE_ALTERNATIVA = "Arial";
	public static final String FAMILIA_FUENTE = elegirFamiliaFuente();

	// Fuente para los textos pequenos (fecha, "Pulsa cualquier tecla...")
	// Se carga desde el fichero fonts/JetBrainsMono-Regular.ttf del proyecto
	public static final String RUTA_FUENTE_MONO = "fonts/JetBrainsMono-Regular.ttf";
	public static final Font FUENTE_MONO = cargarFuenteMono(15);
	public static final Font FUENTE_MONO_NEGRITA = FUENTE_MONO.deriveFont(Font.BOLD, 15f);
	public static final Font FUENTE_MONO_PEQUENA = FUENTE_MONO.deriveFont(Font.PLAIN, 13f);
	// Texto de los botones de abajo de las ventanas (SALIR, VOLVER, GUARDAR...)
	public static final Font FUENTE_BOTON = FUENTE_MONO.deriveFont(Font.PLAIN, 17f);

	// Titulo grande de la caja de login
	public static final Font FUENTE_TITULO_LOGIN = new Font(FAMILIA_FUENTE, Font.BOLD, 52);

	// Ventana principal (home)
	public static final Font FUENTE_LOGO_TEXTO = new Font(FAMILIA_FUENTE, Font.BOLD, 22);
	// Normal + cursiva: mas fina que el nombre, para que destaque el nombre
	public static final Font FUENTE_BIENVENIDO = new Font(FAMILIA_FUENTE, Font.ITALIC, 22);
	public static final Font FUENTE_NOMBRE = new Font(FAMILIA_FUENTE, Font.BOLD, 56);
	public static final Font FUENTE_TARJETA = new Font(FAMILIA_FUENTE, Font.BOLD, 30);

	// ---------- ICONO Y CURSOR DE LA APLICACION ----------
	// Icono: sustituye al de Java en la barra de titulo y en la barra de tareas
	public static final String RUTA_ICONO_APP = "images/iconos/icono_app.png";
	public static final Image ICONO_APP = cargarIconoApp();

	// Cursor: triangulo negro con borde blanco
	public static final String RUTA_CURSOR = "images/iconos/cursor.png";
	public static final Cursor CURSOR_APP = crearCursorApp();

	private static Image cargarIconoApp() {
		ImageIcon icono = UtilidadesVista.cargarImagen(RUTA_ICONO_APP);
		if (icono == null) {
			return null;
		}
		return icono.getImage();
	}

	/**
	 * Crea el cursor a partir de la imagen. El Point es el pixel de la imagen que
	 * hace el clic (la punta del triangulo). Si no encuentra la imagen, se usa el
	 * cursor normal.
	 */
	private static Cursor crearCursorApp() {
		ImageIcon imagen = UtilidadesVista.cargarImagen(RUTA_CURSOR);
		if (imagen == null) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(imagen.getImage(), new Point(3, 3), "CursorGymApp");
	}

	/**
	 * Carga la fuente JetBrains Mono desde el fichero .ttf del proyecto. Si falla,
	 * usa la fuente "Monospaced" de Java.
	 */
	private static Font cargarFuenteMono(int tamano) {
		try {
			Font fuente = Font.createFont(Font.TRUETYPE_FONT, new File(RUTA_FUENTE_MONO));
			return fuente.deriveFont(Font.PLAIN, (float) tamano);
		} catch (Exception e) {
			System.out.println("No se ha podido cargar la fuente, se usa Monospaced: " + e.getMessage());
			return new Font(Font.MONOSPACED, Font.PLAIN, tamano);
		}
	}

	/**
	 * Devuelve "Segoe UI" si esta instalada en el sistema, si no "Arial".
	 */
	private static String elegirFamiliaFuente() {
		String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (int i = 0; i < fuentes.length; i++) {
			if (fuentes[i].equals(FUENTE_PREFERIDA)) {
				return FUENTE_PREFERIDA;
			}
		}
		return FUENTE_ALTERNATIVA;
	}
}
