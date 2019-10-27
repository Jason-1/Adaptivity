import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import java.util.*;

public class MemorySystem 
{

    public interface Listener 
	{

        void memoryUsageLow(long usedMemory, long maxMemory, long allocatedMemory);
    }

    private final Collection<Listener> listeners = new ArrayList<Listener>();

    private static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();
	
	MiniPID pid = new MiniPID(1,0,0); 
	double target;
	double output=0;
	
    public MemorySystem() 
	{
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        emitter.addNotificationListener(new NotificationListener() 
		{
            @Override
            public void handleNotification(Notification n, Object hb) 
			{
				
				pid.setSetpoint(0);
				
				
				//if exceeds memory usage threshold, kick in pid
                if (n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) 
				{
					
                    long maxMemory = tenuredGenPool.getUsage().getMax();
                    long usedMemory = tenuredGenPool.getUsage().getUsed();
					
					target = (double)usedMemory * 1.2;
					long allocatedMemory = 0L; 


					//send to PID
					pid.setSetpoint(target);
					
					while((long)allocatedMemory < usedMemory)
					{
						
						output = pid.getOutput((double)usedMemory, target);
						
						
						allocatedMemory = allocatedMemory + (long)output;
						
						
						/*System.out.println((long)output);
						System.out.println(usedMemory + " /");*/
						
					}

					if(tenuredGenPool.isUsageThresholdSupported())
					{
						
						System.gc();
						if(allocatedMemory < maxMemory)
						{
							//allocates heap size to the value determined by the pid controller
							tenuredGenPool.setUsageThreshold(allocatedMemory);
						}
						else
						{
							tenuredGenPool.setUsageThreshold(maxMemory);
						}
						
						
					}
					
					
                    for (Listener listener : listeners) 
					{
                        listener.memoryUsageLow(usedMemory, maxMemory, allocatedMemory);
                    }
					
                }
				
            }
			
        }, null, null);
    }

    public boolean addListener(Listener listener) 
	{
		 
        return listeners.add(listener);
    }

    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }

    public void setPercentageUsageThreshold(double percentage) 
	{
        if (percentage <= 0.0 || percentage > 1.0) 
		{
            throw new IllegalArgumentException("Percentage not in range");
        }
        long maxMemory = tenuredGenPool.getUsage().getMax();
        long warningThreshold = (long) (maxMemory * percentage);
        tenuredGenPool.setUsageThreshold(warningThreshold);
    }

    private static MemoryPoolMXBean findTenuredGenPool() 
	{
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) 
		{

            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) 
			{
                return pool;
            }
        }
        throw new IllegalStateException("Could not find tenured space");
    }

	
}