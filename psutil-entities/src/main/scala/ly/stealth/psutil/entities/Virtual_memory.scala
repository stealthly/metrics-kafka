
package ly.stealth.psutil.entities

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class Virtual_memory() {
  var total: Long = 0
  var available: Long = 0
  var percent: Double = 0
  var used: Long = 0
  var free: Long = 0
  var active: Long = 0
  var inactive: Long = 0
  var buffers: Long = 0
  var cached: Long = 0
}
