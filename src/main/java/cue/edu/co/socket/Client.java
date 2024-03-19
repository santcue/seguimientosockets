package cue.edu.co.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket; // Socket para la conexión con el servidor
    private DataInputStream bufferDeEntrada = null; // Flujo de entrada de datos desde el servidor
    private DataOutputStream bufferDeSalida = null; // Flujo de salida de datos hacia el servidor
    Scanner teclado = new Scanner(System.in); // Escáner para leer desde el teclado
    final String COMANDO_TERMINACION = "salir()"; // Comando para terminar la conexión

    // Método para establecer la conexión con el servidor
    public void levantarConexion(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto); // Crea un nuevo socket con la dirección IP y el puerto especificados
            mostrarTexto("Conectado a :" + socket.getInetAddress().getHostName()); // Muestra un mensaje de conexión exitosa
        } catch (Exception e) {
            mostrarTexto("Excepción al levantar conexión: " + e.getMessage()); // Muestra un mensaje de excepción si no se puede conectar
            System.exit(0); // Sale del programa
        }
    }

    // Método auxiliar para imprimir texto en la consola
    public static void mostrarTexto(String s) {
        System.out.println(s);
    }

    // Método para abrir los flujos de entrada y salida de datos
    public void abrirFlujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream()); // Crea un flujo de entrada de datos desde el servidor
            bufferDeSalida = new DataOutputStream(socket.getOutputStream()); // Crea un flujo de salida de datos hacia el servidor
            bufferDeSalida.flush(); // Limpia el flujo de salida
        } catch (IOException e) {
            mostrarTexto("Error en la apertura de flujos"); // Muestra un mensaje de error si no se pueden abrir los flujos
        }
    }

    // Método para enviar un mensaje al servidor
    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s); // Escribe el mensaje en el flujo de salida
            bufferDeSalida.flush(); // Limpia el flujo de salida
        } catch (IOException e) {
            mostrarTexto("IOException on enviar"); // Muestra un mensaje de error si ocurre una excepción de E/S
        }
    }

    // Método para cerrar la conexión con el servidor
    public void cerrarConexion() {
        try {
            bufferDeEntrada.close(); // Cierra el flujo de entrada
            bufferDeSalida.close(); // Cierra el flujo de salida
            socket.close(); // Cierra el socket
            mostrarTexto("Conexión terminada"); // Muestra un mensaje de conexión terminada
        } catch (IOException e) {
            mostrarTexto("IOException on cerrarConexion()"); // Muestra un mensaje de error si ocurre una excepción de E/S
        } finally {
            System.exit(0); // Sale del programa
        }
    }

    // Método para ejecutar la conexión con el servidor en un hilo separado
    public void ejecutarConexion(String ip, int puerto) {
        Thread hilo = new Thread(() -> {
            try {
                levantarConexion(ip, puerto); // Establece la conexión con el servidor
                abrirFlujos(); // Abre los flujos de entrada y salida de datos
                recibirDatos(); // Recibe datos del servidor
            } finally {
                cerrarConexion(); // Cierra la conexión con el servidor
            }
        });
        hilo.start(); // Inicia el hilo
    }

    // Método para recibir datos del servidor
    public void recibirDatos() {
        String st = ""; // Variable para almacenar los datos recibidos
        try {
            do {
                st = bufferDeEntrada.readUTF(); // Lee datos del flujo de entrada
                mostrarTexto("\n[Servidor] => " + st); // Muestra los datos recibidos del servidor
                System.out.print("\n[Usted] => "); // Muestra un indicador para que el usuario escriba
            } while (!st.equals(COMANDO_TERMINACION)); // Continúa recibiendo datos hasta que se recibe el comando de terminación
        } catch (IOException e) {
            // Maneja cualquier excepción de E/S, pero no muestra ningún mensaje para evitar interrupciones continuas en la recepción de datos
        }
    }

    // Método para que el usuario escriba datos y los envíe al servidor
    public void escribirDatos() {
        String entrada = ""; // Variable para almacenar la entrada del usuario
        while (true) {
            System.out.print("[Usted] => "); // Muestra un indicador para que el usuario escriba
            entrada = teclado.nextLine(); // Lee la entrada del usuario desde el teclado
            if (entrada.length() > 0) // Verifica si se ha ingresado algún texto
                enviar(entrada); // Envía la entrada del usuario al servidor
        }
    }

    // Método principal del programa
    public static void main(String[] argumentos) {
        Client cliente = new Client(); // Crea una instancia del cliente
        Scanner escaner = new Scanner(System.in); // Crea un escáner para leer desde el teclado
        mostrarTexto("Ingresa la IP: [localhost por defecto] "); // Solicita al usuario que ingrese la dirección IP del servidor
        String ip = escaner.nextLine(); // Lee la dirección IP ingresada por el usuario
        if (ip.length() <= 0) ip = "localhost"; // Utiliza "localhost" como dirección IP predeterminada si no se proporciona ninguna

        mostrarTexto("Puerto: [5050 por defecto] "); // Solicita al usuario que ingrese el puerto del servidor
        String puerto = escaner.nextLine(); // Lee el puerto ingresado por el usuario
        if (puerto.length() <= 0) puerto = "5050"; // Utiliza "5050" como puerto predeterminado si no se proporciona ninguno
        cliente.ejecutarConexion(ip, Integer.parseInt(puerto)); // Ejecuta la conexión con el servidor
        cliente.escribirDatos(); // Permite al usuario escribir y enviar datos al servidor
    }
}
