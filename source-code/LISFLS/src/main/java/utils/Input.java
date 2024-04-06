package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Input {
    private File file;
    public Input() {
        file = null;
    }

    public Input(File file) {
        this.file = file;
    }

    public boolean open(String filename) {
        file = new File(filename);
        return file.canRead();
    }

    public boolean canRead() {
        return file != null && file.canRead();
    }

    public boolean readDomainAndGoal(List<String> domain, List<String> goals) {
        domain.clear();
        goals.clear();

        int line = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String tempString = null;
            String flag = "None";
            while ((tempString = reader.readLine()) != null) {
                line++;
                String str = tempString.trim();

                if (str.startsWith("//")) {
                    continue;
                } else if (str.startsWith("#") && str.toLowerCase().startsWith("#domain")) {
                    flag = "domain";
                    continue;
                } else if (str.startsWith("#") && str.toLowerCase().startsWith("#goal")) {
                    flag = "goals";
                    continue;
                } else if (str.equals("")) {
                    continue;
                }

                if (flag.equals("domain")) {
                    domain.add(str);
                } else if (flag.equals("goals")) {
                    goals.add(str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in line " + line);
            return false;
        }

        return true;
    }
}
