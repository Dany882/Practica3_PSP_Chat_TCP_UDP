package util;

import datos.Usuario;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainServer {

    public static final ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    public static final ArrayList<Usuario> usuariosConectados = new ArrayList<>();
    public static final ArrayList<String> historialMensajes = new ArrayList<>();
    public static final ArrayList<DataOutputStream> salidasClientes = new ArrayList<>();

    public static void main(String[] args) {
        int puerto = 6001;

        listaUsuarios.add(new Usuario(1, "Daniel", "1234"));
        listaUsuarios.add(new Usuario(2, "Maria", "4321"));
        listaUsuarios.add(new Usuario(3, "Luisa", "1423"));
        listaUsuarios.add(new Usuario(4, "Kike", "1213"));
        listaUsuarios.add(new Usuario(5, "Joel", "0000"));


        try {
            ServerSocket servidor = new ServerSocket(puerto);
            System.out.println("Escuchando en el puerto: "+ puerto);

            while(true) {
                Socket cliente = servidor.accept();

                System.out.println("Atendiendo peticion cliente desde: "+ cliente.getInetAddress());

                new Thread(() -> manejarCliente(cliente)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void manejarCliente(Socket cliente) {
        try {

            //---------------SALIDA---------------
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            DataOutputStream salidaData = new DataOutputStream(cliente.getOutputStream());

            //---------------ENTRADA---------------
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            DataInputStream entradaData = new DataInputStream(cliente.getInputStream());


            synchronized (salidasClientes) {
                salidasClientes.add(salidaData);
            }

            Usuario usuario = (Usuario) entrada.readObject();

            Boolean existeUsuario = false;
            String mensajeServidor = "-- El usuario ingresado no existe o no es correcto --";
            int codigo = 300;

            synchronized (listaUsuarios) {
                for(Usuario u:listaUsuarios) {
                    if(u.getNombre().equalsIgnoreCase(usuario.getNombre()) && u.getPassword().equals(usuario.getPassword())) {

                        existeUsuario = true;
                        codigo = 200;
                        mensajeServidor = "-- Usuario conectado --";

                        synchronized (usuariosConectados) {
                            usuariosConectados.add(usuario);
                        }
                        break;
                    }
                }
            }



            salidaData.writeInt(codigo);
            salidaData.writeUTF(mensajeServidor);
            salidaData.flush();

            if(existeUsuario) {

                synchronized (historialMensajes) {
                    salida.writeObject(new ArrayList<>(historialMensajes));
                    salida.flush();
                }

                while(true) {
                    String mensaje = entradaData.readUTF();
                    String mensajeFormato = usuario.getNombre() +": "+ mensaje;

                    synchronized (historialMensajes) {
                        historialMensajes.add(mensajeFormato);
                    }

                    System.out.println("Enviando mensaje a todos: "+ mensaje);
                    enviarMensajeATodos(mensajeFormato);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enviarMensajeATodos(String mensaje) {
        synchronized (salidasClientes) {
            for(DataOutputStream salida : salidasClientes) {
                try {
                    salida.writeUTF(mensaje);
                    salida.flush();
                } catch (IOException e) {
                    System.out.println("-- ERROR enviando mensaje a un cliente --");
                }
            }
        }
    }
}


