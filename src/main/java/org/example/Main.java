package org.example;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <inputFile> <outputFile>");
            System.exit(1);
        }

        List<Set<String>> groups = new ArrayList<>();
        List<Map<String, Integer>> columnGroupMapping = new ArrayList<>();

        try {
            processInputFile(args[0], groups, columnGroupMapping);
            writeOutputFile(args[1], groups);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void processInputFile(String inputFile, List<Set<String>> groups, List<Map<String, Integer>> columnGroupMapping) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                handleLine(line, groups, columnGroupMapping);
            }
        }
    }

    private static void handleLine(String line, List<Set<String>> groups, List<Map<String, Integer>> columnGroupMapping) {
        String[] fields = line.split(";");
        Integer groupId = null;

        for (int i = 0; i < Math.min(columnGroupMapping.size(), fields.length); i++) {
            Integer foundGroupId = columnGroupMapping.get(i).get(fields[i]);
            if (foundGroupId != null) {
                if (groupId == null) {
                    groupId = foundGroupId;
                } else if (!groupId.equals(foundGroupId)) {
                    mergeGroups(groups, columnGroupMapping, groupId, foundGroupId);
                }
            }
        }

        if (groupId == null && Arrays.stream(fields).anyMatch(f -> !f.isEmpty())) {
            Set<String> newGroup = new HashSet<>(Collections.singletonList(line));
            groups.add(newGroup);
            updateMappings(fields, groups.size() - 1, columnGroupMapping);
        } else if (groupId != null) {
            groups.get(groupId).add(line);
            updateMappings(fields, groupId, columnGroupMapping);
        }
    }

    private static void mergeGroups(List<Set<String>> groups, List<Map<String, Integer>> columnGroupMapping, int targetGroupId, int sourceGroupId) {
        for (String entry : groups.get(sourceGroupId)) {
            groups.get(targetGroupId).add(entry);
            updateMappings(entry.split(";"), targetGroupId, columnGroupMapping);
        }
        groups.set(sourceGroupId, new HashSet<>());
    }

    private static void updateMappings(String[] values, int groupId, List<Map<String, Integer>> columnGroupMapping) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].isEmpty()) continue;

            if (i < columnGroupMapping.size()) {
                columnGroupMapping.get(i).put(values[i], groupId);
            } else {
                Map<String, Integer> newColumnMap = new HashMap<>();
                newColumnMap.put(values[i], groupId);
                columnGroupMapping.add(newColumnMap);
            }
        }
    }

    private static void writeOutputFile(String outputFile, List<Set<String>> groups) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            long largeGroupsCount = groups.stream().filter(g -> g.size() > 1).count();
            writer.write("Групп размера >1: " + largeGroupsCount);
            writer.newLine();

            int groupId = 1;
            for (Set<String> group : groups) {
                writer.newLine();
                writer.write("Группа " + (groupId++));
                writer.newLine();
                for (String row : group) {
                    writer.write(row);
                    writer.newLine();
                }
            }
        }
    }
}
