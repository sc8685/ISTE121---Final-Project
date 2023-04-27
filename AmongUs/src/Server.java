import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {

    // attributes
    private static final int PORT = 12345;
    private static TextArea chatArea;
    private static ListView<String> clientListView;
    private static ObservableList<String> clientList = FXCollections.observableArrayList();
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub

        // gui components
        chatArea = new TextArea();
        chatArea.setEditable(false);
        clientListView = new ListView<>(clientList);

        // start server button
        Button start = new Button("Start Server");
        start.setOnAction(e -> {
            startServer();
        });

        // stop server btn
        Button stop = new Button("Stop Server");
        stop.setOnAction(e -> {
            stopServer();
        });

        // gridpane
        GridPane centerPane = new GridPane();
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        centerPane.add(new Label("Server Status:"), 0, 0);
        centerPane.add(chatArea, 0, 1);
        centerPane.add(new Label("Client IP:"), 1, 0);
        centerPane.add(clientListView, 1, 1);

        // vbox for btns
        VBox btnBox = new VBox(8, start, stop);
        btnBox.setAlignment(Pos.CENTER);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        mainLayout.setCenter(centerPane);
        mainLayout.setBottom(btnBox);

        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    // start server method
    public void startServer() {
        Thread serverThread = new Thread(() -> {

            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                chatArea.appendText("Server started on port: " + PORT + "\n");

                // accept connections
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String clientName = in.readLine();
                    Platform.runLater(() -> {
                        chatArea.appendText("New Client Connected: " + clientName + "\n");
                    });
                    ClientHandler clientHandler = new ClientHandler(socket, clientName);
                    clients.add(clientHandler);
                    Platform.runLater(() -> {
                        clientList.add(socket.getInetAddress().getHostAddress());

                    });
                    clientHandler.start();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        });
        serverThread.setDaemon(true);
        serverThread.start();

    }

    // stop server method
    public void stopServer() {

        try {
            for (ClientHandler client : clients) {
                client.socket.close();
            }
            clients.clear();
            chatArea.appendText("Server stopped\n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // client handler class
    public class ClientHandler extends Thread {
        private Socket socket;
        private String name;

        public ClientHandler(Socket socket, String name) {
            this.socket = socket;
            this.name = name;
        }

        public Object getSocketAddress() {
            return socket;
        }
    }

}
