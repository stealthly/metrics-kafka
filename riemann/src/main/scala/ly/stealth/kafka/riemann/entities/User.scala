
package ly.stealth.kafka.riemann.entities

import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
case class User() {
  var name: String = null
  var terminal: String = null
  var host: String = null
  var started: Long = 0
}
