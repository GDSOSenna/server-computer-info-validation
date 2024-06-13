package Server;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    private final Socket clientSocket;

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            // coleta a informação do sistema
            String os = System.getProperty("os.name");
            String processorModel = getProcessorModel();
            String ipAddress = clientSocket.getInetAddress().getHostAddress();

            // valida o espaço livre nas unidades
            File[] roots = File.listRoots();
            StringBuilder diskSpaceInfo = new StringBuilder();
            for (File root : roots) {
                long freeSpace = root.getFreeSpace();
                long totalSpace = root.getTotalSpace();
                String disk = root.getAbsolutePath();
                diskSpaceInfo.append(String.format("Drive: %s, Total: %d MB, Free: %d MB%n",
                        disk, totalSpace / (1024 * 1024), freeSpace / (1024 * 1024)));
            }

            // faz o envio das informações para o client
            out.println("Sistema Operacional: " + os);
            out.println("Modelo do Processador: " + processorModel);
            out.println("Endereço IP: " + ipAddress);
            out.println("Espaço de armazenamento livre:");
            out.println(diskSpaceInfo.toString());

            // fechar socket do cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProcessorModel() {
        String model = "Unknown";
        try {
            // executa o comando wmic cpu get name para obter o nome do processador
            Process process = Runtime.getRuntime().exec("wmic cpu get name");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                model = output.toString().trim(); // remove espaço em branco
            }
            process.waitFor(); // aguarda finalizar a requisição
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return model;
    }
}