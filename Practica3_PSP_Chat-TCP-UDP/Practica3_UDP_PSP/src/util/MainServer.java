package util;

import datos.Usuario;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainServer {
    public static final ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    public static final ArrayList<String> historialMensajes = new ArrayList<>();
    public static final Set<SocketAddress> clientesConectados = new HashSet<>();
    public static final int puerto = 6001;

    public static void main(String[] args) {

        listaUsuarios.add(new Usuario(1, "Daniel", "1234"));
        listaUsuarios.add(new Usuario(2, "Maria", "4321"));
        listaUsuarios.add(new Usuario(3, "Luisa", "1423"));
        listaUsuarios.add(new Usuario(4, "Kike", "1213"));
        listaUsuarios.add(new Usuario(5, "Joel", "0000"));

        try (DatagramSocket socket = new DatagramSocket(puerto)) {
            System.out.println("-- Servidor UDP escuchando en el puerto: " + puerto+" --");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                new Thread(() -> manejarCliente(packet, socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarCliente(DatagramPacket packet, DatagramSocket socket) {
        try {
            String mensaje = new String(packet.getData(), 0, packet.getLength());
            String[] datos = mensaje.split(";");

            if (datos.length == 2) {
                String nombre = datos[0];
                String password = datos[1];
                boolean autenticado = false;

                for (Usuario u : listaUsuarios) {
                    if (u.getNombre().equalsIgnoreCase(nombre) && u.getPassword().equals(password)) {
                        autenticado = true;
                        break;
                    }
                }

                String respuesta = autenticado ? "200;-- Usuario conectado --" : "300;-- Usuario incorrecto --";
                enviarMensaje(respuesta, packet.getAddress(), packet.getPort(), socket);

                if (autenticado) {
                    synchronized (clientesConectados) {
                        clientesConectados.add(packet.getSocketAddress());
                    }
                    synchronized (historialMensajes) {
                        for (String msg : historialMensajes) {
                            enviarMensaje(msg, packet.getAddress(), packet.getPort(), socket);
                        }
                    }
                }
            } else {
                synchronized (historialMensajes) {
                    historialMensajes.add(mensaje);
                }
                System.out.println("Enviando mensaje a todos: " + mensaje);
                enviarMensajeATodos(mensaje, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensaje(String mensaje, InetAddress address, int port, DatagramSocket socket) {
        try {
            byte[] buffer = mensaje.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensajeATodos(String mensaje, DatagramSocket socket) {
        synchronized (clientesConectados) {
            for (SocketAddress cliente : clientesConectados) {
                try {
                    byte[] buffer = mensaje.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, cliente);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}