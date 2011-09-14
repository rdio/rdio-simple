import com.rdio.simple.Om;
import com.rdio.simple.Parameters;

public final class Tester {
  public static void main(String[] args) {
    String token = null;
    String tokenSecret = null;
    if (args[4].length() > 0 && args[5].length() > 0) {
      token = args[4];
      tokenSecret = args[5];
    }
    String realm = null;
    if (args[7].length() > 0) {
      realm = args[7];
    }
    System.out.print(Om.sign(
        args[0], args[1], // consumer
        args[2], // url
        Parameters.fromPercentEncoded(args[3]),
        token, tokenSecret,
        args[6], // method
        realm,
        args[8], args[9] // timestamp & nonce
    ));
  }
}