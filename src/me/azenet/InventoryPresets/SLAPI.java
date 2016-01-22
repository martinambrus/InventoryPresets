/*
 * Decompiled with CFR 0_110.
 */
package me.azenet.InventoryPresets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
    public static void save(Object obj, String path) throws Exception {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Object load(String path) throws Exception {
        Object result = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            result = ois.readObject();
            ois.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

