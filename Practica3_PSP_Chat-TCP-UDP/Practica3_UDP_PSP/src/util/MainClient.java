package util;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MainClient {
    public static final String host = "localhost";
    public static final int puerto = 6001;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(host);

            System.out.println("Ingresa el nombre:");
            String nombre = sc.next();
            System.out.println("Ingresa la contraseña:");
            String password = sc.next();

            String credenciales = nombre + ";" + password;
            enviarMensaje(credenciales, serverAddress, socket);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            new Thread(() -> recibirMensajes(socket)).start();

            System.out.println("Empieza a escribir");

            while (true) {
                String mensaje = sc.nextLine();
                if (!mensaje.isEmpty()) {
                    enviarMensaje(nombre + ": " + mensaje, serverAddress, socket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensaje(String mensaje, InetAddress address, DatagramSocket socket) {
        try {
            byte[] buffer = mensaje.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, puerto);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void recibirMensajes(DatagramSocket socket) {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensaje = new String(packet.getData(), 0, packet.getLength());
                System.out.println(mensaje);
            }
        } catch (IOException e) {
            System.out.println("-- DESCONECTADO del servidor --");
        }
    }
}