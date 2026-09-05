package co.wethinkcode.healthsafe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.wethinkcode.healthsafe.model.Doctor;

public class DataLoader {
    public static String filePath = "/doctors.csv";
    public static String line = "";
    public static List<Doctor> doctors = new ArrayList<>();
    public static Map<Doctor, String> doc_dep_map = new HashMap<>();

    public static void loadDoctors() throws IOException {
        InputStream inputStream = DataLoader.class.getResourceAsStream(filePath);

        if (inputStream == null){
            throw new IOException("Could not find resource: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream)
        )) {
            while ((line = reader.readLine()) != null){
                Doctor doc = getDoctor(line);
                doctors.add(doc);
                doc_dep_map.put(doc, doc.getSpecialty());
            }
        }

    }

    public static Doctor getDoctor(String line){
        if (!line.contains("doctor_id")) {
            String[] array = line.split(",");
            Doctor doc = new Doctor(Integer.parseInt(array[0]), array[3], array[2]);
            return doc;
        }
        return null;
    }
}

/*
    public List<Ward> getWards() throws IOException {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {

            while ((line = reader.readLine()) != null) {
                Ward ward = returnWard(line);
                ward_list.add(ward);
            }
        }

        return List.copyOf(ward_list);
    } */