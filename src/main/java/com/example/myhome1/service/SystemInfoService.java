package com.example.myhome1.service;

import org.springframework.stereotype.Service;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Service
public class SystemInfoService {
    public String getCpuInfo() {
        OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osMxBean.getProcessCpuLoad();
        // Format the CPU load as a percentage with two decimal places
        return String.format("%.2f%%", cpuLoad * 100);
    }


    public String getMemoryInfo() {
        OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osMxBean.getTotalPhysicalMemorySize();
        long freeMemory = osMxBean.getFreePhysicalMemorySize();
        return "Total Memory: " + totalMemory + " bytes, Free Memory: " + freeMemory + " bytes";
    }
}
