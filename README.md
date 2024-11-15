# Sistema de Transferência de Arquivos Cliente-Servidor

Este projeto implementa um sistema cliente-servidor para transferência de arquivos utilizando **sockets**. Ele permite que o cliente envie arquivos para o servidor, baixe arquivos armazenados no servidor e encerre a conexão.

## Estrutura do Projeto

O sistema é composto por dois programas:

- **Servidor (`server.py`)**: Gerencia conexões e permite armazenar e enviar arquivos.
- **Cliente (`Client.java`)**: Permite interação com o servidor para realizar operações como upload, download e desconexão.

## Requisitos

- **Servidor**: Requer **Python 3.x** instalado no ambiente.
- **Cliente**: Requer **Java JDK 8 ou superior** para compilação e execução.
- Ambos os programas devem ser executados em máquinas que suportem comunicação via sockets.

## Funcionalidades

### Cliente
1. **Enviar arquivo para o servidor (operação `1`)**:
   - O cliente solicita o envio de um arquivo.
   - Se o arquivo existir na pasta local do cliente (`client_files`), ele é enviado ao servidor em blocos.

2. **Baixar arquivo do servidor (operação `2`)**:
   - O cliente informa o nome do arquivo desejado.
   - Se o arquivo existir no servidor, ele é enviado ao cliente e salvo na pasta `client_files`.

3. **Desconectar (operação `3`)**:
   - Encerra a conexão com o servidor.

### Servidor
1. **Receber arquivo do cliente**:
   - O servidor salva o arquivo recebido na pasta `server_files`.

2. **Enviar arquivo ao cliente**:
   - O servidor verifica se o arquivo solicitado existe e envia-o ao cliente em blocos.

3. **Encerrar conexão**:
   - Finaliza a comunicação com o cliente.

4. **Encerrar o servidor**:
   - Através de um comando de entrada (`desligar`), o servidor pode ser encerrado manualmente.

## Instruções de Uso

### Servidor

1. Certifique-se de ter o Python instalado.
2. Execute o servidor:
   ```bash
   python3 server.py
   ```
3. O servidor estará disponível no IP e porta configurados (por padrão, `127.0.1.1:5050`).

### Cliente

1. Compile o programa cliente:
   ```bash
   javac Client.java
   ```
2. Execute o cliente:
   ```bash
   java Client
   ```
3. Siga as instruções interativas para realizar as operações desejadas. 

## Estrutura de Diretórios

- **Servidor:** arquivos armazenados na pasta `server_files`.
    > O servidor possui o arquivo `index.html` para ser enviado. 
- **Cliente:** arquivos locais armazenados na pasta `client_files`.
    > O cliente possui os arquivos `joke.jpg` e `texto.txt` para serem enviados. 


## :no_entry: Problemas Conhecidos
**Loop ao enviar arquivos após recebê-lo:**
- Em algumas situações, ao tentar enviar um arquivo imediatamente após realizar o download de outro, o cliente entra em um loop infinito. 
- Esse comportamento pode ser causado por erros não localizados na sincronização entre o cliente e o servidor. 


## :warning: Mensagens de Erro
### Servidor:
- `Nome do arquivo não recebido`: Nome do arquivo não enviado pelo cliente. 
- `Tamanho do arquivo não recebido`: Falha ao obter o tamanho do arquivo. 
- `Arquivo não encontrado:` Arquivo solicitado pelo cliente não existe. 
### Cliente:
- `Arquivo não encontrado`: Arquivo solicitado para upload não está na pasta local. 
- `Erro do servidor`: Erro retornado pelo servidor durante uma operação. 

## Limitações Conhecidas:
- **Transferências Simultâneas:** O servidor atende múltiplas conexões, mas não prioriza filas ou oferece controle avançado de concorrência. 
- **Manuseio de Arquivos Grandes:** O tamanho do cabeçalho (`HEADER = 1024`) pode limitar a eficiência no envio de grandes arquivos. 
