package com.example.myhome1.controller;

import com.example.myhome1.service.SystemInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class SystemInfoController {

    private String executePowerShellCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("powershell.exe -ExecutionPolicy Bypass -NoLogo -NoProfile -Command " + command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error executing PowerShell command: " + e.getMessage();
        }
    }

    @GetMapping("/")
    public String getSystemInfo(Model model) {
        // Use PowerShell to get system information and set it as model attributes
        String cpuInfo = executePowerShellCommand("Get-WmiObject -class Win32_processor");
        String memoryInfo = executePowerShellCommand("Get-WmiObject Win32_ComputerSystem | select TotalPhysicalMemory");
        String diskInfo = executePowerShellCommand("Get-WmiObject -class Win32_DiskDrive");


        // Process anString cpuInfo = executePowerShellCommand("Get-WmiObject -class Win32_processor | select Name");d format CPU and memory information
        cpuInfo = processCpuInfo(cpuInfo);
        memoryInfo = processMemoryInfo(memoryInfo);
        diskInfo = processDiskInfo(diskInfo);

        model.addAttribute("cpuInfo", cpuInfo);
        model.addAttribute("memoryInfo", memoryInfo);

        model.addAttribute("diskInfo", diskInfo);

        return "index";
    }

    private String processDiskInfo(String rawDiskInfo) {
        String[] lines = rawDiskInfo.split("\n");
        StringBuilder diskInfo = new StringBuilder(" ");

        for (String line : lines) {
            if (line.contains("Size")) {
                String size = line.split(": ")[1].trim();
                long sizeInBytes = Long.parseLong(size);
                double sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0);
                String formattedSize = String.format("%.2f GB", sizeInGB);
                diskInfo.append(formattedSize).append("\n");
            }
        }

        return diskInfo.toString();
    }




    private String processCpuInfo(String rawCpuInfo) {
        String[] lines = rawCpuInfo.split("\n");
        StringBuilder cpuInfo = new StringBuilder(" ");

        for (String line : lines) {
            if (line.contains("Name")) {
                String name = line.split(": ")[1];
                cpuInfo.append(name);
                break;
            }
        }

        return cpuInfo.toString();
    }


    private String processMemoryInfo(String rawMemoryInfo) {
        // Remove unwanted characters and whitespace
        String cleanedMemoryInfo = rawMemoryInfo.replaceAll("[^\\d]", "").trim();

        if (cleanedMemoryInfo.isEmpty()) {
            return "N/A";  // Return a message for cases where the memory info is not available
        }

        long totalPhysicalMemory = Long.parseLong(cleanedMemoryInfo);
        long memoryInGB = totalPhysicalMemory / (1024 * 1024 * 1024);

        return memoryInGB + " GB";
    }

}

