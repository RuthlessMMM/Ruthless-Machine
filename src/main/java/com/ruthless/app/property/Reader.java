package com.ruthless.app.property;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Reader {

    private String filename;

    public Reader(String filename) {
        this.filename = filename;
    }

    public String getProperty(String property) {
        Properties prop = new Properties();
        try (InputStream fis = Reader.class.getResourceAsStream(this.filename)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
            System.out.println("file not found: " + this.filename);
            return null;
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
            return null;
        }
        return prop.getProperty(property);
    }
}
