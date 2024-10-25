
package AlgoritmoGeneration;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class AlgoritmoGen {

    private static char[][] matriz;
    private static int contadorGeneraciones = 1; // Variable para contar las generaciones
    private static final Random random = new Random();

    public static void main(String[] args) {

        Scanner consola = new Scanner(System.in);
        double[] formulasAptitud = {
            30.0, -23.0, 5.0, // Coeficientes para la fórmula del programador
            -4.0, -0.5, 10.0 // Coeficientes para la fórmula del bombero
        };

        System.out.println("Ingrese el valor de n: ");
        int n = consola.nextInt();
        System.out.println("¿Cuántas generaciones quiere?: ");
        int totalGeneraciones = consola.nextInt();

        System.out.println("¿Qué aptitud quieres?");
        System.out.println("1- programador: 30*x^2 - 23*x + 5");
        System.out.println("2- bombero: -4*(x-1)^2 - 0.5*x + 10");

        int formAp = consola.nextInt();

        matriz = primeraGeneracion(n);

        int sinMejora = 0;
        double mejorAptitudPromedio = Double.MIN_VALUE;

        for (int g = 0; g < totalGeneraciones; g++) {
            System.out.println("\n--- Generación " + (g + 1) + " ---");
            double[] decimalesGeneracion = convertirDecimal(matriz);
            double promedio = evalAptitud(decimalesGeneracion, formulasAptitud, formAp);

            // Condición de convergencia
            if (promedio <= mejorAptitudPromedio) {
                sinMejora++;
            } else {
                mejorAptitudPromedio = promedio;
                sinMejora = 0;
            }

            if (sinMejora >= 10) {
                System.out.println("Convergencia alcanzada. Terminando...");
                break;
            }

            matriz = siguienteGeneracion(matriz, decimalesGeneracion);
        }
    }

    public static char[][] primeraGeneracion(int n) {
        int filas = (int) Math.pow(2, n);
        int columnas = 10;
        char[][] matriz = new char[filas][columnas];

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = (char) (random.nextInt(2) + '0');
            }
        }

        System.out.println("Primera Generación:");
        imprimirMatriz(matriz);
        return matriz;
    }

    public static double[] convertirDecimal(char[][] matriz) {
        double[] valoresDecimales = new double[matriz.length];

        for (int i = 0; i < matriz.length; i++) {
            double genAnalizado = 0.0;
            int signo = matriz[i][0] == '0' ? -1 : 1;

            for (int j = 1; j < matriz[i].length - 1; j++) {
                genAnalizado += (matriz[i][j] - '0') * Math.pow(2, matriz[i].length - 2 - j);
            }

            if (matriz[i][matriz[i].length - 1] == '1') {
                genAnalizado += 0.5;
            }

            valoresDecimales[i] = signo * genAnalizado;
        }

        System.out.println("Decimal de la Generación:");
        for (double valor : valoresDecimales) {
            System.out.println(valor);
        }

        return valoresDecimales;
    }

    public static double evalAptitud(double[] decimales, double[] formulasAptitud, int formAp) {
        System.out.println("Aptitud de la Generación:");

        int coefIndex = (formAp - 1) * 3; // Índice del primer coeficiente de la fórmula seleccionada
        double sumaAptitudes = 0.0;

        List<AbstractMap.SimpleEntry<Integer, Double>> aptitudesConIndice = new ArrayList<>();

        for (int i = 0; i < decimales.length; i++) {
            double x = decimales[i];
            double aptitud = formulasAptitud[coefIndex] * Math.pow(x, 2)
                    + formulasAptitud[coefIndex + 1] * x
                    + formulasAptitud[coefIndex + 2];
            sumaAptitudes += aptitud;

            aptitudesConIndice.add(new AbstractMap.SimpleEntry<>(i + 1, aptitud));
        }

        aptitudesConIndice.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (AbstractMap.SimpleEntry<Integer, Double> entry : aptitudesConIndice) {
            System.out.println("Gen " + entry.getKey() + ": " + entry.getValue());
        }

        double promedio = sumaAptitudes / decimales.length;
        System.out.println("Generación " + contadorGeneraciones + " / Promedio: " + promedio);
        contadorGeneraciones++;
        return promedio;
    }

    public static char[][] siguienteGeneracion(char[][] matrizActual, double[] aptitudes) {
        int filas = matrizActual.length;
        char[][] nuevaGeneracion = new char[filas][matrizActual[0].length];

        // Aplicar elitismo (el mejor gen pasa a la nueva generación sin cambios)
        int mejorIndice = 0;
        for (int i = 1; i < aptitudes.length; i++) {
            if (aptitudes[i] > aptitudes[mejorIndice]) {
                mejorIndice = i;
            }
        }
        nuevaGeneracion[0] = matrizActual[mejorIndice]; // El mejor individuo permanece

        // Selección por ruleta para el resto de la nueva generación
        for (int i = 1; i < filas; i++) {
            char[] padre1 = seleccionarPorRuleta(aptitudes, matrizActual);
            char[] padre2 = seleccionarPorRuleta(aptitudes, matrizActual);
            char[][] hijosCruzados = cruzar(padre1, padre2);

            // Mutación
            if (random.nextDouble() < 0.01) { // 1% de probabilidad de mutación
                mutar(hijosCruzados[0]);
                mutar(hijosCruzados[1]);
            }

            nuevaGeneracion[i] = hijosCruzados[0];
        }

        System.out.println("Nueva Generación:");
        imprimirMatriz(nuevaGeneracion);
        return nuevaGeneracion;
    }

    public static char[] seleccionarPorRuleta(double[] aptitudes, char[][] matriz) {
        double sumaAptitudes = Arrays.stream(aptitudes).sum();
        double[] probabilidadesAcumuladas = new double[aptitudes.length];

        // Calcular probabilidades acumuladas
        for (int i = 0; i < aptitudes.length; i++) {
            probabilidadesAcumuladas[i] = (i == 0) ? aptitudes[i] / sumaAptitudes
                    : probabilidadesAcumuladas[i - 1] + aptitudes[i] / sumaAptitudes;
        }

        // Seleccionar un valor aleatorio
        double valorAleatorio = random.nextDouble();
        for (int i = 0; i < probabilidadesAcumuladas.length; i++) {
            if (valorAleatorio <= probabilidadesAcumuladas[i]) {
                return matriz[i]; // Devolver el individuo seleccionado
            }
        }

        return matriz[matriz.length - 1]; // Caso de borde (último individuo)
    }

    public static char[][] cruzar(char[] padre1, char[] padre2) {
        int puntoCruce = random.nextInt(padre1.length - 1) + 1; // Punto de cruce entre 1 y longitud - 1
        char[] hijo1 = new char[padre1.length];
        char[] hijo2 = new char[padre1.length];

        for (int i = 0; i < padre1.length; i++) {
            if (i < puntoCruce) {
                hijo1[i] = padre1[i];
                hijo2[i] = padre2[i];
            } else {
                hijo1[i] = padre2[i];
                hijo2[i] = padre1[i];
            }
        }

        return new char[][]{hijo1, hijo2};
    }

    public static void mutar(char[] gen) {
        int indiceMutacion = random.nextInt(gen.length);
        gen[indiceMutacion] = (gen[indiceMutacion] == '0') ? '1' : '0'; // Aplicar XOR
    }

    public static void imprimirMatriz(char[][] matriz) {
        for (char[] fila : matriz) {
            for (char gen : fila) {
                System.out.print(gen + " ");
            }
            System.out.println();
        }
    }
}
