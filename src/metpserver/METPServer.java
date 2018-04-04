/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metpserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Stefano Fiordi
 */
public class METPServer {

    public static void writeDigests(long[] digests, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
        for (long l : digests) {
            writer.write(String.valueOf(l));
            writer.newLine();
        }

    }

    public static long[] readDigests(String filename, int chunks) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        long[] digests = new long[chunks];
        int i = 0;
        while ((line = reader.readLine()) != null) {
            digests[i] = Long.parseLong(line);
            i++;
        }
        reader.close();

        return digests;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        File newVersion = new File("E:/vdis/FSV 2.vdi");
        File oldVersion = new File("E:/vdis/FSV.vdi");
        final int ChunkSize = 1024 * 1024; // 1 MB
        long newChunks, oldChunks;
        long[] oldDigests, newDigests;

        if (newVersion.length() % ChunkSize == 0) {
            newChunks = newVersion.length() / ChunkSize;
        } else {
            newChunks = newVersion.length() / ChunkSize + 1;
        }
        if (oldVersion.length() % ChunkSize == 0) {
            oldChunks = oldVersion.length() / ChunkSize;
        } else {
            oldChunks = oldVersion.length() / ChunkSize + 1;
        }

        if (!new File("new.txt").exists() && !new File("old.txt").exists()) {
            FileCRCIndex newfCRCi = new FileCRCIndex(newVersion.getAbsolutePath(), ChunkSize, newChunks, newVersion.length());
            FileCRCIndex oldfCRCi = new FileCRCIndex(oldVersion.getAbsolutePath(), ChunkSize, oldChunks, oldVersion.length());

            System.out.println("Old file digests calculation...");
            oldDigests = oldfCRCi.calcDigests();
            System.out.println("New file digests calculation...");
            newDigests = newfCRCi.calcDigests();

            System.out.println("Writing old.txt...");
            writeDigests(oldDigests, "old.txt");
            System.out.println("Success\nWriting new.txt...");
            writeDigests(newDigests, "new.txt");
            System.out.println("Success");
        } else if (!new File("new.txt").exists() && new File("old.txt").exists()) {
            oldDigests = readDigests("old.txt", (int) oldChunks);
            FileCRCIndex newfCRCi = new FileCRCIndex(newVersion.getAbsolutePath(), ChunkSize, newChunks, newVersion.length());
            System.out.println("New file digests calculation...");
            newDigests = newfCRCi.calcDigests();
            System.out.println("Writing new.txt...");
            writeDigests(newDigests, "new.txt");
            System.out.println("Success");
        } else if (new File("new.txt").exists() && !new File("old.txt").exists()) {
            newDigests = readDigests("new.txt", (int) newChunks);
            FileCRCIndex oldfCRCi = new FileCRCIndex(oldVersion.getAbsolutePath(), ChunkSize, oldChunks, oldVersion.length());
            System.out.println("Old file digests calculation...");
            oldDigests = oldfCRCi.calcDigests();
            System.out.println("Writing old.txt...");
            writeDigests(oldDigests, "old.txt");
            System.out.println("Success");
        } else {
            oldDigests = readDigests("old.txt", (int) oldChunks);
            newDigests = readDigests("new.txt", (int) newChunks);
        }

        int equals = 0;
        for (int i = 0; i < newChunks; i++) {
            for (int j = 0; j < oldChunks; j++) {
                if (newDigests[i] == oldDigests[j]) {
                    equals++;
                    break;
                }
            }
        }

        System.out.println("\nEquals: " + equals);
    }

}
