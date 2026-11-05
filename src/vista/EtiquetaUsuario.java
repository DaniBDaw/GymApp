package vista;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Nombre y nivel del usuario que ha hecho login, pegados a la derecha:
 * "Alexis · Nivel [icono] Intermedio". El nivel va con su icono y su color.
 * La usan las ventanas de Workouts, Detalle y Workout en curso.
 */
public class EtiquetaUsuario extends JPanel {

	private static final long serialVersionUID = 1L;

	// Color de cada nivel: la posicion del array es el numero de nivel (0 a 4)
	private static final Color[] COLORES_NIVEL = { Estilos.COLOR_NIVEL_NOVATO, Estilos.COLOR_NIVEL_PRINCIPIANTE,
			Estilos.COLOR_NIVEL_INTERMEDIO, Estilos.COLOR_NIVEL_AVANZADO, Estilos.COLOR_NIVEL_EXPERTO };

	private JLabel lblNombre;
	private JLabel lblNivel;

	public EtiquetaUsuario() {
		// FlowLayout pone las etiquetas una detras de otra, pegadas a la derecha
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		setOpaque(false);

		lblNombre = new JLabel();
		lblNombre.setFont(Estilos.FUENTE_MONO);
		lblNombre.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		add(lblNombre);

		// El icono del nivel va a la izquierda del texto del nivel
		lblNivel = new JLabel();
		lblNivel.setFont(Estilos.FUENTE_MONO_NEGRITA);
		lblNivel.setIconTextGap(5);
		add(lblNivel);
	}

	/**
	 * Ejemplo: setUsuario("Alexis", 2, "Intermedio")
	 * -> "Alexis · Nivel " + icono + "Intermedio" en el color del nivel 2.
	 */
	public void setUsuario(String nombre, int nivel, String nombreNivel) {
		lblNombre.setText(nombre + " · Nivel ");
		lblNivel.setText(nombreNivel);
		lblNivel.setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_" + nombreNivel.toLowerCase() + ".png"));
		if (nivel >= 0 && nivel < COLORES_NIVEL.length) {
			lblNivel.setForeground(COLORES_NIVEL[nivel]);
		} else {
			lblNivel.setForeground(Estilos.COLOR_CAJA);
		}
	}
}
