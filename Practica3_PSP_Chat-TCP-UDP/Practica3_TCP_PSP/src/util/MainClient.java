package util;

import datos.Usuario;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        int puerto = 6001;
        Scanner sc = new Scanner(System.in);

        try {

            Socket sCliente = new Socket("localhost", puerto);
            System.out.println("Conectado");

            //---------------SALIDA---------------
            ObjectOutputStream salida = new ObjectOutputStream(sCliente.getOutputStream());
            DataOutputStream salidaData = new DataOutputStream(sCliente.getOutputStream());

            //---------------ENTRADA---------------
            ObjectInputStream entrada = new ObjectInputStream(sCliente.getInputStream());
            DataInputStream entradaData = new DataInputStream(sCliente.getInputStream());


            System.out.println("Ingresa el nombre:");
            String nombre = sc.next();
            System.out.println("Ingresa la contraseña:");
            String password = sc.next();
            Usuario usuario = new Usuario(0, nombre, password);

            salida.writeObject(usuario);
            salida.flush();


            int codigo = entradaData.readInt();
            String mensajeServidor = entradaData.readUTF();
            System.out.println(mensajeServidor);

            if(codigo == 200) {

                List<String> historialMensajes =(List<String>) entrada.readObject();

                System.out.println("-- Historial mensajes --");

                for(String mensajeHistorial : historialMensajes) {
                    System.out.println(mensajeHistorial);
                }

                new Thread(() -> {
                   try {
                       while (true) {
                           String mensaje = entradaData.readUTF();
                           System.out.println(mensaje);
                       }
                   } catch (IOException e) {
                       System.out.println("-- DESCONECTADO del servidor");
                   }
                }).start();

                while(true) {
                    String mensaje = sc.nextLine();
                    salidaData.writeUTF(mensaje);
                    salidaData.flush();
                }
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}