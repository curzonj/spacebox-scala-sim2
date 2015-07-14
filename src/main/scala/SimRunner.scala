import java.util.TimerTask

import com.github.curzonj.Logging
import com.github.curzonj.WorldTicker

object SimRunner extends Logging {
  def main(args: Array[String]): Unit = {
    WorldTicker.schedule
  }
}
