
package ly.stealth.psutil.entities

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class Disk_io_counters() {
  var read_count: Long = 0
  var write_count: Long = 0
  var read_bytes: Long = 0
  var write_bytes: Long = 0
  var read_time: Long = 0
  var write_time: Long = 0
}
