package co.wethinkcode.healthsafe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.wethinkcode.healthsafe.model.Ward;

public class CSVLoader {

    public String filePath = "/wards-outdated.csv";
    public String line = "";
    public static Map<String, String> wing_department = new HashMap<>();

    public List<Ward> getWards() throws IOException {
        List<Ward> wards = new ArrayList<>();

        InputStream inputStream = CSVLoader.class.getResourceAsStream(filePath);

        if (inputStream == null) {
            throw new IOException("Could not find resource: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {

            while ((line = reader.readLine()) != null) {
                Ward ward = returnWard(line);
                wards.add(ward);
            }
        }

        return wards;
    }

    private Ward returnWard(String line) {
        String[] values = line.split(",");
        if (!values[0].equals("ward_id")){
            values = cleanWard(values).split(",");
        }

        return new Ward(values[0], values[1], values[2], values[3]);
    }

    private String cleanWard(String[] ward_array){
        String[] first = ward_array[0].split("-");
        String id = first[0].toLowerCase() + "-" + first[1];
        String wing = handleWing(first[1].toLowerCase());
        String department = handleDepartment(ward_array[2]);
        String bed = handleBeds(ward_array[3]);


        return id + "," + wing + "," + department + "," + bed;
    }

    public void fillMap(){
        wing_department.put("cardiology", "east wing");
        wing_department.put("paediatrics", "west wing");
        wing_department.put("oncology", "north wing");
        wing_department.put("radiology", "south wing");
        wing_department.put("maternity","west wing");
        wing_department.put("icu","east wing");
    }

    public String handleWing(String department) {
        
        return CSVLoader.wing_department.get(department);
    }

    public String handleBeds(String bed){
        Map<String, String> numbers = Map.of("one","1","two","2","three","3",
            "four","4","five","5","six","6","seven","7","eight","8","nine","9",
            "ten","10"
        );
        List<String> capacity = List.of("0","full","N/A");
        if (bed.equals("unknown") || bed.equals("TBD")){return "TBD";}
        if (capacity.contains(bed)){return "at capacity";}
        if (numbers.keySet().contains(bed)){return numbers.get(bed);}
        if (Integer.parseInt(bed) < 0){return "over capacity";}
        
        return "TBD";
    }

    public String handleDepartment(String dep){
        return dep.toLowerCase();
    }
}