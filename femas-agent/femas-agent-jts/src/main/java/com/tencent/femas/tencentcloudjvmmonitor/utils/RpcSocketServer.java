/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.tencentcloudjvmmonitor.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class RpcSocketServer implements RpcServer {
    private static final Logger LOGGER = Logger.getLogger(RpcSocketServer.class);
    private static volatile RpcSocketServer socketServer;
    private ServerSocket svcSocket;
    private String port;

    public static RpcSocketServer getInstance(String port, Instrumentation inst) {
        if (socketServer == null) {
            synchronized (RpcSocketServer.class) {
                if (socketServer == null) {
                    socketServer = new RpcSocketServer(port);
                }
            }
        }
        return socketServer;
    }

    private RpcSocketServer(String port) {
        this.port = port;
    }

    public void start(String s) throws IOException {
        svcSocket = new ServerSocket(Integer.parseInt(port));
        Runnable rpcServerTask = new Runnable() {
            public void run() {
                try {
                    LOGGER.info("RPC Server waiting for connection...");
                    while (true) {
                        Socket client = svcSocket.accept();
                        new HandlerThread(client);
                    }
                } catch (SocketException e) {
                    LOGGER.warn("RpcSocketServer closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread;
        serverThread = new Thread(rpcServerTask, "RPC Server Thread");
        serverThread.start();
    }

    public void stop() {
        if (!svcSocket.isClosed()) {
            try {
                svcSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class HandlerThread implements Runnable {
        private Socket socket;

        HandlerThread(Socket socket) {
            this.socket = socket;
            new Thread(this).start();
        }

        public void run() {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String command = input.readUTF();
                LOGGER.debug("Get command " + command + " from socket.");
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String response = processCommand(command);
                out.write(response.getBytes());
                out.close();
                input.close();
            } catch (IOException e) {
                LOGGER.error("Error Reading Data from Socket");
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        socket = null;
                        System.out.println("Server Wrong at finally" + e.getMessage());
                    }
                }
            }
        }

        private String processCommand(String command) {
            // TODO: impl me.

            return command;
        }
    }


}
