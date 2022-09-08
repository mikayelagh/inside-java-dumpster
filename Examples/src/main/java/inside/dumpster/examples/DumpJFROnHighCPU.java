/*
 * 
 */
package inside.dumpster.examples;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import jdk.jfr.Configuration;
import jdk.jfr.consumer.RecordingStream;

/**
 * Example to show how to dump a JFR file when the CPU load is too high. 
 * A recording stream is used to monitor the CPULoad event and then trigger 
 * a JFR file to be written when the CPU load is above a certain level.
 * 
 * @author Joakim Nordstrom joakim.nordstrom@oracle.com
 */
public class DumpJFROnHighCPU {
  public static final double CPU_LEVEL_THRESHOLD = 0.3;
  
  public static void main(String[] args) throws Exception {
    Configuration c = Configuration.getConfiguration("default");
    // create a recording stream using the "default" JFC configuration
    try (RecordingStream rs = new RecordingStream(c)) {
      
      rs.onEvent("jdk.CPULoad", (event) -> {
        // get CPU measurement: "jvmSystem", "jvmUser" or "machineTotal"
        float cpuLevel = event.getFloat("machineTotal");
        
        if(cpuLevel > CPU_LEVEL_THRESHOLD) {
          System.out.println(new SimpleDateFormat("yyyyMMdd HH:mm.ss").format(new Date())+ ": High CPU level noticed: "+cpuLevel);
          try {
            final Path jfrFilePath = generateJFRFilename();
      
            // This will limit a new JFR file to every minute,
            // you'd likely want something smarter here.
            if( ! jfrFilePath.toFile().exists()) {
              rs.dump(jfrFilePath);
              System.out.println("JFR file dumped due to high CPU: " + jfrFilePath);
            }
            
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        } else {
          System.out.println(new SimpleDateFormat("yyyyMMdd HH:mm.ss").format(new Date())+ ": CPU level ok: "+cpuLevel);
        
        }
      });
      
      rs.start();
    }
  }
  
  private static Path generateJFRFilename() {
    final String tmpdir = System.getProperty("java.io.tmpdir");
    final String filename = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    return Path.of(tmpdir, filename + ".jfr");
  }
}
