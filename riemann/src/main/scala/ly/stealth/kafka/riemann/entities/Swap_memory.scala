
package ly.stealth.kafka.riemann.entities

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class Swap_memory() {
  var total: Long = 0
  var used: Long = 0
  var free: Long = 0
  var percent: Long = 0
  var sin: Long = 0
  var sout: Long = 0
}
