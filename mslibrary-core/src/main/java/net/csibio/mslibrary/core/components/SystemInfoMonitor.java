package net.csibio.mslibrary.core.components;

import com.google.common.collect.Maps;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.domain.bean.statistic.DiskInfo;
import net.csibio.mslibrary.client.domain.bean.statistic.SystemCpu;
import net.csibio.mslibrary.client.domain.bean.statistic.SystemEnv;
import net.csibio.mslibrary.client.domain.bean.statistic.SystemMem;
import net.csibio.mslibrary.core.config.VMProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.File;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SystemInfoMonitor {

    @Autowired
    VMProperties vmProperties;

    public SystemEnv env() throws UnknownHostException {
        SystemEnv env = new SystemEnv();
        Properties props = System.getProperties();
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        Map<String, String> map = System.getenv();
        String userName = map.get("USERNAME");// 获取用户名
        String computerName = map.get("COMPUTERNAME");// 获取计算机名
        String userDomain = map.get("USERDOMAIN");// 获取计算机域名
        env.setUserName(userName);
        env.setComputerName(computerName);
        env.setUserDomain(userDomain);
        env.setLocalIp(ip);
        env.setHostName(addr.getHostName());
        env.setJavaVendor(props.getProperty("java.vendor"));
        env.setJavaVersion(props.getProperty("java.version"));
        env.setOsName(props.getProperty("os.name"));
        env.setOsArch(props.getProperty("os.arch"));
        env.setOsVersion(props.getProperty("os.version"));
        env.setTimezone(props.getProperty("user.timezone"));
        return env;
    }

    public DiskInfo disk() {
        File file = new File(vmProperties.getRepository());
        DiskInfo info = new DiskInfo();
        info.setPath(file.getPath());
        info.setTotal(file.getTotalSpace());
        info.setAvailable(file.getUsableSpace());
        info.setFree(file.getFreeSpace());
        return info;
    }

    public SystemMem mem() {
        SystemMem mem = new SystemMem();

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();  // 堆内存使用情况
        long initTotalMemorySize = memoryUsage.getInit();      // 初始的总内存
        long maxMemorySize = memoryUsage.getMax(); // 最大可用内存
        long usedMemorySize = memoryUsage.getUsed();  // 已使用的内存

        mem.setJvmInitMem(initTotalMemorySize);
        mem.setJvmMaxMem(maxMemorySize);
        mem.setJvmUsedMem(usedMemorySize);
        mem.setTotalMem(osmxb.getTotalMemorySize());
        mem.setFreeMem(osmxb.getFreeMemorySize());
        mem.setUsedMem(osmxb.getTotalMemorySize() - osmxb.getFreeMemorySize());
        return mem;
    }

    public SystemCpu cpu() throws InterruptedException {
        SystemCpu cpu = new SystemCpu();
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        TimeUnit.MILLISECONDS.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;

        cpu.setCore(processor.getLogicalProcessorCount());
        cpu.setInfo(processor.toString());
        cpu.setTotal(totalCpu);
        cpu.setUser(user);
        cpu.setNice(nice);
        cpu.setSystem(cSys);
        cpu.setIdle(idle);
        cpu.setIowait(iowait);
        cpu.setIrq(irq);
        cpu.setSoftirq(softirq);
        cpu.setSteal(steal);
        return cpu;
    }

    public Map<String, Object> jvm() {
        Map<String, Object> result = Maps.newHashMap();
        // 获得线程总数
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup();
             parentThread.getParent() != null;
             parentThread = parentThread.getParent()) {
        }

        int totalThread = parentThread.activeCount();
        result.put("totalThread", totalThread);
        result.put("PID", System.getProperty("PID"));
        result.put("LibraryPath", ManagementFactory.getRuntimeMXBean().getLibraryPath());
//        result.put("BootClassPath", ManagementFactory.getRuntimeMXBean().getBootClassPath());
        result.put("ClassPath", ManagementFactory.getRuntimeMXBean().getClassPath());
        result.put("ObjectPendingFinalizationCount", ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount());
        result.put("HeapMemoryUsage", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        result.put("NonHeapMemoryUsage", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        result.put("ObjectName", ManagementFactory.getMemoryMXBean().getObjectName());
        result.put("LoadedClassCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
        result.put("TotalLoadedClassCount", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        result.put("TotalCompilationTime", ManagementFactory.getCompilationMXBean().getTotalCompilationTime());
        result.put("Compilation", ManagementFactory.getCompilationMXBean().getName());
        result.put("OperatingSystemMXBean", ManagementFactory.getOperatingSystemMXBean().getName());
        result.put("OperatingSystemMXArch", ManagementFactory.getOperatingSystemMXBean().getArch());
        result.put("AvailableProcessors", ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
        result.put("SystemLoadAverage", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        Map jvmMemPool = Maps.newHashMap();
        //内存池对象
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            jvmMemPool.put(pool.getName(), new HashMap() {
                {
                    put("name", pool.getName());
                    put("Type", pool.getType());
                    put("ObjectName", pool.getObjectName());
                    put("Usage", pool.getUsage().toString());
                    put("PeakUsage", pool.getPeakUsage());
                    put("CollectionUsage", pool.getCollectionUsage());
                }
            });

        }

        result.put("MemoryPool", jvmMemPool);
        Map<String, Object> garbageCollector = Maps.newHashMap();
        // gc
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        // ParallelOld("ParallelOld"),
        // SerialOld("SerialOld"),
        // PSMarkSweep("PSMarkSweep"),
        // ParallelScavenge("ParallelScavenge"),
        // DefNew("DefNew"),
        // ParNew("ParNew"),
        // G1New("G1New"),
        // ConcurrentMarkSweep("ConcurrentMarkSweep"),
        // G1Old("G1Old"),
        // GCNameEndSentinel("GCNameEndSentinel");
        for (GarbageCollectorMXBean gc : gcs) {
            garbageCollector.put(gc.getName(), gc);
        }
        result.put("GarbageCollector", garbageCollector);
        return result;
    }
}
