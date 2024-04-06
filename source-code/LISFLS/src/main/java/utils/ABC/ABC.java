package utils.ABC;

import java.io.*;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class ABC {
  public static BigInteger count(LinkedList<String> formulas, long bound, long timeout) {

    String constraint = "(set-logic QF_S)\n"
		+ "(declare-fun x () String)\n";
    
    for(String f : formulas){
    	constraint+= "(assert (in x /"+f+"/))\n";
    }
    constraint += "(check-sat)\n";

      BigInteger count = BigInteger.ZERO;
      try {
          count = invokeABC(constraint, bound, timeout);
      } catch (IOException e) {
          e.printStackTrace();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
    return count;
  }

  protected static BigInteger invokeABC(String content, long bound, long timeout) throws IOException, InterruptedException {
//      long startTime = System.currentTimeMillis();

      final String abcCmd = "abc -v 0 -bs " + bound;
      Process p = Runtime.getRuntime().exec(abcCmd); //执行命令
      OutputStream o = p.getOutputStream();
      OutputStreamWriter ou = new OutputStreamWriter(o);
      BufferedWriter OUT = new BufferedWriter(ou);
      OUT.write(content);
      OUT.close();
      ou.close();
      o.close();

      boolean timeoutFlag = false;
      if(!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
          timeoutFlag = true; //kill the process.
          p.destroy(); // consider using destroyForcibly instead
      }
      BigInteger ret = BigInteger.ZERO;

      if (!timeoutFlag) {
          String aux;
          String line = "";
          InputStream in = p.getErrorStream(); // 结果打印在错误流中
          InputStreamReader inread = new InputStreamReader(in);
          BufferedReader bufferedreader = new BufferedReader(inread);
          while ((aux = bufferedreader.readLine()) != null) {
              if (aux.contains("report bound:")) {
                  line = aux.split("count:")[1].split("time")[0].trim();
                  ret = new BigInteger(line);
                  break;
              }
          }
      }

      if (p != null) {
          p.destroy();
      }

      return ret;
  }


}
