
package ly.stealth.psutil.entities

import java.util.ArrayList
import java.util.List
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class PsutilsReport() {
  var net_connections: List[Net_connection] = new ArrayList[Net_connection]()
  var disk_usage: Disk_usage = null
  var cpu_times: Cpu_times = null
  var users: List[User] = new ArrayList[User]()
  var cpu_percent: Double = 0
  var cpu_count: Long = 0
  var cpu_times_percent: Cpu_times_percent = null
  var boot_time: Long = 0
  var swap_memory: Swap_memory = null
  var virtual_memory: Virtual_memory = null
  var disk_io_counters: Disk_io_counters = null
}
