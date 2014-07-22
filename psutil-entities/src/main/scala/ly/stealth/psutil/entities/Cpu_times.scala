
package ly.stealth.psutil.entities

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class Cpu_times() {
  var user: Double = 0
  var nice: Double = 0
  var system: Double = 0
  var idle: Double = 0
  var iowait: Double = 0
  var irq: Double = 0
  var softirq: Double = 0
  var steal: Long = 0
  var guest: Long = 0
  var guest_nice: Long = 0
}
