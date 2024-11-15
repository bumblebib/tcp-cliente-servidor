import socket
import threading
import os

HEADER = 1024
PORT = 5050
SERVER = socket.gethostbyname(socket.gethostname())
ADDR = (SERVER, PORT)
FORMAT = 'utf-8'
STORAGE_DIR = "server_files"
DISCONNECT_MESSAGE = "3"

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(ADDR)

# Cria o diretório de armazenamento se não existir
if not os.path.exists(STORAGE_DIR):
    os.makedirs(STORAGE_DIR)

def handle_client(conn, addr):
    print(f"[NOVA CONEXÃO] {addr} conectado.")
    connected = True
    while connected:
        operation = conn.recv(HEADER).decode(FORMAT)
        
        if operation == "1":  # Recebe Arquivo do Cliente
            filename = conn.recv(HEADER).decode(FORMAT)

            if not filename:
                print("[SERVIDOR] ERRO: Nome do arquivo não recebido.")
                continue

            # Recebe o tamanho do arquivo
            file_size = conn.recv(HEADER).decode(FORMAT)
            if not file_size:
                print("[SERVIDOR] ERRO Tamanho do arquivo não recebido.")
                continue

            file_size = int(file_size)

            with open(os.path.join(STORAGE_DIR, filename), "wb") as file:
                bytes_received = 0
                while bytes_received < file_size:
                    data = conn.recv(HEADER)
                    file.write(data)
                    bytes_received += len(data)
            print(f"[ARQUIVO RECEBIDO] {filename} de {addr}")
            conn.send("[SERVIDOR] Arquivo recebido com sucesso.".encode(FORMAT))
            
        elif operation == "2":  # Envia arquivo para o Cliente
            filename = conn.recv(HEADER).decode(FORMAT)
            filepath = os.path.join(STORAGE_DIR, filename)
            if os.path.exists(filepath):
                file_size = os.path.getsize(filepath)
                conn.send(str(file_size).encode(FORMAT))
                reply = conn.recv(HEADER).decode(FORMAT)
                if reply != "OK":
                    print("[SERVIDOR] ERRO: Cliente não confirmou recebimento.")
                    continue
                with open(filepath, "rb") as file:
                    while (data := file.read(HEADER)):
                        conn.send(data)
                print(f"[ARQUIVO ENVIADO] {filename} para {addr}")
                conn.send("[SERVIDOR] Arquivo enviado com sucesso.".encode(FORMAT))
            else:
                conn.send("[SERVIDOR] ERRO: Arquivo não encontrado.".encode(FORMAT))
        
        elif operation == DISCONNECT_MESSAGE:
            connected = False
            print(f"[DESCONECTADO] {addr} desconectado.")
            conn.send("[SERVIDOR] Desconectado com sucesso.".encode(FORMAT))
    conn.close()


def start():
    server.listen()
    print(f"[SERVIDOR LIGADO] Servidor em {SERVER}")
    
    def shutdown_server():
        while True:
            command = input()
            if command.lower() == "desligar":
                print("[DESLIGANDO] O servidor está desligando...")
                server.close()
                os._exit(0)
    
    shutdown_thread = threading.Thread(target=shutdown_server)
    shutdown_thread.start()
    
    while True:
        try:
            conn, addr = server.accept()
            thread = threading.Thread(target=handle_client, args=(conn, addr))
            thread.start()
            print(f"[CONEXÕES ATIVAS] {threading.active_count() - 1}")
        except OSError:
            break

print("[INICIANDO] O servidor está iniciando...")
start()
