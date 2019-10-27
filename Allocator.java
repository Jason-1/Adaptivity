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

public class Allocator 
{
	
	public static void main(String[] args)
	{
		
	
		
		MemorySystem mem = new MemorySystem();
		
		//usage threshold prevents program from wasting memory when waters memory usage is minimal
		mem.setPercentageUsageThreshold(0.05d);
		mem.addListener(new MemorySystem.Listener() 
		{
        @Override
        public void memoryUsageLow(long usedMemory, long maxMemory, long allocatedMemory) 
	
		{
			//pass usedMemory to PID, return 1.2X its value and send that to system
			
			
            System.out.println("Allocation increased: "+((usedMemory/1024)/1024)+"MB Used of "+((allocatedMemory/1024)/1024) + "MB Allocated");
			
        }
		});
		
		
		 Collection<Double> numbers = new LinkedList<Double>();
		 
		 
		 
		 //TEST LOOP, PUT SIMULATION TESTS IN HERE
		 while(true)
		 {
			 
			 
			 numbers.add(Math.random());

			 
		 }
		
		
		 

	}
}

