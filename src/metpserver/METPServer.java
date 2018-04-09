/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metpserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Stefano Fiordi
 */
public class METPServer {

    static byte[] createPacket(ByteBuffer buf) {
        buf.flip();
        byte[] packet = new byte[buf.remaining()];
        buf.get(packet);
        buf.clear();

        return packet;
    }

    public static void writeDigests(long[] digests, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
        for (long l : digests) {
            writer.write(Long.toString(l) + " ");
        }
        writer.close();
    }

    public static long[] readDigests(String filename, int chunks) throws IOException, NumberFormatException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        long[] digests = new long[chunks];
        line = reader.readLine();
        String[] pcs = line.split(" ");
        for (int i = 0; i < chunks; i++) {
            digests[i] = Long.parseLong(pcs[i]);
        }
        reader.close();

        return digests;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, MyExc, NumberFormatException {
        File newVersion = new File("tfiles/testo1.1.txt");
        File oldVersion = new File("tfiles/testo1.0.txt");
        FileChannel fNew = new FileInputStream(newVersion).getChannel(); // Canale di lettura del nuovo file
        FileChannel fOld = new FileInputStream(oldVersion).getChannel(); // Canale di lettura del vecchio file
        FileChannel fOutOld = FileChannel.open(oldVersion.toPath(), StandardOpenOption.WRITE); // Canale di scrittura sul vecchio file in append
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

        if (!new File("new.crc").exists() && !new File("old.crc").exists()) {
            FileCRCIndex newfCRCi = new FileCRCIndex(newVersion.getAbsolutePath(), ChunkSize, newChunks, newVersion.length());
            FileCRCIndex oldfCRCi = new FileCRCIndex(oldVersion.getAbsolutePath(), ChunkSize, oldChunks, oldVersion.length());

            System.out.println("Old file digests calculation...");
            oldDigests = oldfCRCi.calcDigests();
            System.out.println("New file digests calculation...");
            newDigests = newfCRCi.calcDigests();

            System.out.println("Writing old.crc...");
            writeDigests(oldDigests, "old.crc");
            System.out.println("Success\nWriting new.crc...");
            writeDigests(newDigests, "new.crc");
            System.out.println("Success");
        } else if (!new File("new.crc").exists() && new File("old.crc").exists()) {
            oldDigests = readDigests("old.crc", (int) oldChunks);
            FileCRCIndex newfCRCi = new FileCRCIndex(newVersion.getAbsolutePath(), ChunkSize, newChunks, newVersion.length());
            System.out.println("New file digests calculation...");
            newDigests = newfCRCi.calcDigests();
            System.out.println("Writing new.crc...");
            writeDigests(newDigests, "new.crc");
            System.out.println("Success");
        } else if (new File("new.crc").exists() && !new File("old.crc").exists()) {
            newDigests = readDigests("new.crc", (int) newChunks);
            FileCRCIndex oldfCRCi = new FileCRCIndex(oldVersion.getAbsolutePath(), ChunkSize, oldChunks, oldVersion.length());
            System.out.println("Old file digests calculation...");
            oldDigests = oldfCRCi.calcDigests();
            System.out.println("Writing old.crc...");
            writeDigests(oldDigests, "old.crc");
            System.out.println("Success");
        } else {
            oldDigests = readDigests("old.crc", (int) oldChunks);
            newDigests = readDigests("new.crc", (int) newChunks);
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

        IndexedArray iaNew = new IndexedArray(newDigests);
        AIndexedArray aiaOld = new AIndexedArray(oldDigests);
        for (int i = 0; i < oldChunks; i++) {
            for (int j = 0; j < newChunks; j++) {
                if (iaNew.sIndexes[j] == 0) {
                    if (aiaOld.array[i] == iaNew.array[j]) {
                        aiaOld.addIndex(i, j);
                        iaNew.sIndexes[j]++;
                    }
                } else if (i == j) {
                    if (aiaOld.array[i] == iaNew.array[j]) {
                        aiaOld.addIndex(i, j);
                    }
                }
            }
            if (aiaOld.sIndexes[i].length == 0) {
                aiaOld.addIndex(i, -1);
            }
        }

        for (int i = 0; i < aiaOld.sIndexes.length; i++) {
            System.out.println(aiaOld.sIndexes[i].length);
        }

        for (int i = 0; i < aiaOld.sIndexes.length; i++) {
            System.out.print("\nIndexes[" + i + "]: ");
            for (int j = 0; j < aiaOld.sIndexes[i].length; j++) {
                System.out.print(aiaOld.sIndexes[i][j] + " ");
            }
        }

        if (oldVersion.length() < newVersion.length()) {
            long[] newArray = new long[(int) newChunks];
            System.arraycopy(aiaOld.array, 0, newArray, 0, aiaOld.array.length);
            aiaOld.array = newArray;
            System.out.println("\nNew version is bigger");
            for (int i = (int) oldChunks; i < (int) newChunks; i++) {
                int j;
                ByteBuffer buf = ByteBuffer.allocate(ChunkSize);
                for (j = 0; j < aiaOld.sIndexes.length; j++) {
                    int index = aiaOld.searchColumnIndex(j, i);
                    if (index != -1) {
                        int len;
                        if ((len = fOld.read(buf, j * ChunkSize)) != -1) {
                            byte[] chunk = createPacket(buf);
                            fOutOld.write(ByteBuffer.wrap(chunk), i * ChunkSize);
                            System.out.println("Copied chunk " + j + " to " + i);
                            aiaOld.array[i] = aiaOld.array[j];
                            if (aiaOld.sIndexes[j].length == 1) {
                                aiaOld.sIndexes[j][0] = -1;
                            } else {
                                aiaOld.delIndex(j, index);
                            }
                            break;
                        }
                    }
                }
                if (j == aiaOld.sIndexes.length) {
                    int len;
                    if ((len = fNew.read(buf, i * ChunkSize)) != -1) {
                        byte[] chunk = createPacket(buf);
                        fOutOld.write(ByteBuffer.wrap(chunk), i * ChunkSize);
                        System.out.println("Copied chunk " + j + " from new version to " + i);
                        aiaOld.array[i] = iaNew.array[i];
                    }
                }
            }
        }
        long dBuf = 0;
        int[] dsIndexes = null;
        byte[] dChunk = null;
        int iterations;
        if (newChunks >= oldChunks) {
            iterations = aiaOld.sIndexes.length;
        } else {
            iterations = (int) newChunks;
        }
        System.out.println("\nFile handling started...");
        while (true) {
            int updated = 0, notUp = 0;
            ByteBuffer buf = ByteBuffer.allocate(ChunkSize);
            for (int i = 0; i < iterations; i++) {
                if (aiaOld.sIndexes[i][0] == -1) {
                    System.out.println("Processing chunk " + i);
                    int k = -1;
                    if (dsIndexes != null && dChunk != null) {
                        for (k = 0; k < dsIndexes.length; k++) {
                            if (dsIndexes[k] == i) {
                                fOutOld.write(ByteBuffer.wrap(dChunk), i * ChunkSize);
                                System.out.println("Copied buffer to " + i);
                                aiaOld.array[i] = dBuf;
                                aiaOld.sIndexes[i][0] = i;
                                if (dsIndexes.length > 1) {
                                    for (int z = k; z < dsIndexes.length - 1; z++) {
                                        dsIndexes[z] = dsIndexes[z + 1];
                                    }
                                } else {
                                    dsIndexes = null;
                                    dBuf = 0;
                                    dChunk = null;
                                }
                                updated++;
                                break;
                            }
                        }

                        if (dsIndexes != null && k == dsIndexes.length) {
                            k = -1;
                        }
                    }
                    if (k == -1) {
                        int j = 0;
                        for (; j < aiaOld.sIndexes.length; j++) {
                            int index = aiaOld.searchColumnIndex(j, i);
                            if (index != -1) {
                                int len;
                                if ((len = fOld.read(buf, j * ChunkSize)) != -1) {
                                    byte[] chunk = createPacket(buf);
                                    fOutOld.write(ByteBuffer.wrap(chunk), i * ChunkSize);
                                    System.out.println("Copied chunk " + j + " to " + i);
                                    aiaOld.array[i] = aiaOld.array[j];
                                    aiaOld.sIndexes[i][0] = i;
                                    if (aiaOld.sIndexes[j].length == 1) {
                                        aiaOld.sIndexes[j][0] = -1;
                                        i = j - 1;
                                    } else {
                                        aiaOld.delIndex(j, index);
                                    }
                                    updated++;
                                    break;
                                }
                            }
                        }
                        if (j == aiaOld.sIndexes.length) {
                            int len;
                            if ((len = fNew.read(buf, i * ChunkSize)) != -1) {
                                byte[] chunk = createPacket(buf);
                                fOutOld.write(ByteBuffer.wrap(chunk), i * ChunkSize);
                                System.out.println("Copied chunk " + i + " from new version");
                                aiaOld.array[i] = iaNew.array[i];
                                aiaOld.sIndexes[i][0] = i;
                                updated++;
                            }
                            System.out.println(len);
                        }
                    }
                } else {
                    if (aiaOld.searchColumnIndex(i, i) != -1) {
                        updated++;
                        notUp++;
                    } else {
                        notUp++;
                    }
                }
            }
            if (updated == iterations) {
                break;
            }
            if (notUp == iterations) {
                for (int i = 0; i < aiaOld.sIndexes.length; i++) {
                    if (aiaOld.searchColumnIndex(i, i) == -1 && aiaOld.sIndexes[i][0] != -1) {
                        int len;
                        if ((len = fOld.read(buf, i * ChunkSize)) != -1) {
                            dChunk = createPacket(buf);
                            dBuf = aiaOld.array[i];
                            dsIndexes = new int[aiaOld.sIndexes[i].length];
                            System.arraycopy(aiaOld.sIndexes[i], 0, dsIndexes, 0, aiaOld.sIndexes[i].length);
                            aiaOld.sIndexes[i] = new int[]{-1};
                        }
                    }
                }
            }
        }
        if (oldVersion.length() > newVersion.length()) {
            fOutOld.truncate(newVersion.length());
            System.out.println("New file smaller -> Old file truncated to " + newVersion.length());
            long[] newArray = new long[(int) newChunks];
            System.arraycopy(aiaOld.array, 0, newArray, 0, (int) newChunks);
        }
        System.out.println("\nFile handling finished");

        System.out.println("\nWriting old.crc...");
        writeDigests(aiaOld.array, "old.crc");
        System.out.println("Success");
    }

}
