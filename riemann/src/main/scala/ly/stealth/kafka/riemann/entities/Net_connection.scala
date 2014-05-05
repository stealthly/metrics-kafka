
package ly.stealth.kafka.riemann.entities

import java.util.ArrayList
import java.util.List
import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonProperty, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class Net_connection() {
  var fd: Long = 0
  var family: Long = 0
  @JsonProperty("type")
  var connectionType: Long = 0
  var laddr: List[String] = new ArrayList[String]()
  var raddr: List[Object] = new ArrayList[Object]()
  var status: String = null
  var pid: Object = null
}
