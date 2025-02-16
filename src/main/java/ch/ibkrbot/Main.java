package ch.ibkrbot;

public class Main {
    public static void main(String[] args) {
        Constants.ACCOUNT = args[0];
        IBKRClientBot ibkrClientBot = new IBKRClientBot();
        ibkrClientBot.start();
    }
}