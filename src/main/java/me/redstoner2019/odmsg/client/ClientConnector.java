package me.redstoner2019.odmsg.client;

import me.redstoner2019.client.ODClient;
import me.redstoner2019.events.ConnectionFailedEvent;
import me.redstoner2019.events.ConnectionSuccessEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

public class ClientConnector extends ODClient {
    private static String ip = "localhost";
    private static int port = 8005;

    public ClientConnector() {

    }
}
