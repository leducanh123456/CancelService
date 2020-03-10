package com.neo.config;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import com.neo.utils.ExtractException;
import com.neo.utils.Utils;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
long    getCommittedVirtualMemorySize()
Returns the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported.

long    getFreePhysicalMemorySize()
Returns the amount of free physical memory in bytes.

long    getFreeSwapSpaceSize()
Returns the amount of free swap space in bytes.

double  getProcessCpuLoad()
Returns the "recent cpu usage" for the Java Virtual Machine process.

long    getProcessCpuTime()
Returns the CPU time used by the process on which the Java virtual machine is running in nanoseconds.

double  getSystemCpuLoad()
Returns the "recent cpu usage" for the whole system.

long    getTotalPhysicalMemorySize()
Returns the total amount of physical memory in bytes.

long    getTotalSwapSpaceSize()
Returns the total amount of swap space in bytes.
 */

@SuppressWarnings({"resource", "restriction"})
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceManagement {

	private final Logger logger = LoggerFactory.getLogger(ResourceManagement.class);

	private long lastUpTime = 0;
	private Map<Long, Long> previousThreadCPUTime = new HashMap<Long, Long>();
	private int sortIndex = 3;
	private boolean reverseSort = true;
	private RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
	private OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	private ThreadMXBean threads = ManagementFactory.getThreadMXBean();
	private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
	private ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

	/**
	 * gc : garbage collection
	 */
	public void gc() {
		try {
			Runtime.getRuntime().gc();
			Date date = new Date();
			logger.info(String.format("Garbage collection successful at time: %8tF-%8tT", date, date));
		} catch (Exception e) {
			logger.error("Garbage Collection Exception: {}", ExtractException.exceptionToString(e));
		}
	}

	public void printUsage() {
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
				Object value;
				try {
					value = method.invoke(operatingSystemMXBean);
				} catch (Exception e) {
					value = e;
				} // try
				System.out.println(method.getName() + " = " + value);
			} // if
		} // for
		System.out.println("============================> " + (operatingSystemMXBean.getProcessCpuLoad() * 100 ) );
	}

	/**
	 * print info resource
	 * 
	 * @return
	 */
	public void reportResource() {
		StringBuilder result = new StringBuilder("\r\n");
		Thread.currentThread().setName("ThreadMonitor");
		// Clear console, then print JVM stats
		// printOperatingSystemHeader
		result.append(String.format("Report Resource at Time %8tT, %6s, %2d cpus, %15.15s, CPU load %3.2f %%, Process VM time: %d nanoseconds %n", new Date(), os.getArch(), os.getAvailableProcessors(), os.getName() + " " + os.getVersion(),  (os.getProcessCpuLoad() * 100 ), TimeUnit.SECONDS.convert(os.getProcessCpuTime(), TimeUnit.NANOSECONDS)));

		// printThreadsHeader(runtime, threads);
		result.append(String.format("UpTime: %-7s Threads: %-4d ThreadsPeak: %-4d ThreadsCreated: %-4d %n", timeUnitToHoursMinutes(MILLISECONDS, runtime.getUptime()), threads.getThreadCount(), threads.getPeakThreadCount(), threads.getTotalStartedThreadCount()));

		// printGCHeader();
		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			float time = gc.getCollectionTime();
			result.append(String.format("Garbage collector: Name: %-16s %-12s %d Time: %5.3f ms %n", gc.getName(), "Collections:", gc.getCollectionCount(), time));
		}

		// printClassLoaderHeader(cl);
		result.append(String.format("CurrentClassesLoaded: %-8d TotalClassesLoaded: %-8d TotalClassesUnloaded: %-8d %n", cl.getLoadedClassCount(), cl.getTotalLoadedClassCount(), cl.getUnloadedClassCount()));
		// print MEMORY
		MemoryUsage memoryUsage = memoryBean.getHeapMemoryUsage();
		result.append(String.format("%-16s[init:%10s, used:%10s, committed:%10s, max:%10s, free: %5.2f %%] %n", "Heap memory", Utils.getSize(memoryUsage.getInit()), Utils.getSize(memoryUsage.getUsed()), Utils.getSize(memoryUsage.getCommitted()), Utils.getSize(memoryUsage.getMax()), ((float) (memoryUsage.getMax() - memoryUsage.getUsed()) / memoryUsage.getMax() * 100)));
		memoryUsage = memoryBean.getNonHeapMemoryUsage();
		result.append(String.format("%-16s[init:%10s, used:%10s, committed:%10s, max:%10s] %n", "Non-heap memory", Utils.getSize(memoryUsage.getInit()), Utils.getSize(memoryUsage.getUsed()), Utils.getSize(memoryUsage.getCommitted()), Utils.getSize(memoryUsage.getMax())));

		// print disk
		File root = new File(System.getProperty("user.dir"));
		result.append("Home Directory	[").append("Total space: ").append(Utils.getSize(root.getTotalSpace())).append(", Used: ").append(Utils.getSize(root.getTotalSpace() - root.getFreeSpace())).append(", Free space: ").append(Utils.getSize(root.getFreeSpace())).append("]\n");
		// print thread
		if (logger.isDebugEnabled()) {
			result.append(String.format("%8s %-40s  %13s %5s %8s %5s %n", "TheadID", "THREAD NAME", "STATE", "CPU", "CPU-TIME", "BLOCKEDBY"));
			result.append(printTopThreads());
		}
		
		logger.info(result.toString());
	}

	private String printTopThreads() {
		// Test if this JVM supports telling us thread stats!
		String result = "";
		if (threads.isThreadCpuTimeSupported()) {

			long uptime = runtime.getUptime();
			long deltaUpTime = uptime - lastUpTime;
			lastUpTime = uptime;

			Map<Long, Object[]> stats = getThreadStats(threads, deltaUpTime);

			List<Long> sortedKeys = sortByValue(stats);

			// Display threads
			result = printThreads(sortedKeys, stats);

		} else {
			result = String.format("%n -Thread CPU metrics are not available on this jvm/platform-%n");
		}
		return result;
	}

	private String printThreads(List<Long> sortedKeys, Map<Long, Object[]> stats) {
		StringBuilder result = new StringBuilder();
		for (Long tid : sortedKeys) {
			result.append(String.format("%8d %-40s  %13s %5.2f%% %8s %5s %n", stats.get(tid)[0], stats.get(tid)[1], stats.get(tid)[2], stats.get(tid)[3], stats.get(tid)[4], stats.get(tid)[5]));
		}
		return result.toString();
	}

	private Map<Long, Object[]> getThreadStats(ThreadMXBean threads, long deltaUpTime) {
		Map<Long, Object[]> allStats = new HashMap<Long, Object[]>();

		for (Long tid : threads.getAllThreadIds()) {

			ThreadInfo info = threads.getThreadInfo(tid);

			if (info != null) {
				Object[] stats = new Object[6];
				long threadCpuTime = threads.getThreadCpuTime(tid);
				long deltaThreadCpuTime;

				if (previousThreadCPUTime.containsKey(tid)) {
					deltaThreadCpuTime = threadCpuTime - previousThreadCPUTime.get(tid);
				} else {
					deltaThreadCpuTime = threadCpuTime;
				}

				previousThreadCPUTime.put(tid, threadCpuTime);

				String name = info.getThreadName();
				stats[0] = tid;
				stats[1] = name.substring(0, Math.min(name.length(), 40));
				stats[2] = info.getThreadState();
				stats[3] = getThreadCPUUtilization(deltaThreadCpuTime, deltaUpTime);
				stats[4] = timeUnitToMinutesSeconds(NANOSECONDS, threads.getThreadCpuTime(tid));
				stats[5] = getBlockedThread(info);

				allStats.put(tid, stats);
			}
		}

		return allStats;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Long> sortByValue(Map map) {
		List<Map.Entry> list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry>() {

			public int compare(Map.Entry o1, Map.Entry o2) {
				Comparable c1 = ((Comparable) (((Object[]) o1.getValue())[sortIndex]));
				Comparable c2 = ((Comparable) (((Object[]) o2.getValue())[sortIndex]));
				return c1.compareTo(c2);
			}
		});

		if (reverseSort) {
			Collections.reverse(list);
		}

		List result = new ArrayList();
		for (Iterator<Map.Entry> it = list.iterator(); it.hasNext();) {
			result.add(it.next().getKey());
		}
		return result;
	}

	private String getBlockedThread(ThreadInfo info) {
		if (info.getLockOwnerId() >= 0) {
			return "" + info.getLockOwnerId();
		} else {
			return "";
		}
	}

	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime) {
		return getThreadCPUUtilization(deltaThreadCpuTime, totalTime, 1000 * 1000);
	}

	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime, double factor) {
		if (totalTime == 0) {
			return 0;
		}
		return deltaThreadCpuTime / factor / totalTime * 100d;
	}

	private String timeUnitToHoursMinutes(TimeUnit timeUnit, long value) {
		if (value == -1) {
			return "0";
		}
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		long hours = HOURS.convert(value, timeUnit);
		long minutes = MINUTES.convert(value, timeUnit) - MINUTES.convert(hours, HOURS);
		formatter.format("%2d:%02dm", hours, minutes);
		return sb.toString();
	}

	public String timeUnitToMinutesSeconds(TimeUnit timeUnit, long value) {
		if (value == -1) {
			return "0";
		}
		long valueRemaining = value;
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);

		long minutes = MINUTES.convert(valueRemaining, timeUnit);
		valueRemaining = valueRemaining - timeUnit.convert(minutes, MINUTES);
		long seconds = SECONDS.convert(valueRemaining, timeUnit);
		valueRemaining = valueRemaining - timeUnit.convert(seconds, SECONDS);
		long nanoseconds = NANOSECONDS.convert(valueRemaining, timeUnit);
		// min so that 99.5+ does not show up as 100 hundredths of a second
		int hundredthsOfSecond = Math.min(Math.round(nanoseconds / 10000000f), 99);
		formatter.format("%2d:%02d.%02d", minutes, seconds, hundredthsOfSecond);
		return sb.toString();
	}
}