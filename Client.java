import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.1.1"; 
    private static final int SERVER_PORT = 5050;
    private static final int HEADER = 1024;
    private static final String DISCONNECT_MESSAGE = "3";
    private static final String FORMAT = "utf-8";
    private static final String CLIENT_FILES_DIR = "client_files";

    public static void main(String[] args) {
        File clientFilesDir = new File(CLIENT_FILES_DIR);
        if (!clientFilesDir.exists()) {
            clientFilesDir.mkdir();
        }

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor.");

            while (true) {
                System.out.println("Escolha uma operação: \n1 (Enviar arquivo), \n2 (Receber arquivo), \n3 (Desconectar)");
                String operation = scanner.nextLine();

                if (operation.equals("1")) {
                    // Enviar arquivo
                    System.out.print("Digite o nome do arquivo para enviar: ");
                    String fileName = scanner.nextLine();
                    File file = new File(clientFilesDir, fileName);
                    if (file.exists()) {
                        // Envia a operação para o servidor
                        out.write(operation.getBytes(FORMAT));
                        out.flush();

                        // Envia o nome e o tamanho do arquivo como string
                        out.write(file.getName().getBytes(FORMAT));
                        out.flush();

                        out.write(String.valueOf(file.length()).getBytes(FORMAT));
                        out.flush();

                        // Envia o conteúdo do arquivo em blocos
                        try (FileInputStream fileIn = new FileInputStream(file)) {
                            byte[] buffer = new byte[HEADER];
                            int bytesRead;
                            while ((bytesRead = fileIn.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                        out.flush();
                        System.out.println("[CLIENTE] Arquivo enviado com sucesso.");

                        // Lê a mensagem de resposta do servidor
                        byte[] responseBuffer = new byte[HEADER];
                        int responseBytes = in.read(responseBuffer);
                        String responseMessage = new String(responseBuffer, 0, responseBytes, FORMAT).trim();
                        System.out.println(responseMessage);
                    } else {
                        System.out.println("[CLIENTE] Arquivo não encontrado.");
                    }
                } else if (operation.equals("2")) {
                    // Envia a operação para o servidor
                    out.write(operation.getBytes(FORMAT));
                    // Receber arquivo
                    System.out.print("Digite o nome do arquivo para receber: ");
                    String fileName = scanner.nextLine();
                
                    // Envia o nome do arquivo para o servidor
                    out.write(fileName.getBytes(FORMAT));
                    out.flush();
                
                    // Lê a resposta do servidor: tamanho do arquivo ou mensagem de erro
                    byte[] responseBuffer = new byte[HEADER];
                    int responseBytes = in.read(responseBuffer);

                    if(responseBytes < 0) {
                        out.write("ERRO".getBytes(FORMAT));
                    }else{
                        out.write("OK".getBytes(FORMAT));
                    }
                    out.flush();

                    String fileSizeStr = new String(responseBuffer, 0, responseBytes, FORMAT).trim();
                    
                    // Verifica se a resposta contém um erro
                    if (fileSizeStr.startsWith("[SERVIDOR] ERRO:")) {
                        System.out.println("Erro do servidor: " + fileSizeStr);
                    } else {
                        long fileSize = Long.parseLong(fileSizeStr);
                
                        File file = new File(clientFilesDir, fileName);
                        try (FileOutputStream fileOut = new FileOutputStream(file)) {
                            byte[] buffer = new byte[HEADER];
                            int bytesRead;
                            long totalBytesRead = 0;
                
                            // Recebe o arquivo em blocos e escreve no arquivo local
                            while (totalBytesRead < fileSize) {
                                bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                                if (bytesRead == -1) break; // Encerrar caso não haja mais dados
                                fileOut.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                            }
                        }
                        out.flush();

                        responseBuffer = new byte[HEADER];
                        responseBytes = in.read(responseBuffer);
                        String responseMessage = new String(responseBuffer, 0, responseBytes, FORMAT).trim();
                        System.out.println(responseMessage);
                
                        System.out.println("[CLIENTE] Arquivo '" + fileName + "' recebido com sucesso.");
                    }
                }
                // Desconectar
                 else if (operation.equals(DISCONNECT_MESSAGE)) {
                    System.out.println("[CLIENTE] Desconectando...");
                    out.write(DISCONNECT_MESSAGE.getBytes(FORMAT));
                    byte[] responseBuffer = new byte[HEADER];
                    int responseBytes = in.read(responseBuffer);
                    String responseMessage = new String(responseBuffer, 0, responseBytes, FORMAT).trim();
                    System.out.println(responseMessage);
                    out.flush();
                    socket.close();
                    break;
                } else {
                    System.out.println("Operação inválida.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
