package de.miltschek.tracker;

public class TransferRequest {
    private String address;
    private int port;
    private String filePath;

    public TransferRequest(String address, int port, String filePath) {
        this.address = address;
        this.port = port;
        this.filePath = filePath;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getFilePath() {
        return filePath;
    }
}
