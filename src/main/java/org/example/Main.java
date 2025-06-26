package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        List<Set<String>> groupedLines = new ArrayList<>();
        List<Map<String, Integer>> columnMappings = new ArrayList<>();

        try {
            readAndGroupData(args[0], groupedLines, columnMappings);
            writeGroupsToFile(args[1], groupedLines);
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void readAndGroupData(String inputPath,
                                         List<Set<String>> groups,
                                         List<Map<String, Integer>> mappings) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, groups, mappings);
            }
        }
    }

    private static void processLine(String line,
                                    List<Set<String>> groups,
                                    List<Map<String, Integer>> mappings) {

        String[] fields = line.replace("\"","").split(";");
        if (fields.length == 0) return;

        Integer currentGroup = null;

        for (int i = 0; i < Math.min(mappings.size(), fields.length); i++) {
            Integer existingGroup = mappings.get(i).get(fields[i]);
            if (existingGroup != null) {
                if (currentGroup == null) {
                    currentGroup = existingGroup;
                } else if (!currentGroup.equals(existingGroup)) {
                    mergeGroups(groups, mappings, currentGroup, existingGroup);
                }
            }
        }

        if (currentGroup == null) {
            if (Arrays.stream(fields).anyMatch(f -> !f.isEmpty())) {
                groups.add(new HashSet<>(List.of(line)));
                updateColumnMappings(fields, groups.size() - 1, mappings);
            }
        } else {
            groups.get(currentGroup).add(line);
            updateColumnMappings(fields, currentGroup, mappings);
        }
    }

    private static void mergeGroups(List<Set<String>> groups,
                                    List<Map<String, Integer>> mappings,
                                    int targetGroup,
                                    int sourceGroup) {

        for (String entry : groups.get(sourceGroup)) {
            groups.get(targetGroup).add(entry);
            updateColumnMappings(entry.replace("\"","").split(";"), targetGroup, mappings);
        }
        groups.set(sourceGroup, new HashSet<>());
    }

    private static void updateColumnMappings(String[] fields, int groupId,
                                             List<Map<String, Integer>> mappings) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEmpty()) continue;

            while (mappings.size() <= i) {
                mappings.add(new HashMap<>());
            }

            mappings.get(i).put(fields[i], groupId);
        }
    }

    private static void writeGroupsToFile(String outputPath, List<Set<String>> groups) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            long count = groups.stream().filter(g -> g.size() > 1).count();
            writer.write("Групп размера >1: " + count);
            writer.newLine();

            int groupId = 1;
            for (Set<String> group : groups) {
                writer.newLine();
                writer.write("Группа " + groupId++);
                writer.newLine();
                for (String entry : group) {
                    writer.write(entry);
                    writer.newLine();
                }
            }
        }
    }
}
