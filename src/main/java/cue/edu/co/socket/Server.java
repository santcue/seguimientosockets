package cue.edu.co.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private Socket socket; // Socket para la conexión con el cliente
    private DataInputStream bufferDeEntrada = null; // Flujo de entrada de datos desde el cliente
    private DataOutputStream bufferDeSalida = null; // Flujo de salida de datos hacia el cliente
    Scanner escaner = new Scanner(System.in); // Escáner para leer desde el teclado
    final String COMANDO_TERMINACION = "salir()"; // Comando para terminar la conexión

    // Método para levantar la conexión con el cliente en el puerto especificado
    public void levantarConexion(int puerto) {
        try {
            ServerSocket serverSocket = new ServerSocket(puerto); // Crea un ServerSocket en el puerto especificado
            mostrarTexto("Esperando conexión entrante en el puerto " + String.valueOf(puerto) + "..."); // Muestra un mensaje de espera
            socket = serverSocket.accept(); // Acepta la conexión entrante del cliente
            mostrarTexto("Conexión establecida con: " + socket.getInetAddress().getHostName() + "\n\n\n"); // Muestra un mensaje de conexión establecida
        } catch (Exception e) {
            mostrarTexto("Error en levantarConexion(): " + e.getMessage()); // Muestra un mensaje de error si ocurre alguna excepción
            System.exit(0); // Sale del programa
        }
    }

    // Método para abrir los flujos de entrada y salida de datos
    public void flujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream()); // Crea un flujo de entrada de datos desde el cliente
            bufferDeSalida = new DataOutputStream(socket.getOutputStream()); // Crea un flujo de salida de datos hacia el cliente
            bufferDeSalida.flush(); // Limpia el flujo de salida
        } catch (IOException e) {
            mostrarTexto("Error en la apertura de flujos"); // Muestra un mensaje de error si ocurre una excepción de E/S
        }
    }

    // Método para recibir datos del cliente
    public void recibirDatos() {
        String st = ""; // Variable para almacenar los datos recibidos
        try {
            do {
                st = bufferDeEntrada.readUTF(); // Lee datos del flujo de entrada
                mostrarTexto("\n[Cliente] => " + st); // Muestra los datos recibidos del cliente
                System.out.print("\n[Usted] => "); // Muestra un indicador para que el servidor escriba
            } while (!st.equals(COMANDO_TERMINACION)); // Continúa recibiendo datos hasta que se recibe el comando de terminación
        } catch (IOException e) {
            cerrarConexion(); // Cierra la conexión con el cliente si hay un error de E/S
        }
    }

    // Método para enviar un mensaje al cliente
    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s); // Escribe el mensaje en el flujo de salida
            bufferDeSalida.flush(); // Limpia el flujo de salida
        } catch (IOException e) {
            mostrarTexto("Error en enviar(): " + e.getMessage()); // Muestra un mensaje de error si ocurre una excepción de E/S
        }
    }

    // Método auxiliar para imprimir texto en la consola
    public static void mostrarTexto(String s) {
        System.out.print(s);
    }

    // Método para que el servidor escriba datos y los envíe al cliente
    public void escribirDatos() {
        while (true) {
            System.out.print("[Usted] => "); // Muestra un indicador para que el servidor escriba
            enviar(escaner.nextLine()); // Lee la entrada del servidor desde el teclado y la envía al cliente
        }
    }

    // Método para cerrar la conexión con el cliente
    public void cerrarConexion() {
        try {
            bufferDeEntrada.close(); // Cierra el flujo de entrada
            bufferDeSalida.close(); // Cierra el flujo de salida
            socket.close(); // Cierra el socket
        } catch (IOException e) {
            mostrarTexto("Excepción en cerrarConexion(): " + e.getMessage()); // Muestra un mensaje de error si ocurre una excepción de E/S
        } finally {
            mostrarTexto("Conversación finalizada...."); // Muestra un mensaje de conversación finalizada
            System.exit(0); // Sale del programa
        }
    }

    // Método para ejecutar la conexión con el cliente en un hilo separado
    public void ejecutarConexion(int puerto) {
        Thread hilo = new Thread(() -> {
            while (true) {
                try {
                    levantarConexion(puerto); // Levanta la conexión con el cliente en el puerto especificado
                    flujos(); // Abre los flujos de entrada y salida de datos
                    recibirDatos(); // Recibe datos del cliente
                } finally {
                    cerrarConexion(); // Cierra la conexión con el cliente
                }
            }
        });
        hilo.start(); // Inicia el hilo
    }

    // Método principal del programa
    public static void main(String[] args) throws IOException {
        Server s = new Server(); // Crea una instancia del servidor
        Scanner sc = new Scanner(System.in); // Crea un escáner para leer desde el teclado
        mostrarTexto("Ingresa el puerto [5050 por defecto]: "); // Solicita al usuario que ingrese el puerto del servidor
        String puerto = sc.nextLine(); // Lee el puerto ingresado por el usuario
        if (puerto.length() <= 0) puerto = "5050"; // Utiliza "5050" como puerto predeterminado si no se proporciona ninguno
        s.ejecutarConexion(Integer.parseInt(puerto)); // Ejecuta la conexión con el cliente en el puerto especificado
        s.escribirDatos(); // Permite al servidor escribir y enviar datos al cliente
    }
}
